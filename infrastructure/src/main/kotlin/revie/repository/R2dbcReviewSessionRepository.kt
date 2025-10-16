package revie.repository

import org.springframework.data.r2dbc.repository.R2dbcRepository
import revie.entity.ReviewSessionEntity

interface R2dbcReviewSessionRepository : R2dbcRepository<ReviewSessionEntity, Long>