package revie.repository

import reactor.core.publisher.Mono
import revie.dto.chat.ConversationHistory
import revie.dto.chat.ConversationStats

interface ConversationHistoryRepository {

  fun save(history: ConversationHistory): Mono<ConversationHistory>

  fun findBySessionId(sessionId: String): Mono<ConversationHistory>

  fun existsBySessionId(sessionId: String): Mono<Boolean>

  fun deleteBySessionId(sessionId: String): Mono<Void>

  fun getStatsBatch(sessionIds: List<String>): Mono<Map<String, ConversationStats>>
}