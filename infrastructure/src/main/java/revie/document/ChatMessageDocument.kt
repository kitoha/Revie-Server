package revie.document

import revie.dto.ChatMessage
import java.time.LocalDateTime

data class ChatMessageDocument(
  val role: String,
  val content: String,
  val timestamp: LocalDateTime,
  val metadata: Map<String, String>? = null
) {

  fun toDto() = ChatMessage(
    role = revie.enums.MessageRole.valueOf(role),
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