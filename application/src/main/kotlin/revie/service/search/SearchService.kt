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

  fun formatContextForPrompt(context: ReviewContext): String {
    val sb = StringBuilder()

    if(context.recentMessages.isNotEmpty()){
      sb.appendLine("## 최근 대화 (${context.recentMessages.size}개)")
      context.recentMessages.takeLast(10).forEach { message ->
        val prefix = when(message.role) {
          revie.enums.MessageRole.USER -> "User"
          revie.enums.MessageRole.ASSISTANT -> "Assistant"
          revie.enums.MessageRole.SYSTEM -> "System"
        }
        sb.appendLine("$prefix: ${message.content}")
      }
      sb.appendLine()
    }

    if(context.similarDiffs.isNotEmpty()){
      sb.appendLine("## 관련 코드 변경사항 (${context.similarDiffs.size}개)")
      context.similarDiffs.forEach { diff ->
        sb.appendLine("### 변경사항 ${diff.filePath}")
        sb.appendLine("```diff")
        sb.appendLine(diff.diffContent.take(500))
        if(diff.diffContent.length > 500){
          sb.appendLine("... (생략됨)")
        }
        sb.appendLine("```")
        sb.appendLine()
      }
    }

    return sb.toString().trim()
  }

}