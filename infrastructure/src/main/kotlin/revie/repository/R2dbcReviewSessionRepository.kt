package revie.repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import revie.entity.ReviewSessionEntity

interface R2dbcReviewSessionRepository : ReactiveCrudRepository<ReviewSessionEntity, Long>