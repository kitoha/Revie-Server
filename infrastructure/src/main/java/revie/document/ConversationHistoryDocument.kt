package revie.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("conversation_history")
class ConversationHistoryDocument(
  @Id
  val id: String,
  val sessionId: String,
  val messages: List<ChatMessageDocument>
){
  fun toDto() = revie.dto.ConversationHistory(
    id = id,
    sessionId = sessionId,
    messages = messages.map { it.toDto() }
  )

  companion object{
    fun from(dto: revie.dto.ConversationHistory) = ConversationHistoryDocument(
      id = dto.id,
      sessionId = dto.sessionId,
      messages = dto.messages.map { ChatMessageDocument.from(it) }
    )
  }
}