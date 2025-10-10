package revie.dto

data class ConversationHistory(
  val id: String,
  val sessionId: String,
  val messages: List<ChatMessage>,
)