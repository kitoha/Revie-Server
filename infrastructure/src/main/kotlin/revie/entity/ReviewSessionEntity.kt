package revie.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
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
) : BaseR2dbcEntity(), Persistable<Long> {
  @Transient
  private var isNew: Boolean = true

  fun toDto() = ReviewSession(
    id = Tsid.encode(id),
    userId = userId,
    pullRequestUrl = pullRequestUrl,
    title = title,
    status = ReviewStatus.valueOf(status),
    createdAt = createdAt,
    updatedAt = updatedAt
  )

  override fun getId(): Long = id

  override fun isNew(): Boolean = isNew

  fun markNotNew() {
    this.isNew = false
  }

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