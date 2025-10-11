package revie.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import revie.dto.chat.ConversationHistory

@Document("conversation_history")
class ConversationHistoryDocument(
  @Id
  val sessionId: String,
  val messages: List<ChatMessageDocument>
) : BaseMongoDocument() {
  fun toDto() = ConversationHistory(
    sessionId = sessionId,
    messages = messages.map { it.toDto() },
    createdAt = createdAt,
    updatedAt = updatedAt
  )

  companion object{
    fun from(dto: ConversationHistory) = ConversationHistoryDocument(
      sessionId = dto.sessionId,
      messages = dto.messages.map { ChatMessageDocument.from(it) }
    )
  }
}