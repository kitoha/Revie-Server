package revie.service.review

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import revie.client.GitHubClientImpl
import revie.dto.chat.ConversationHistory
import revie.dto.review.ReviewListDto
import revie.dto.review.ReviewSession
import revie.enums.ReviewStatus
import revie.repository.ConversationHistoryRepository
import revie.repository.ReviewSessionRepository
import revie.utils.Tsid

@Service
class ReviewService (
  private val reviewSessionRepository: ReviewSessionRepository,
  private val conversationHistoryRepository: ConversationHistoryRepository,
  private val transactionalOperator: TransactionalOperator,
  private val gitHubClientImpl: GitHubClientImpl
){

  private val log = LoggerFactory.getLogger(ReviewService::class.java)
  /**
   * 새 리뷰 세션 생성
   */
  fun createReview(
    userId: String,
    pullRequestUrl: String
  ): Mono<ReviewSession> {
    return reviewSessionRepository.findByUserIdAndPullRequestUrl(userId, pullRequestUrl)
      .flatMap<ReviewSession> {
        Mono.error(IllegalArgumentException("이미 리뷰 중인 Pull Request입니다: $pullRequestUrl"))
      }
      .switchIfEmpty(
        Mono.defer {
          val titleMono = getTitleFromPullRequest(pullRequestUrl)

          titleMono.flatMap { title ->
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

            reviewSessionRepository.save(session)
              .`as`(transactionalOperator::transactional)
              .doOnSuccess { log.info("✅ ReviewSession saved: ${it.id}") }
              .doOnError { log.error("❌ ReviewSession save failed", it) }
              .flatMap { savedSession ->
                val history = ConversationHistory(
                  sessionId = sessionId,
                  messages = emptyList(),
                  createdAt = null,
                  updatedAt = null
                )

                conversationHistoryRepository.save(history)
                  .doOnSuccess { log.info("✅ ConversationHistory saved") }
                  .doOnError { log.error("❌ ConversationHistory save failed", it) }
                  .thenReturn(savedSession)
                  .onErrorResume { mongoError ->
                    log.error("❌ MongoDB 저장 실패, PostgreSQL 데이터 삭제 시작", mongoError)
                    reviewSessionRepository.deleteById(savedSession.id)
                      .doOnSuccess { log.warn("🔄 보상 트랜잭션 완료: PostgreSQL 데이터 삭제됨") }
                      .doOnError { deleteError ->
                        log.error("💥 보상 트랜잭션 실패: PostgreSQL 데이터 삭제 불가", deleteError)
                      }
                      .then(Mono.error(
                        RuntimeException("리뷰 세션 생성 실패: MongoDB 저장 중 오류", mongoError)
                      ))
                  }
              }
              .doOnSuccess { log.info("✅ 리뷰 세션 생성 완료: ${it.id}") }
          }
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

  private fun getTitleFromPullRequest(pullRequestUrl: String): Mono<String> {
    val (owner, repo, number) = gitHubClientImpl.parsePullRequestUrl(pullRequestUrl)
    return gitHubClientImpl.getPullRequest(owner, repo, number)
      .map { pr -> pr.title }
      .onErrorReturn("Untitled Review")
  }
}