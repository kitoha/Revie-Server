package revie.dto.chat

import java.time.LocalDateTime

data class ConversationHistory(
  val sessionId: String,
  val messages: List<ChatMessage>,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
){

  companion object{
    fun create(sessionId: String, messages: List<ChatMessage>): ConversationHistory{
      return ConversationHistory(
        sessionId = sessionId,
        messages = messages,
        createdAt = null,
        updatedAt = null
      )
    }
  }
}