package com.example.myphonec

import android.content.Context
import android.opengl.EGL14
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mobile-optimized GPU benchmark renderer.
 *
 * Workload (single render pass to default framebuffer):
 *   - 128×256 UV sphere (~33k vertices, ~65k tris) uploaded once as static VBO.
 *   - Fragment shader: 32-octave value-noise FBM + 4 dynamic Phong lights.
 *     ALU-heavy, zero texture lookups — stresses what mobile GPUs are weakest at.
 *
 * Frame timing uses a 120-frame rolling deltaNs window. No glFinish, no FBO,
 * no per-frame allocation. FPS samples are emitted every ~200ms with a wall-clock
 * timestamp so the ViewModel can do thermal-segment analysis.
 */
class BenchmarkRenderer(@Suppress("unused") private val context: Context) : GLSurfaceView.Renderer {

    enum class Phase { IDLE, WARMUP, MEASUREMENT }

    companion object {
        private const val TAG = "BenchmarkRenderer"
        private const val STACKS = 128
        private const val SLICES = 256
        private const val SPHERE_RADIUS = 2.5f
        private const val FRAME_WINDOW = 120
        private const val SAMPLE_EMIT_INTERVAL_NS = 200_000_000L
    }

    @Volatile var phase: Phase = Phase.IDLE
    @Volatile var lastError: String? = null
        private set

    val frameCounter = AtomicLong(0L)

    var onFpsSample: ((Float, Long) -> Unit)? = null
    var onGpuDetected: ((String) -> Unit)? = null

    // ─── Shaders ──────────────────────────────────────────────────────────────

    private val sphereVS = """
        #version 300 es
        layout(location = 0) in vec3 aPos;
        layout(location = 1) in vec3 aNormal;
        uniform mat4 uVP;
        out vec3 vWorldPos;
        out vec3 vNormal;
        out vec2 vUV;
        void main() {
            vWorldPos = aPos;
            vNormal = aNormal;
            vUV = aPos.xy * 0.25 + 0.5;
            gl_Position = uVP * vec4(aPos, 1.0);
        }
    """.trimIndent()

    private val fbmFS = """
        #version 300 es
        precision highp float;

        in vec3 vWorldPos;
        in vec3 vNormal;
        in vec2 vUV;
        uniform float uTime;
        uniform vec3 uCamPos;
        out vec4 fragColor;

        float hash(vec3 p) {
            p = fract(p * vec3(443.8975, 397.2973, 491.1871));
            p += dot(p, p.yxz + 19.27);
            return fract((p.x + p.y) * p.z);
        }

        float noise(vec3 p) {
            vec3 i = floor(p);
            vec3 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            return mix(
                mix(mix(hash(i), hash(i + vec3(1.0, 0.0, 0.0)), f.x),
                    mix(hash(i + vec3(0.0, 1.0, 0.0)), hash(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
                mix(mix(hash(i + vec3(0.0, 0.0, 1.0)), hash(i + vec3(1.0, 0.0, 1.0)), f.x),
                    mix(hash(i + vec3(0.0, 1.0, 1.0)), hash(i + vec3(1.0, 1.0, 1.0)), f.x), f.y),
                f.z);
        }

        void main() {
            vec3 p = vWorldPos * 2.0;
            float n = 0.0;

            // 32 octaves FBM — ~256 hash calls per fragment.
            float amp = 1.0;
            float freq = 1.0;
            for (int i = 0; i < 32; i++) {
                n += amp * noise(p * freq + uTime * 0.5);
                freq *= 2.0;
                amp *= 0.5;
            }

            vec3 N = normalize(vNormal);
            vec3 V = normalize(uCamPos - vWorldPos);
            vec3 color = vec3(0.0);

            // 4 animated lights × Phong.
            for (int i = 0; i < 4; i++) {
                float fi = float(i);
                vec3 lightPos = vec3(
                    sin(uTime + fi * 1.57) * 5.0,
                    cos(uTime * 0.7 + fi) * 5.0,
                    sin(uTime * 1.3 + fi * 0.8) * 5.0
                );
                vec3 L = normalize(lightPos - vWorldPos);
                vec3 H = normalize(L + V);

                float diff = max(0.0, dot(N, L));
                float spec = pow(max(0.0, dot(N, H)), 64.0);

                vec3 lightCol = vec3(
                    0.5 + 0.5 * sin(fi * 2.1),
                    0.5 + 0.5 * cos(fi * 1.7),
                    0.5 + 0.5 * sin(fi * 3.3)
                );

                color += lightCol * (diff + spec) * (0.5 + 0.5 * n);
            }

            // Reinhard tone map + gamma.
            color = color / (1.0 + color);
            color = pow(color, vec3(1.0 / 2.2));

            fragColor = vec4(color, 1.0);
        }
    """.trimIndent()

