package revie.dto.review

import revie.enums.ReviewStatus
import java.time.LocalDateTime

data class ReviewSession(
  val id: String,
  val userId: String,
  val pullRequestUrl: String,
  val title: String,
  val status: ReviewStatus,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
){

  companion object{
  fun create(sessionId: String, userId: String, pullRequestUrl: String,
    title: String): ReviewSession{
    return ReviewSession(
      id = sessionId,
      userId = userId,
      pullRequestUrl = pullRequestUrl,
      title = title,
      status = ReviewStatus.NEW,
      createdAt = null,
      updatedAt = null
    )
  }
    }

  fun updateStatus(newStatus: ReviewStatus): ReviewSession {
    return this.copy(
      status = newStatus
    )
  }
}
