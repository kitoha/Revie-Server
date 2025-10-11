package revie.dto.file

import java.time.LocalDateTime

data class PrFileDiff(
  val id: String,
  val sessionId: String,
  val filePath: String,
  val diffContent: String,
  val contentHash: String,
  val embedding: List<Float>? = null,
  val createdAt: LocalDateTime? = null,
  val updatedAt: LocalDateTime? = null
) {

  val fileExtension: String
    get() = filePath.substringAfterLast('.', "")

  val fileName: String
    get() = filePath.substringAfterLast('/')

  val directoryPath: String
    get() = filePath.substringBeforeLast('/', "")
}