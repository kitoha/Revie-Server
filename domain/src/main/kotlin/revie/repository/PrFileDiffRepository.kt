package revie.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.file.PrFileDiff

interface PrFileDiffRepository {
  fun save(diff: PrFileDiff): Mono<PrFileDiff>

  fun findById(id: String): Mono<PrFileDiff>

  fun findBySessionId(sessionId: String): Flux<PrFileDiff>

  fun findBySessionIdAndFilePath(sessionId: String, filePath: String): Mono<PrFileDiff>

  fun countBySessionId(sessionId: String): Mono<Long>

  fun deleteBySessionId(sessionId: String): Mono<Void>

  fun findSimilarDiffs(
    sessionId: String,
    queryEmbedding: List<Float>,
    limit: Int = 5
  ): Flux<PrFileDiff>

  fun existsEmbeddingBySessionId(sessionId: String): Mono<Boolean>
}