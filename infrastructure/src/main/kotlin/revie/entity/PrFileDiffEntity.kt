package revie.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import revie.dto.file.PrFileDiff
import revie.utils.Tsid

@Table("pr_file_diffs")
class PrFileDiffEntity(
  @Id
  val id: Long,

  @Column("session_id")
  val sessionId: Long,

  @Column("file_path")
  val filePath: String,

  @Column("diff_content")
  val diffContent: ByteArray,

  @Column("content_hash")
  val contentHash: String,

  @Column("is_compressed")
  val isCompressed: Boolean = true,

  @Column("embedding")
  val embedding: String? = null,

) : BaseR2dbcEntity() {
  fun toDto(decompressedContent: String): PrFileDiff {
    return PrFileDiff(
      id = Tsid.encode(id),
      sessionId = Tsid.encode(sessionId),
      filePath = filePath,
      diffContent = decompressedContent,
      contentHash = contentHash,
      embedding = embedding?.let { parseEmbedding(it) },
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }

  companion object {

    fun from(dto: PrFileDiff, compressedContent: ByteArray): PrFileDiffEntity {
      return PrFileDiffEntity(
        id = Tsid.decode(dto.id),
        sessionId = Tsid.decode(dto.sessionId),
        filePath = dto.filePath,
        diffContent = compressedContent,
        contentHash = dto.contentHash,
        isCompressed = true,
        embedding = dto.embedding?.let { formatEmbedding(it) }
      )
    }

    private fun formatEmbedding(embedding: List<Float>): String {
      return "[${embedding.joinToString(",")}]"
    }

    private fun parseEmbedding(embeddingStr: String): List<Float> {
      return embeddingStr
        .removePrefix("[")
        .removeSuffix("]")
        .split(",")
        .map { it.trim().toFloat() }
    }
  }
}