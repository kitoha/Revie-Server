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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.ReviewSession
import revie.entity.ReviewSessionEntity
import revie.enums.ReviewStatus
import revie.utils.Tsid

@Repository
class ReviewSessionRepositoryImpl(
  private val r2dbcRepository: R2dbcReviewSessionRepository,
  private val entityTemplate: R2dbcEntityTemplate
) : ReviewSessionRepository {

  override fun save(session: ReviewSession): Mono<ReviewSession> {
    val entity = ReviewSessionEntity.from(session)
    return r2dbcRepository.save(entity)
      .map { it.toDto() }
  }

  override fun findById(id: String): Mono<ReviewSession> {
    val longId = Tsid.decode(id)
    return r2dbcRepository.findById(longId)
      .map { it.toDto() }
  }

  override fun findByUserId(userId: String): Flux<ReviewSession> {
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
        .and("deleted_at").isNull()
    ).sort(
      Sort.by(Sort.Order.desc("updated_at"))
    )

    return entityTemplate
      .select(query, ReviewSessionEntity::class.java)
      .map { it.toDto() }
  }

  override fun findByUserIdAndStatus(
    userId: String,
    status: ReviewStatus
  ): Flux<ReviewSession> {
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
        .and("status").`is`(status.name)
        .and("deleted_at").isNull()
    ).sort(
      Sort.by(Sort.Order.desc("updated_at"))
    )

    return entityTemplate
      .select(query, ReviewSessionEntity::class.java)
      .map { it.toDto() }
  }

  override fun findByPullRequestUrl(pullRequestUrl: String): Mono<ReviewSession> {
    val query = Query.query(
      Criteria.where("pull_request_url").`is`(pullRequestUrl)
        .and("deleted_at").isNull()
    )

    return entityTemplate
      .selectOne(query, ReviewSessionEntity::class.java)
      .map { it.toDto() }
  }

  override fun deleteById(id: String): Mono<Void> {
    val longId = Tsid.decode(id)
    return r2dbcRepository.deleteById(longId)
  }

  override fun existsById(id: String): Mono<Boolean> {
    val longId = Tsid.decode(id)
    return r2dbcRepository.existsById(longId)
  }

  override fun findByUserIdAndPullRequestUrl(
    userId: String,
    pullRequestUrl: String
  ): Mono<ReviewSession> {
    val query = Query.query(
      Criteria.where("user_id").`is`(userId)
        .and("pull_request_url").`is`(pullRequestUrl)
        .and("deleted_at").isNull()
    )

    return entityTemplate
      .selectOne(query, ReviewSessionEntity::class.java)
      .map { it.toDto() }
  }
}