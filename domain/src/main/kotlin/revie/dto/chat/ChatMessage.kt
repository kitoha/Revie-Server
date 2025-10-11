package revie.dto.chat

import revie.enums.MessageRole
import java.time.LocalDateTime

data class ChatMessage(
  val role: MessageRole,
  val content: String,
  val timestamp: LocalDateTime = LocalDateTime.now(),
  val metadata: Map<String, String>? = null
) {
  companion object{
    fun userMessage(content: String) = ChatMessage(MessageRole.USER, content)
    fun assistantMessage(content: String) = ChatMessage(MessageRole.ASSISTANT, content)
    fun systemMessage(content: String) = ChatMessage(MessageRole.SYSTEM, content)
  }
}