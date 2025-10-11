package revie.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import revie.entity.PrFileDiffEntity

interface R2dbcPrFileDiffRepository : ReactiveCrudRepository<PrFileDiffEntity, Long>