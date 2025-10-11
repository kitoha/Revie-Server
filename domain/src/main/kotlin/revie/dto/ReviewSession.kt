package revie.dto

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

  fun updateStatus(newStatus: ReviewStatus): ReviewSession {
    return this.copy(
      status = newStatus
    )
  }
}
