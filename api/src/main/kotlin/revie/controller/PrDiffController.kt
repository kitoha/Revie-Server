package revie.revie.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import revie.dto.file.PrFileDiff
import revie.revie.resonse.ApiResponse
import revie.service.embedding.EmbeddingService
import revie.service.file.PrDiffService

@RestController
@RequestMapping("/api/diffs")
class PrDiffController(
  private val prDiffService: PrDiffService,
  private val embeddingService: EmbeddingService
) {

  @GetMapping("/{sessionId}")
  fun getDiffs(@PathVariable sessionId: String): Mono<ApiResponse<List<PrFileDiff>>>{
    return prDiffService.getDiffs(sessionId).collectList()
      .map { diffs ->
        ApiResponse.success(data = diffs)
      }
  }

  @GetMapping("/{sessionId}/files")
  fun getDiffByFile(
    @PathVariable sessionId: String,
    @PathVariable filePath: String
  ): Mono<ApiResponse<PrFileDiff>>{
    return prDiffService.getDiffByFile(sessionId, filePath)
      .map { diff ->
        ApiResponse.success(data = diff)
      }
  }

  @GetMapping("/{sessionId}/search")
  fun searchSimilarDiffs(@PathVariable sessionId: String,
    @RequestParam query: String,
    @RequestParam(defaultValue = "5") limit: Int
  ): Mono<List<PrFileDiff>> {
    return embeddingService.createQueryEmbedding(query)
      .flatMapMany { embedding ->
        prDiffService.searchSimilarDiffs(sessionId, embedding, limit)
      }.collectList()
  }
}