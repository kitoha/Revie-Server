package revie.repository

import reactor.core.publisher.Mono
import revie.dto.ConversationHistory
import revie.dto.ConversationStats

interface ConversationHistoryRepository {

  fun save(history: ConversationHistory): Mono<ConversationHistory>

  fun findBySessionId(sessionId: String): Mono<ConversationHistory>

  fun existsBySessionId(sessionId: String): Mono<Boolean>

  fun deleteBySessionId(sessionId: String): Mono<Void>

  fun getStatsBatch(sessionIds: List<String>): Mono<Map<String, ConversationStats>>
}