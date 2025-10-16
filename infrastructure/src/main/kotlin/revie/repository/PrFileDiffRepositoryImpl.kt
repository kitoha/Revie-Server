package revie.repository

import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.file.PrFileDiff
import revie.entity.PrFileDiffEntity
import revie.service.compress.CompressionService
import revie.utils.Tsid

@Repository
class PrFileDiffRepositoryImpl(
  private val r2dbcRepository: R2dbcPrFileDiffRepository,
  private val entityTemplate: R2dbcEntityTemplate,
  private val compressionService: CompressionService,
  private val vectorRepository: PrFileDiffVectorRepository
) : PrFileDiffRepository{

  override fun save(
    diff: PrFileDiff
  ): Mono<PrFileDiff> {
    val compressed = compressionService.compress(diff.diffContent)
    val entity = PrFileDiffEntity.from(diff, compressed)

    return r2dbcRepository.save(entity)
      .doOnNext { it.markNotNew() }
      .map { savedEntity ->
        savedEntity.toDto(diff.diffContent)
      }
  }

  override fun findById(id: String): Mono<PrFileDiff> {
    val longId = Tsid.decode(id)
    return r2dbcRepository.findById(longId)
      .map { entity ->
        val decompressed = compressionService.decompress(entity.diffContent)
        entity.toDto(decompressed)
      }
  }

  override fun findBySessionId(sessionId: String): Flux<PrFileDiff> {
    val criteria: CriteriaDefinition = Criteria.where("session_id").`is`(Tsid.decode(sessionId))
      .and("deleted_at").isNull()

    val query = Query.query(criteria)
      .sort(Sort.by(Sort.Order.asc("file_path")))

    return entityTemplate
      .select(query, PrFileDiffEntity::class.java)
      .map { entity ->
        val decompressed = compressionService.decompress(entity.diffContent)
        entity.toDto(decompressed)
      }
  }

  override fun findBySessionIdAndFilePath(
    sessionId: String,
    filePath: String
  ): Mono<PrFileDiff> {
    val criteria = Criteria.where("session_id").`is`(Tsid.decode(sessionId))
      .and("file_path").`is`(filePath)
      .and("deleted_at").isNull()

    val query = Query.query(criteria)

    return entityTemplate
      .selectOne(query, PrFileDiffEntity::class.java)
      .map { entity ->
        val decompressed = compressionService.decompress(entity.diffContent)
        entity.toDto(decompressed)
      }
  }

  override fun countBySessionId(sessionId: String): Mono<Long> {
    val criteria = Criteria.where("session_id").`is`(Tsid.decode(sessionId))
      .and("deleted_at").isNull()

    val query = Query.query(criteria)

    return entityTemplate.count(query, PrFileDiffEntity::class.java)
  }

  override fun deleteBySessionId(sessionId: String): Mono<Void> {
    val criteria = Criteria.where("session_id").`is`(Tsid.decode(sessionId))
    val query = Query.query(criteria)

    return entityTemplate
      .delete(query, PrFileDiffEntity::class.java)
      .then()
  }

  override fun findSimilarDiffs(
    sessionId: String,
    queryEmbedding: List<Float>,
    limit: Int
  ): Flux<PrFileDiff> {
    return vectorRepository.findSimilarByEmbedding(sessionId, queryEmbedding, limit)
  }

  override fun existsEmbeddingBySessionId(sessionId: String): Mono<Boolean> {
    val criteria = Criteria.where("session_id").`is`(Tsid.decode(sessionId))
      .and("embedding").isNotNull()
      .and("deleted_at").isNull()

    val query = Query.query(criteria)

    return entityTemplate
      .exists(query, PrFileDiffEntity::class.java)
  }
}