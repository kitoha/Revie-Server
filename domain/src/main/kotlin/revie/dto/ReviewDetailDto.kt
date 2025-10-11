package revie.dto

import revie.enums.ReviewStatus
import java.time.LocalDateTime

data class ReviewDetailDto(
  val sessionId: String,
  val userId: String,
  val title: String,
  val pullRequestUrl: String,
  val status: ReviewStatus,
  val messages: List<ChatMessage>,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
) {

  companion object{
    fun create(
      sessionId: String,
      userId: String,
      title: String,
      pullRequestUrl: String,
      status: ReviewStatus,
      messages: List<ChatMessage>,
      createdAt: LocalDateTime?,
      updatedAt: LocalDateTime?
    ): ReviewDetailDto{
      return ReviewDetailDto(
        sessionId = sessionId,
        userId = userId,
        title = title,
        pullRequestUrl = pullRequestUrl,
        status = status,
        messages = messages,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
    }
  }
}