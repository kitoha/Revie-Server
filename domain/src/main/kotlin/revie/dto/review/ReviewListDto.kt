package revie.dto.review

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
){
  companion object{
    fun create(
      sessionId: String,
      title: String,
      pullRequestUrl: String,
      status: ReviewStatus,
      messageCount: Int,
      lastMessage: String?,
      createdAt: LocalDateTime?,
      updatedAt: LocalDateTime?
    ): ReviewListDto{
      return ReviewListDto(
        sessionId = sessionId,
        title = title,
        pullRequestUrl = pullRequestUrl,
        status = status,
        messageCount = messageCount,
        lastMessage = lastMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
    }
  }
}