package revie.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import revie.entity.ReviewSessionEntity

interface R2dbcReviewSessionRepository : CoroutineCrudRepository<ReviewSessionEntity, Long>