package revie.dto

import revie.enums.ReviewStatus
import java.time.LocalDateTime

data class ReviewListDto(
  val sessionId: String,
  val title: String,
  val pullRequestUrl: String,
  val status: ReviewStatus,
  val messageCount: Int,
  val lastMessage: String?,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
)