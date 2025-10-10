package revie.dto

data class ConversationStats(
  val sessionId: String,
  val messageCount: Int,
  val lastMessageContent: String?,
  val lastMessageTimestamp: java.time.LocalDateTime?
)