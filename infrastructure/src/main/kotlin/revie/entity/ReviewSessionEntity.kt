package revie.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import revie.dto.review.ReviewSession
import revie.enums.ReviewStatus
import revie.utils.Tsid

@Table("review_sessions")
class ReviewSessionEntity (
  @Id
  val id: Long,
  val userId: String,
  val pullRequestUrl: String,
  val title: String,
  val status: String,
) : BaseR2dbcEntity() {

  fun toDto() = ReviewSession(
    id = Tsid.encode(id),
    userId = userId,
    pullRequestUrl = pullRequestUrl,
    title = title,
    status = ReviewStatus.valueOf(status),
    createdAt = createdAt,
    updatedAt = updatedAt
  )

  companion object{
    fun from(dto: ReviewSession) = ReviewSessionEntity(
      id = Tsid.decode(dto.id),
      userId = dto.userId,
      pullRequestUrl = dto.pullRequestUrl,
      title = dto.title,
      status = dto.status.name
    )
  }
}