package com.example.myphonec

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for AI-powered build suggestions.
 * Strictly using Firebase AI Logic with Gemini Developer API.
 */
class BuildAiRepository {

    // Sử dụng model mới nhất gemini-3-flash-preview theo yêu cầu.
    // Firebase.ai mặc định sử dụng Gemini Developer API (googleAI).
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3-flash-preview")
    }

    /**
     * Suggests a build based on the provided prompt.
     * Returns a Result to allow proper error handling in the UI.
     */
    suspend fun suggestBuild(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(prompt)
            val responseText = response.text
            
            if (responseText != null) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("AI không trả về nội dung. Vui lòng thử lại."))
            }
        } catch (e: Exception) {
            Log.e("AI_REPOSITORY", "Error generating content: ${e.message}", e)
            Result.failure(handleAiException(e))
        }
    }

    /**
     * Multi-turn chat interaction with the AI.
     */
    suspend fun chatWithAi(history: List<ChatMessage>, message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val chatHistory = history.takeLast(10).map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }

            val chat = model.startChat(history = chatHistory)
            val response = chat.sendMessage(message)
            val responseText = response.text
            
            if (responseText != null) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("Phản hồi từ AI trống."))
            }
        } catch (e: Exception) {
            Log.e("AI_REPOSITORY", "Chat session error: ${e.message}", e)
            Result.failure(handleAiException(e))
        }
    }

    /**
     * Maps Firebase AI Logic exceptions to user-friendly messages.
     */
    private fun handleAiException(e: Exception): Exception {
        val errorMsg = e.message ?: ""
        return when {
            errorMsg.contains("denied", ignoreCase = true) || errorMsg.contains("403") -> {
                Exception("Truy cập bị từ chối. Vui lòng kiểm tra cấu hình Firebase AI Logic trong Console.")
            }
            errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("429") -> {
                Exception("Giới hạn yêu cầu đã hết. Vui lòng thử lại sau vài phút.")
            }
            errorMsg.contains("network", ignoreCase = true) -> {
                Exception("Lỗi kết nối mạng. Vui lòng kiểm tra wifi hoặc dữ liệu di động.")
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
