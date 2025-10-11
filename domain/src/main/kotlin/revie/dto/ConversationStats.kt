package revie.dto

import java.time.LocalDateTime

data class ConversationStats(
  val sessionId: String,
  val messageCount: Int,
  val lastMessageContent: String?,
  val lastMessageTimestamp: LocalDateTime?
)