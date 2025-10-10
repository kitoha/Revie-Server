package revie.repository

import revie.dto.ReviewSession
import revie.enums.ReviewStatus

interface ReviewSessionRepository {

  suspend fun save(session: ReviewSession): ReviewSession

  suspend fun findById(id: String): ReviewSession?

  suspend fun findByUserId(userId: String): List<ReviewSession>

  suspend fun findByUserIdAndStatus(userId: String, status: ReviewStatus): List<ReviewSession>

  suspend fun findByPullRequestUrl(pullRequestUrl: String): ReviewSession?

  suspend fun deleteById(id: String)

  suspend fun existsById(id: String): Boolean
}