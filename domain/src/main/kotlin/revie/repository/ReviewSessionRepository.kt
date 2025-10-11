package revie.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.ReviewSession
import revie.enums.ReviewStatus

interface ReviewSessionRepository {

  fun save(session: ReviewSession): Mono<ReviewSession>

  fun findById(id: String): Mono<ReviewSession>

  fun findByUserId(userId: String): Flux<ReviewSession>

  fun findByUserIdAndStatus(userId: String, status: ReviewStatus): Flux<ReviewSession>

  fun findByPullRequestUrl(pullRequestUrl: String): Mono<ReviewSession>

  fun deleteById(id: String): Mono<Void>

  fun existsById(id: String): Mono<Boolean>

  fun findByUserIdAndPullRequestUrl(userId: String, pullRequestUrl: String): Mono<ReviewSession>
}