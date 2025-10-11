package revie.repository

import revie.dto.ConversationHistory
import revie.dto.ConversationStats

interface ConversationHistoryRepository {

  suspend fun save(history: ConversationHistory): ConversationHistory

  suspend fun findBySessionId(sessionId: String): ConversationHistory?

  suspend fun existsBySessionId(sessionId: String): Boolean

  suspend fun deleteBySessionId(sessionId: String)

  suspend fun getStatsBatch(sessionIds: List<String>): Map<String, ConversationStats>
}