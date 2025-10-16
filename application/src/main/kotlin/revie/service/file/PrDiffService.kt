package revie.service.file

import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.compress.CompressionStats
import revie.dto.file.PrFileDiff
import revie.repository.PrFileDiffRepository
import revie.service.compress.CompressionService
import revie.utils.Tsid
import java.security.MessageDigest

@Service
class PrDiffService(
  private val prFileDiffRepository: PrFileDiffRepository,
  private val compressionService: CompressionService,
) {

  fun saveDiff(
    sessionId: String,
    filePath: String,
    diffContent: String,
    embedding: List<Float>? = null
  ): Mono<PrFileDiff> {
    val contentHash = calculateHash(diffContent)
    val compressed = compressionService.compress(diffContent)

    val diff = PrFileDiff(
      id = Tsid.generate(),
      sessionId = sessionId,
      filePath = filePath,
      diffContent = diffContent,
      contentHash = contentHash,
      embedding = embedding,
      createdAt = null,
      updatedAt = null
    )

    return prFileDiffRepository.save(diff)
  }

  fun saveDiffs(
    sessionId: String,
    fileDiffs: Map<String, String>
  ): Flux<PrFileDiff> {
    return Flux.fromIterable(fileDiffs.entries)
      .flatMap { (filePath, diffContent) ->
        saveDiff(sessionId, filePath, diffContent)
      }
  }

  fun getDiffs(sessionId: String): Flux<PrFileDiff> {
    return prFileDiffRepository.findBySessionId(sessionId)
  }

  fun getDiffByFile(sessionId: String, filePath: String): Mono<PrFileDiff> {
    return prFileDiffRepository.findBySessionIdAndFilePath(sessionId, filePath)
      .switchIfEmpty(
        Mono.error(IllegalArgumentException("Diff를 찾을 수 없습니다: $filePath"))
      )
  }

  fun countDiffs(sessionId: String): Mono<Long> {
    return prFileDiffRepository.countBySessionId(sessionId)
  }

  fun searchSimilarDiffs(
    sessionId: String,
    queryEmbedding: List<Float>,
    limit: Int = 5
  ): Flux<PrFileDiff> {
    return prFileDiffRepository.findSimilarDiffs(sessionId, queryEmbedding, limit)
  }

  fun updateEmbedding(
    diffId: String,
    embedding: List<Float>
  ): Mono<PrFileDiff> {
    return prFileDiffRepository.findById(diffId)
      .switchIfEmpty(
        Mono.error(IllegalArgumentException("Diff를 찾을 수 없습니다: $diffId"))
      )
      .flatMap { diff ->
        val updated = diff.copy(embedding = embedding)
        val compressed = compressionService.compress(diff.diffContent)
        prFileDiffRepository.save(updated)
      }
  }

  fun deleteDiffs(sessionId: String): Mono<Void> {
    return prFileDiffRepository.deleteBySessionId(sessionId)
  }

  fun getCompressionStats(sessionId: String): Mono<CompressionStats> {
    return prFileDiffRepository.findBySessionId(sessionId)
      .collectList()
      .map { diffs ->
        val totalOriginal = diffs.sumOf { it.diffContent.length }
        val totalCompressed = diffs.sumOf { diff ->
          compressionService.compress(diff.diffContent).size
        }
        val ratio = if (totalOriginal > 0) {
          1.0 - (totalCompressed.toDouble() / totalOriginal.toDouble())
        } else 0.0

        CompressionStats(
          fileCount = diffs.size,
          totalOriginalSize = totalOriginal,
          totalCompressedSize = totalCompressed,
          compressionRatio = ratio
        )
      }
  }

  private fun calculateHash(content: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(content.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
  }
}