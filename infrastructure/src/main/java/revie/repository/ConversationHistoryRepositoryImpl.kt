package revie.repository

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import revie.document.ConversationHistoryDocument
import revie.dto.ConversationHistory
import revie.dto.ConversationStats
import java.time.LocalDateTime

@Repository
class ConversationHistoryRepositoryImpl(
  private val mongoRepository: ReactiveMongoConversationHistoryRepository,
  private val mongoTemplate: ReactiveMongoTemplate,
) : ConversationHistoryRepository {

  override suspend fun save(history: ConversationHistory): ConversationHistory {
    val document = ConversationHistoryDocument.from(history)
    val saved = mongoRepository.save(document).awaitFirstOrNull()
      ?: throw IllegalStateException("Failed to save conversation history")
    return saved.toDto()
  }

  override suspend fun findBySessionId(sessionId: String): ConversationHistory? {
    return mongoRepository.findBySessionId(sessionId)
      .awaitFirstOrNull()?.toDto()
  }

  override suspend fun existsBySessionId(sessionId: String): Boolean {
    return mongoRepository.existsBySessionId(sessionId).awaitFirstOrNull() ?: false
  }

  override suspend fun deleteBySessionId(sessionId: String) {
    mongoRepository.deleteBySessionId(sessionId).awaitFirstOrNull()
  }

  override suspend fun getStatsBatch(sessionIds: List<String>): Map<String, ConversationStats> {
    if(sessionIds.isEmpty()) return emptyMap()

    val aggregation = Aggregation.newAggregation(
      Aggregation.match(Criteria.where("sessionId").`in`(sessionIds)),
      Aggregation.project()
        .and("sessionId").`as`("sessionId")
        .and("messages").size().`as`("messageCount")
        .and("messages").arrayElementAt(-1).`as`("lastMessage")
    )

    val results = mongoTemplate
      .aggregate(aggregation, ConversationHistoryDocument::class.java, AggregationResult::class.java)
      .collectList()
      .awaitFirstOrNull() ?: emptyList()

    return results.associate { result ->
      result.sessionId to ConversationStats(
        sessionId = result.sessionId,
        messageCount = result.messageCount,
        lastMessageContent = result.lastMessage?.content?.take(100),
        lastMessageTimestamp = result.lastMessage?.timestamp
      )
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