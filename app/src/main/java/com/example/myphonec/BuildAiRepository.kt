package com.example.myphonec

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Repository for AI-powered build suggestions.
 * Strictly using Firebase AI Logic with Gemini Developer API.
 */
class BuildAiRepository {

    // 1. Initialize Firebase AI Logic - Sử dụng Gemini 3 Flash Preview
    // Firebase.ai tự động sử dụng Google AI Backend (Gemini Developer API)
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3-flash-preview")
    }

    /**
     * Hàm gọi Gemini API an toàn:
     * - Có Timeout (20 giây) để tránh chờ đợi vô hạn
     * - Có Try-Catch chi tiết để bắt lỗi bảo mật/mạng
     * - Trả về Result để UI dễ dàng xử lý các trạng thái
     */
    suspend fun suggestBuild(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Thiết lập timeout 20s
            val response = withTimeoutOrNull(20000L) {
                model.generateContent(prompt)
            }

            val text = response?.text
            
            if (text != null) {
                Result.success(text)
            } else if (response == null) {
                Result.failure(Exception("AI phản hồi quá chậm (Timeout). Vui lòng thử lại."))
            } else {
                Result.failure(Exception("AI không trả về nội dung. Thử lại với câu lệnh khác."))
            }
        } catch (e: Exception) {
            Log.e("AI_SAFE_CALL", "Error generating content: ${e.message}", e)
            Result.failure(mapFirebaseAiError(e))
        }
    }

    /**
     * Multi-turn chat interaction với xử lý lỗi an toàn.
     */
    suspend fun chatWithAi(history: List<ChatMessage>, message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val chatHistory = history.takeLast(10).map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }

            val chat = model.startChat(history = chatHistory)
            val response = withTimeoutOrNull(15000L) {
                chat.sendMessage(message)
            }

            val responseText = response?.text
            
            if (responseText != null) {
                Result.success(responseText)
            } else if (response == null) {
                Result.failure(Exception("Phiên chat bị quá hạn (Timeout)."))
            } else {
                Result.failure(Exception("Phản hồi từ AI trống."))
            }
        } catch (e: Exception) {
            Log.e("AI_SAFE_CALL", "Chat session error: ${e.message}", e)
            Result.failure(mapFirebaseAiError(e))
        }
    }

    /**
     * Maps Firebase AI Logic exceptions thành thông báo tiếng Việt thân thiện.
     */
    private fun mapFirebaseAiError(e: Exception): Exception {
        val errorMsg = e.message ?: ""
        return when {
            errorMsg.contains("denied", ignoreCase = true) || errorMsg.contains("403") -> {
                Exception("Truy cập bị từ chối. Vui lòng kiểm tra cấu hình App Check trong Console.")
            }
            errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("429") -> {
                Exception("Giới hạn sử dụng AI hôm nay đã hết. Vui lòng thử lại sau.")
            }
            errorMsg.contains("network", ignoreCase = true) -> {
                Exception("Lỗi kết nối mạng. Vui lòng kiểm tra Wifi hoặc dữ liệu di động.")
            }
            else -> Exception("Đã xảy ra lỗi khi kết nối với AI: ${e.localizedMessage}")
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
