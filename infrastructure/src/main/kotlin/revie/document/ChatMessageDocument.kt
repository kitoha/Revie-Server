package revie.document

import revie.dto.chat.ChatMessage
import revie.enums.MessageRole
import java.time.LocalDateTime

data class ChatMessageDocument(
  val role: String,
  val content: String,
  val timestamp: LocalDateTime,
  val metadata: Map<String, String>? = null
) {

  fun toDto() = ChatMessage(
    role = MessageRole.valueOf(role),
    content = content,
    timestamp = timestamp,
    metadata = metadata
  )

  companion object {
    fun from(dto: ChatMessage) = ChatMessageDocument(
      role = dto.role.name,
      content = dto.content,
      timestamp = dto.timestamp,
      metadata = dto.metadata
    )
  }
}