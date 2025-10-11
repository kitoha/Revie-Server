package revie.repository

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import revie.dto.file.PrFileDiff
import revie.entity.PrFileDiffEntity
import revie.service.compress.CompressionService
import revie.utils.Tsid
import java.lang.Boolean
import java.lang.Long

@Repository
class PrFileDiffVectorRepository(
  private val databaseClient: DatabaseClient,
  private val compressionService: CompressionService
) {
  fun findSimilarByEmbedding(
    sessionId: String,
    queryEmbedding: List<Float>,
    limit: Int = 5
  ): Flux<PrFileDiff> {
    val embeddingStr = formatEmbedding(queryEmbedding)
    val sessionLongId = Tsid.decode(sessionId)

    val sql = """
      SELECT 
        id, session_id, file_path, diff_content, content_hash, 
        is_compressed, embedding, created_at, updated_at, deleted_at,
        1 - (embedding <=> :embedding::vector) as similarity
      FROM pr_file_diffs
      WHERE session_id = :sessionId
        AND deleted_at IS NULL
        AND embedding IS NOT NULL
      ORDER BY embedding <=> :embedding::vector
      LIMIT :limit
    """.trimIndent()

    return databaseClient
      .sql(sql)
      .bind("embedding", embeddingStr)
      .bind("sessionId", sessionLongId)
      .bind("limit", limit)
      .map { row, _ ->
        mapRowToEntity(row)
      }
      .all()
      .map { entity ->
        val decompressed = compressionService.decompress(entity.diffContent)
        entity.toDto(decompressed)
      }
  }

  private fun formatEmbedding(embedding: List<Float>): String {
    return "[${embedding.joinToString(",")}]"
  }

  private fun mapRowToEntity(row: io.r2dbc.spi.Row): PrFileDiffEntity {
    return PrFileDiffEntity(
      id = row.get("id", Long::class.java)!!.toLong(),
      sessionId = row.get("session_id", Long::class.java)!!.toLong(),
      filePath = row.get("file_path", String::class.java)!!,
      diffContent = row.get("diff_content", ByteArray::class.java)!!,
      contentHash = row.get("content_hash", String::class.java)!!,
      isCompressed = (row.get("is_compressed", Boolean::class.java) ?: true) as kotlin.Boolean,
      embedding = row.get("embedding", String::class.java)
    ).apply {
      createdAt = row.get("created_at", java.time.LocalDateTime::class.java)
      updatedAt = row.get("updated_at", java.time.LocalDateTime::class.java)
    }
  }
}