    // ─── GL state ─────────────────────────────────────────────────────────────

    private var program = 0
    private var uVPLoc = -1
    private var uTimeLoc = -1
    private var uCamPosLoc = -1

    private var vboSphere = 0
    private var eboSphere = 0
    private var vaoSphere = 0
    private var indexCount = 0

    private var surfaceWidth = 1
    private var surfaceHeight = 1

    private val vpMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val camPos = FloatArray(3)

    // Frame-time ring buffer (no per-frame allocation).
    private val frameTimesNs = LongArray(FRAME_WINDOW)
    private var frameTimesCount = 0
    private var frameTimesHead = 0
    private var lastFrameTimeNs = 0L
    private var lastSampleEmitNs = 0L

    // ─── External control ────────────────────────────────────────────────────

    fun beginWarmup() {
        frameCounter.set(0L)
        resetTiming()
        lastError = null
        phase = Phase.WARMUP
    }

    fun beginMeasurement() {
        resetTiming()
        phase = Phase.MEASUREMENT
    }

    fun stop() {
        phase = Phase.IDLE
    }

    private fun resetTiming() {
        frameTimesCount = 0
        frameTimesHead = 0
        lastFrameTimeNs = 0L
        lastSampleEmitNs = 0L
    }

    // ─── GLSurfaceView.Renderer ──────────────────────────────────────────────

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        try {
            // Best-effort: many devices ignore this and stay v-synced. That's OK —
            // the workload-per-frame measurement is still valid either way.
            try {
                EGL14.eglSwapInterval(EGL14.eglGetCurrentDisplay(), 0)
            } catch (e: Throwable) {
                Log.w(TAG, "Cannot disable vsync: ${e.message}")
            }
            initGl()
            onGpuDetected?.invoke(GLES30.glGetString(GLES30.GL_RENDERER) ?: "Unknown GPU")
            lastError = null
        } catch (e: Throwable) {
            Log.e(TAG, "GL init failed", e)
            lastError = "GL init failed: ${e.message}"
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        surfaceWidth = width.coerceAtLeast(1)
        surfaceHeight = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, surfaceWidth, surfaceHeight)
        val ratio = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 55f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(unused: GL10?) {
        if (lastError != null) return

        if (phase == Phase.IDLE) {
            GLES30.glClearColor(0.02f, 0.02f, 0.04f, 1f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            return
        }

        val now = System.nanoTime()

        try {
            renderScene()
        } catch (e: Throwable) {
            Log.e(TAG, "Draw failed", e)
            lastError = "Draw failed: ${e.message}"
            return
        }

        // Frame-time bookkeeping. No glFinish — we measure presentation cadence,
        // which is what the user actually sees.
        if (lastFrameTimeNs != 0L) {
            val deltaNs = now - lastFrameTimeNs
            frameTimesNs[frameTimesHead] = deltaNs
            frameTimesHead = (frameTimesHead + 1) % frameTimesNs.size
            if (frameTimesCount < frameTimesNs.size) frameTimesCount++

            if (phase == Phase.MEASUREMENT) {
                if (lastSampleEmitNs == 0L) lastSampleEmitNs = now
                if (now - lastSampleEmitNs >= SAMPLE_EMIT_INTERVAL_NS && frameTimesCount >= 30) {
                    var sum = 0L
                    for (i in 0 until frameTimesCount) sum += frameTimesNs[i]
                    val avgDeltaNs = sum / frameTimesCount
                    if (avgDeltaNs > 0L) {
                        val fps = 1_000_000_000.0f / avgDeltaNs.toFloat()
                        onFpsSample?.invoke(fps, System.currentTimeMillis())
                    }
                    lastSampleEmitNs = now
                }
            }
        }
        lastFrameTimeNs = now
        frameCounter.incrementAndGet()
    }

    // ─── Rendering ────────────────────────────────────────────────────────────

    private fun renderScene() {
        val time = (SystemClock.elapsedRealtime() % 1_000_000L) / 1000f

        GLES30.glViewport(0, 0, surfaceWidth, surfaceHeight)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClearColor(0.02f, 0.02f, 0.04f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Camera orbit defeats GPU hidden-surface caching across frames.
        val camRadius = 6f
        camPos[0] = sin(time * 0.3f) * camRadius
        camPos[1] = sin(time * 0.5f) * 2f
        camPos[2] = cos(time * 0.3f) * camRadius

        Matrix.setLookAtM(viewMatrix, 0,
            camPos[0], camPos[1], camPos[2],
            0f, 0f, 0f,
            0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        GLES30.glUseProgram(program)
        GLES30.glUniformMatrix4fv(uVPLoc, 1, false, vpMatrix, 0)
        GLES30.glUniform1f(uTimeLoc, time)
        GLES30.glUniform3f(uCamPosLoc, camPos[0], camPos[1], camPos[2])

        GLES30.glBindVertexArray(vaoSphere)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private fun initGl() {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClearColor(0.02f, 0.02f, 0.04f, 1f)
        program = linkChecked(sphereVS, fbmFS, "fbm-sphere")
        uVPLoc = GLES30.glGetUniformLocation(program, "uVP")
        uTimeLoc = GLES30.glGetUniformLocation(program, "uTime")
        uCamPosLoc = GLES30.glGetUniformLocation(program, "uCamPos")
        buildSphere()
    }

    private fun buildSphere() {
        val vertCount = (STACKS + 1) * (SLICES + 1)
        // 6 floats per vertex: pos(xyz) + normal(xyz).
        val verts = FloatArray(vertCount * 6)
        var v = 0
        for (s in 0..STACKS) {
            val phi = PI * s / STACKS                  // 0..π
            val sinPhi = sin(phi).toFloat()
            val cosPhi = cos(phi).toFloat()
            for (sl in 0..SLICES) {
                val theta = 2.0 * PI * sl / SLICES     // 0..2π
                val sinTh = sin(theta).toFloat()
                val cosTh = cos(theta).toFloat()
                val nx = sinPhi * cosTh
                val ny = cosPhi
                val nz = sinPhi * sinTh
                verts[v++] = nx * SPHERE_RADIUS
                verts[v++] = ny * SPHERE_RADIUS
                verts[v++] = nz * SPHERE_RADIUS
                verts[v++] = nx
                verts[v++] = ny
                verts[v++] = nz
            }
        }

        val rowStride = SLICES + 1
        val indices = IntArray(STACKS * SLICES * 6)
        var i = 0
        for (s in 0 until STACKS) {
            for (sl in 0 until SLICES) {
                val a = s * rowStride + sl
                val b = a + 1
                val c = a + rowStride
                val d = c + 1
                indices[i++] = a; indices[i++] = c; indices[i++] = b
                indices[i++] = b; indices[i++] = c; indices[i++] = d
            }
        }
        indexCount = indices.size

        val vBuf = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder())
        vBuf.asFloatBuffer().put(verts); vBuf.position(0)
        val iBuf = ByteBuffer.allocateDirect(indices.size * 4).order(ByteOrder.nativeOrder())
        iBuf.asIntBuffer().put(indices); iBuf.position(0)

        val ids = IntArray(2); GLES30.glGenBuffers(2, ids, 0)
        vboSphere = ids[0]; eboSphere = ids[1]

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboSphere)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, verts.size * 4, vBuf, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboSphere)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, iBuf, GLES30.GL_STATIC_DRAW)

        val vaoArr = IntArray(1); GLES30.glGenVertexArrays(1, vaoArr, 0)
        vaoSphere = vaoArr[0]
        GLES30.glBindVertexArray(vaoSphere)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboSphere)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 3 * 4)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboSphere)
        GLES30.glBindVertexArray(0)
    }

    private fun linkChecked(vsCode: String, fsCode: String, label: String): Int {
        val vs = compileChecked(GLES30.GL_VERTEX_SHADER, vsCode, "$label-vs")
        val fs = compileChecked(GLES30.GL_FRAGMENT_SHADER, fsCode, "$label-fs")
        val prog = GLES30.glCreateProgram()
        GLES30.glAttachShader(prog, vs)
        GLES30.glAttachShader(prog, fs)
        GLES30.glLinkProgram(prog)
        val status = IntArray(1)
        GLES30.glGetProgramiv(prog, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(prog)
            throw RuntimeException("Link $label failed: $info")
        }
        return prog
    }

    private fun compileChecked(type: Int, code: String, label: String): Int {
        val s = GLES30.glCreateShader(type)
        GLES30.glShaderSource(s, code)
        GLES30.glCompileShader(s)
        val status = IntArray(1)
        GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(s)
            throw RuntimeException("Compile $label failed: $info")
        }
        return s
    }
}
