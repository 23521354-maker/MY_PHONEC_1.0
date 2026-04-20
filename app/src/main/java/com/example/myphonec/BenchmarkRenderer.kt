package com.example.myphonec

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class BenchmarkRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val cubeCoords = floatArrayOf(
        -1.0f,  1.0f,  1.0f,   1.0f,  1.0f,  1.0f,   1.0f, -1.0f,  1.0f,  -1.0f, -1.0f,  1.0f,
        -1.0f,  1.0f, -1.0f,   1.0f,  1.0f, -1.0f,   1.0f, -1.0f, -1.0f,  -1.0f, -1.0f, -1.0f
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 0, 1, 5, 0, 5, 4, 2, 3, 7, 2, 7, 6, 1, 2, 6, 1, 6, 5, 0, 3, 7, 0, 7, 4
    )

    private val vertexShaderCode = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        layout(location = 0) in vec4 vPosition;
        out vec3 vPos;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vPos = (uModelMatrix * vPosition).xyz;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec3 vPos;
        out vec4 fragColor;
        uniform float uTime;
        uniform vec3 uLightPos;
        void main() {
            vec3 color = vec3(0.13, 0.83, 0.93); // Neon Cyan
            float dist = distance(vPos, uLightPos);
            float diff = 1.0 / (1.0 + 0.1 * dist * dist);
            
            // Pulse glow
            float pulse = sin(uTime * 4.0) * 0.1 + 0.9;
            
            // Scanline effect
            float scanline = sin(vPos.y * 10.0 + uTime * 5.0) * 0.05 + 0.95;
            
            fragColor = vec4(color * diff * pulse * scanline, 0.8);
        }
    """.trimIndent()

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var drawListBuffer: ShortBuffer
    private var program: Int = 0
    private var vPMatrixHandle: Int = 0
    private var modelMatrixHandle: Int = 0
    private var timeHandle: Int = 0
    private var lightPosHandle: Int = 0

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var frameCount = 0
    private var startTime = SystemClock.elapsedRealtime()
    var onFpsUpdate: ((Int) -> Unit)? = null
    var onGpuInfoDetected: ((String) -> Unit)? = null

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.04f, 0.04f, 0.04f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        val gpuName = GLES30.glGetString(GLES30.GL_RENDERER)
        onGpuInfoDetected?.invoke(gpuName ?: "Unknown GPU")

        val bb = ByteBuffer.allocateDirect(cubeCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(cubeCoords)
        vertexBuffer.position(0)

        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val time = SystemClock.elapsedRealtime() % 1000000L / 1000.0f
        
        GLES30.glUseProgram(program)
        vPMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix")
        modelMatrixHandle = GLES30.glGetUniformLocation(program, "uModelMatrix")
        timeHandle = GLES30.glGetUniformLocation(program, "uTime")
        lightPosHandle = GLES30.glGetUniformLocation(program, "uLightPos")

        GLES30.glUniform1f(timeHandle, time)
        GLES30.glUniform3f(lightPosHandle, sin(time) * 5f, cos(time) * 5f, 2f)

        // Camera movement (Phase 1.4)
        val camX = sin(time * 0.5f) * 10f
        val camZ = cos(time * 0.5f) * 10f
        Matrix.setLookAtM(viewMatrix, 0, camX, 5f, camZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Render 7x7x3 = 147 cubes for stress (Phase 1.4)
        for (i in -3..3) {
            for (j in -3..3) {
                for (k in -1..1) {
                    Matrix.setIdentityM(modelMatrix, 0)
                    Matrix.translateM(modelMatrix, 0, i * 3f, k * 4f, j * 3f)
                    Matrix.rotateM(modelMatrix, 0, time * 60f + (i * j), 1f, 1f, 0f)
                    
                    Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
                    val mvp = FloatArray(16)
                    Matrix.multiplyMM(mvp, 0, vPMatrix, 0, modelMatrix, 0)

                    GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvp, 0)
                    GLES30.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
                    
                    GLES30.glEnableVertexAttribArray(0)
                    GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
                    GLES30.glDrawElements(GLES30.GL_TRIANGLES, drawOrder.size, GLES30.GL_UNSIGNED_SHORT, drawListBuffer)
                }
            }
        }

        frameCount++
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - startTime >= 1000) {
            onFpsUpdate?.invoke(frameCount)
            frameCount = 0
            startTime = currentTime
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
        }
    }
}
