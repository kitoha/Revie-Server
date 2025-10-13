package revie.dto.review

import revie.dto.chat.ChatMessage
import revie.dto.file.PrFileDiff

data class ReviewContext(
  val recentMessages: List<ChatMessage>,
  val similarDiffs: List<PrFileDiff>
) {
  val hasMessages: Boolean
    get() = recentMessages.isNotEmpty()

  val hasDiffs: Boolean
    get() = similarDiffs.isNotEmpty()

  val isEmpty: Boolean
    get() = !hasMessages && !hasDiffs
}