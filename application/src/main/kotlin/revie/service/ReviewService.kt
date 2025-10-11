package revie.service

import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import revie.dto.ConversationHistory
import revie.dto.ReviewListDto
import revie.dto.ReviewSession
import revie.enums.ReviewStatus
import revie.repository.ConversationHistoryRepository
import revie.repository.ReviewSessionRepository
import revie.utils.Tsid

@Service
class ReviewService (
  private val reviewSessionRepository: ReviewSessionRepository,
  private val conversationHistoryRepository: ConversationHistoryRepository,
  private val transactionalOperator: TransactionalOperator
){

  /**
   * 새 리뷰 세션 생성
   */
  suspend fun createReview(
    userId: String,
    pullRequestUrl: String,
    title: String
  ) : ReviewSession{
    val existing = reviewSessionRepository.findByUserIdAndPullRequestUrl(userId, pullRequestUrl)
    if (existing != null) {
      throw IllegalArgumentException("이미 리뷰 중인 Pull Request입니다: $pullRequestUrl")
    }

    val sessionId = Tsid.generate()
    val session = ReviewSession(
      id = sessionId,
      userId = userId,
      pullRequestUrl = pullRequestUrl,
      title = title,
      status = ReviewStatus.NEW,
      createdAt = null,
      updatedAt = null
    )

    return transactionalOperator.executeAndAwait {
      val savedSession = reviewSessionRepository.save(session)

      val history = ConversationHistory(
        sessionId = sessionId,
        messages = emptyList(),
        createdAt = null,
        updatedAt = null
      )
      conversationHistoryRepository.save(history)
      savedSession
    }
  }

  suspend fun getReviewList(userId: String): List<ReviewListDto> {
    val sessions = reviewSessionRepository.findByUserId(userId)

    if(sessions.isEmpty()){
      return emptyList()
    }

    val sessionIds = sessions.map { it.id }
    val stats = conversationHistoryRepository.getStatsBatch(sessionIds)

    return sessions.map{ session ->
      val stat = stats[session.id]
      ReviewListDto(
        sessionId = session.id,
        title = session.title,
        pullRequestUrl = session.pullRequestUrl,
        status = session.status,
        messageCount = stat?.messageCount ?: 0,
        lastMessage = stat?.lastMessageContent,
        createdAt = session.createdAt,
        updatedAt = session.updatedAt
      )
    }
  }

  suspend fun updateReviewStatus(
    sessionId: String,
    status: ReviewStatus
  ): ReviewSession {
    val session = reviewSessionRepository.findById(sessionId)
      ?: throw IllegalArgumentException("존재하지 않는 리뷰 세션입니다: $sessionId")

    val updatedSession = session.updateStatus(status)
    return reviewSessionRepository.save(updatedSession)
  }

}