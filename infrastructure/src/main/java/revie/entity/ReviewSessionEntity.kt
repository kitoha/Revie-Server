package revie.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import revie.dto.ReviewSession

@Table("review_sessions")
class ReviewSessionEntity (
  @Id
  val id: String,
  val userId: String,
  val pullRequestUrl: String,
  val title: String,
  val status: String,
){

  fun toDto() = ReviewSession(
    id = id,
    userId = userId,
    pullRequestUrl = pullRequestUrl,
    title = title,
    status = revie.enums.ReviewStatus.valueOf(status)
  )

  companion object{
    fun from(dto: ReviewSession) = ReviewSessionEntity(
      id = dto.id,
      userId = dto.userId,
      pullRequestUrl = dto.pullRequestUrl,
      title = dto.title,
      status = dto.status.name
    )
  }
}