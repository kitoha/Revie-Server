package revie.service

import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
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
  fun createReview(
    userId: String,
    pullRequestUrl: String,
    title: String
  ): Mono<ReviewSession> {
    return reviewSessionRepository.findByUserIdAndPullRequestUrl(userId, pullRequestUrl)
      .flatMap<ReviewSession> {
        Mono.error(IllegalArgumentException("이미 리뷰 중인 Pull Request입니다: $pullRequestUrl"))
      }
      .switchIfEmpty(
        Mono.defer {
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

          transactionalOperator.execute { _ ->
            reviewSessionRepository.save(session)
              .flatMap { savedSession ->
                val history = ConversationHistory(
                  sessionId = sessionId,
                  messages = emptyList(),
                  createdAt = null,
                  updatedAt = null
                )
                conversationHistoryRepository.save(history)
                  .thenReturn(savedSession)
              }
          }.next()
        }
      )
  }

  /**
   * 리뷰 목록 조회
   */
  fun getReviewList(userId: String): Mono<List<ReviewListDto>> {
    return reviewSessionRepository.findByUserId(userId)
      .collectList()
      .flatMap { sessions ->
        if (sessions.isEmpty()) {
          Mono.just(emptyList())
        } else {
          val sessionIds = sessions.map { it.id }
          conversationHistoryRepository.getStatsBatch(sessionIds)
            .map { stats ->
              sessions.map { session ->
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
        }
      }
  }

  /**
   * 리뷰 상태 업데이트
   */
  fun updateReviewStatus(
    sessionId: String,
    status: ReviewStatus
  ): Mono<ReviewSession> {
    return reviewSessionRepository.findById(sessionId)
      .switchIfEmpty(
        Mono.error(IllegalArgumentException("존재하지 않는 리뷰 세션입니다: $sessionId"))
      )
      .map { it.updateStatus(status) }
      .flatMap { updatedSession ->
        reviewSessionRepository.save(updatedSession)
      }
  }
}