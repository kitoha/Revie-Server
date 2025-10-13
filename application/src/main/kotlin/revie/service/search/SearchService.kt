package revie.service.search

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import revie.dto.chat.ChatMessage
import revie.dto.review.ReviewContext
import revie.repository.ConversationHistoryRepository
import revie.repository.PrFileDiffRepository
import revie.service.embedding.EmbeddingService

@Service
class SearchService (
  private val conversationHistoryRepository: ConversationHistoryRepository,
  private val prFileDiffRepository: PrFileDiffRepository,
  private val embeddingService: EmbeddingService
){

  fun buildReviewContext(sessionId: String,
    userQuery: String,): Mono<ReviewContext>{
    val recentMessages = getRecentMessages(sessionId, limit = 20)

    val similarDiffs = embeddingService.createQueryEmbedding(userQuery)
      .flatMapMany { queryEmbedding ->
        prFileDiffRepository.findSimilarDiffs(
          sessionId = sessionId,
          queryEmbedding = queryEmbedding,
          limit = 5
        )
      }
      .collectList()

    return Mono.zip(recentMessages, similarDiffs)
      .map { tuple ->
        ReviewContext(
          recentMessages = tuple.t1,
          similarDiffs = tuple.t2
        )
      }
  }

  private fun getRecentMessages(sessionId: String, limit: Int): Mono<List<ChatMessage>> {
    return conversationHistoryRepository.findBySessionId(sessionId)
      .map { history ->
        history.messages.takeLast(limit)
      }
      .defaultIfEmpty(emptyList())
  }

}