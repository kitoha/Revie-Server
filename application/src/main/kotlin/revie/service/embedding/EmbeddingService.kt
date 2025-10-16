package revie.service.embedding

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.client.GeminiService
import revie.repository.PrFileDiffRepository

@Service
class EmbeddingService (
  private val geminiService: GeminiService,
  private val prFileDiffRepository: PrFileDiffRepository
){

  fun createEmbedding(text: String): Mono<List<Float>>{
    return geminiService.createEmbedding(text)
  }

  fun generateEmbeddingForSession(sessionId: String): Flux<String>{
    return prFileDiffRepository.findBySessionId(sessionId)
      .filter { it.embedding == null }
      .flatMap { diff ->
        geminiService.createEmbedding(diff.diffContent)
          .flatMap { embedding ->
            val updated = diff.copy(embedding = embedding)
            prFileDiffRepository.save(updated)
          }
          .map { "✓ ${it.filePath}" }
          .onErrorResume { error ->
            Mono.just("✗ ${diff.filePath}: ${error.message}")
          }
      }
  }

  fun generateEmbeddingForDiff(diffId: String): Mono<List<Float>>{
    return prFileDiffRepository.findById(diffId)
      .flatMap { diff ->
        geminiService.createEmbedding(diff.diffContent)
          .flatMap { embedding ->
            val updated = diff.copy(embedding = embedding)
            prFileDiffRepository.save(updated)
              .map { embedding }
          }
      }
  }

  fun createQueryEmbedding(query: String): Mono<List<Float>>{
    return geminiService.createEmbedding(query)
  }

  fun hasEmbeddings(sessionId: String): Mono<Boolean>{
    return prFileDiffRepository.existsEmbeddingBySessionId(sessionId)
  }
}