package revie.repository

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import revie.dto.ReviewSession
import revie.entity.ReviewSessionEntity
import revie.enums.ReviewStatus
import revie.utils.Tsid

@Repository
class ReviewSessionRepositoryImpl(
  private val r2dbcRepository: R2dbcReviewSessionRepository,
  private val entityTemplate: R2dbcEntityTemplate
) : ReviewSessionRepository {

  override suspend fun save(session: ReviewSession): ReviewSession {
    val entity = ReviewSessionEntity.from(session)
    val savedEntity = r2dbcRepository.save(entity)
    return savedEntity.toDto()
  }

  override suspend fun findById(id: String): ReviewSession? {
    val longId  = Tsid.decode(id)
    return r2dbcRepository.findById(longId )?.toDto()
  }

  override suspend fun findByUserId(userId: String): List<ReviewSession> {
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
    ).sort(
      Sort.by(
        Sort.Order.desc("updated_at")
      )
    )

    return entityTemplate
      .select(query, ReviewSessionEntity::class.java)
      .asFlow()
      .map { it.toDto() }
      .toList()
  }

  override suspend fun findByUserIdAndStatus(
    userId: String,
    status: ReviewStatus
  ): List<ReviewSession> {
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
        .and("status").`is`(status.name)
    ).sort(
      Sort.by(
        Sort.Order.desc("updated_at")
      )
    )

    return entityTemplate
      .select(query, ReviewSessionEntity::class.java)
      .asFlow()
      .map { it.toDto() }
      .toList()
  }

  override suspend fun findByPullRequestUrl(pullRequestUrl: String): ReviewSession? {
    val query = Query.query(
      Criteria.where("pull_request_url").`is`(pullRequestUrl)
    )

    return entityTemplate
      .selectOne(query, ReviewSessionEntity::class.java)
      .awaitFirstOrNull()?.toDto()
  }

  override suspend fun deleteById(id: String) {
    val longId  = Tsid.decode(id)
    r2dbcRepository.deleteById(longId )
  }

  override suspend fun existsById(id: String): Boolean {
    val longId  = Tsid.decode(id)
    return r2dbcRepository.existsById(longId)
  }

  override suspend fun findByUserIdAndPullRequestUrl(
    userId: String,
    pullRequestUrl: String
  ): ReviewSession?{
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
        .and("pull_request_url").`is`(pullRequestUrl)
    )

    return entityTemplate
      .selectOne(query, ReviewSessionEntity::class.java)
      .awaitFirstOrNull()
      ?.toDto()
  }
}