package com.example.myphonec

import android.util.Log
import com.example.myphonec.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repository for AI-powered build suggestions.
 * Uses Google AI Studio (Gemini Developer API) via REST.
 */
class BuildAiRepository {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY
    private val model: String = "gemini-2.5-flash"
    private val endpoint: String =
        "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    suspend fun suggestBuild(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val history = listOf(ChatMessage(prompt, true))
            val response = withTimeoutOrNull(30000L) {
                callGemini(history)
            }
            when {
                response == null -> Result.failure(Exception("AI phản hồi quá chậm (Timeout). Vui lòng thử lại."))
                response.isBlank() -> Result.failure(Exception("AI không trả về nội dung. Thử lại với câu lệnh khác."))
                else -> Result.success(response)
            }
        } catch (e: Exception) {
            Log.e("AI_SAFE_CALL", "Error generating content: ${e.message}", e)
            Result.failure(mapGeminiError(e))
        }
    }

    suspend fun chatWithAi(history: List<ChatMessage>, message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fullHistory = history.takeLast(10) + ChatMessage(message, true)
            val response = withTimeoutOrNull(20000L) {
                callGemini(fullHistory)
            }
            when {
                response == null -> Result.failure(Exception("Phiên chat bị quá hạn (Timeout)."))
                response.isBlank() -> Result.failure(Exception("Phản hồi từ AI trống."))
                else -> Result.success(response)
            }
        } catch (e: Exception) {
            Log.e("AI_SAFE_CALL", "Chat session error: ${e.message}", e)
            Result.failure(mapGeminiError(e))
        }
    }

    private fun callGemini(history: List<ChatMessage>): String {
        val contents = JSONArray()
        for (msg in history) {
            val parts = JSONArray().put(JSONObject().put("text", msg.text))
            contents.put(
                JSONObject()
                    .put("role", if (msg.isUser) "user" else "model")
                    .put("parts", parts)
            )
        }

        val body = JSONObject()
            .put("contents", contents)
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.7)
                    .put("topP", 0.95)
                    .put("maxOutputTokens", 4096)
            )
            .toString()

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 25000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        try {
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw Exception("HTTP $code: ${extractErrorMessage(raw)}")
            }

            return extractText(raw)
        } finally {
            conn.disconnect()
        }
    }

    private fun extractText(raw: String): String {
        val json = JSONObject(raw)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) {
            val feedback = json.optJSONObject("promptFeedback")
            val reason = feedback?.optString("blockReason")
            if (!reason.isNullOrBlank()) throw Exception("Nội dung bị chặn: $reason")
            return ""
        }
        val parts = candidates.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val t = parts.getJSONObject(i).optString("text", "")
            if (t.isNotEmpty()) sb.append(t)
        }
        return sb.toString().trim()
    }

    private fun extractErrorMessage(raw: String): String {
        return try {
            JSONObject(raw).optJSONObject("error")?.optString("message") ?: raw.take(200)
        } catch (_: Exception) {
            raw.take(200)
        }
    }

    private fun mapGeminiError(e: Exception): Exception {
        val msg = e.message ?: ""
        return when {
            msg.contains("401") || msg.contains("API key", ignoreCase = true) ->
                Exception("API key không hợp lệ. Vui lòng kiểm tra cấu hình.")
            msg.contains("403") || msg.contains("denied", ignoreCase = true) ->
                Exception("Truy cập bị từ chối. Kiểm tra quyền API trong Google AI Studio.")
            msg.contains("429") || msg.contains("quota", ignoreCase = true) ->
                Exception("Giới hạn sử dụng AI hôm nay đã hết. Vui lòng thử lại sau.")
            msg.contains("network", ignoreCase = true) ||
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ->
                Exception("Lỗi kết nối mạng. Vui lòng kiểm tra Wifi hoặc dữ liệu di động.")
            else -> Exception("Đã xảy ra lỗi khi kết nối với AI: ${e.localizedMessage ?: msg}")
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
