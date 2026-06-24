package com.cactus.app.data.network
import kotlinx.serialization.SerialName; import kotlinx.serialization.Serializable
@Serializable data class WhisperResponse(val text: String? = null, val segments: List<WhisperSegment> = emptyList())
@Serializable data class WhisperSegment(val id: Int = 0, val start: Float = 0f, val end: Float = 0f, val text: String = "")
@Serializable data class ChatCompletionRequest(val model: String, val messages: List<ChatMessage>, val temperature: Float = 0.2f, @SerialName("max_completion_tokens") val maxCompletionTokens: Int = 8192, @SerialName("top_p") val topP: Float = 1f, val stream: Boolean = false, @SerialName("reasoning_effort") val reasoningEffort: String = "medium", val stop: String? = null)
@Serializable data class ChatMessage(val role: String, val content: String)
@Serializable data class ChatCompletionResponse(val choices: List<ChatChoice> = emptyList())
@Serializable data class ChatChoice(val message: ChatMessage? = null)
@Serializable data class TranslationItem(val id: Int, @SerialName("bn_text") val bnText: String, val pronunciation: String)
