package revie.repository

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import revie.document.ConversationHistoryDocument
import revie.dto.chat.ConversationHistory
import revie.dto.chat.ConversationStats
import java.time.LocalDateTime

@Repository
class ConversationHistoryRepositoryImpl(
  private val mongoRepository: ReactiveMongoConversationHistoryRepository,
  private val mongoTemplate: ReactiveMongoTemplate,
) : ConversationHistoryRepository {

  override fun save(history: ConversationHistory): Mono<ConversationHistory> {
    val document = ConversationHistoryDocument.from(history)
    return mongoRepository.save(document)
      .map { it.toDto() }
  }

  override fun findBySessionId(sessionId: String): Mono<ConversationHistory> {
    return mongoRepository.findBySessionId(sessionId)
      .map { it.toDto() }
  }

  override fun existsBySessionId(sessionId: String): Mono<Boolean> {
    return mongoRepository.existsBySessionId(sessionId)
  }

  override fun deleteBySessionId(sessionId: String): Mono<Void> {
    return mongoRepository.deleteBySessionId(sessionId)
  }

  override fun getStatsBatch(sessionIds: List<String>): Mono<Map<String, ConversationStats>> {
    if (sessionIds.isEmpty()) return Mono.just(emptyMap())

    val aggregation = Aggregation.newAggregation(
      Aggregation.match(Criteria.where("sessionId").`in`(sessionIds)),
      Aggregation.project()
        .and("sessionId").`as`("sessionId")
        .and("messages").size().`as`("messageCount")
        .and("messages").arrayElementAt(-1).`as`("lastMessage")
    )

    return mongoTemplate
      .aggregate(
        aggregation,
        ConversationHistoryDocument::class.java,
        AggregationResult::class.java
      )
      .collectList()
      .map { results ->
        results.associate { result ->
          result.sessionId to ConversationStats(
            sessionId = result.sessionId,
            messageCount = result.messageCount,
            lastMessageContent = result.lastMessage?.content?.take(100),
            lastMessageTimestamp = result.lastMessage?.timestamp
          )
        }
      }
  }

  private data class AggregationResult(
    val sessionId: String,
    val messageCount: Int,
    val lastMessage: LastMessageDto?
  )

  private data class LastMessageDto(
    val content: String,
    val timestamp: LocalDateTime
  )
}