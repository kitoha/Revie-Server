package revie.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import revie.document.ConversationHistoryDocument

interface ReactiveMongoConversationHistoryRepository :
  ReactiveMongoRepository<ConversationHistoryDocument, String> {

  fun findBySessionId(sessionId: String): Mono<ConversationHistoryDocument>

  fun existsBySessionId(sessionId: String): Mono<Boolean>

  fun deleteBySessionId(sessionId: String): Mono<Void>
}