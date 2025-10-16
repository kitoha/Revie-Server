package revie.revie.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import revie.dto.review.ReviewListDto
import revie.dto.review.ReviewSession
import revie.enums.ReviewStatus
import revie.revie.request.CreateReviewRequest
import revie.service.review.ReviewService

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
  private val reviewService: ReviewService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createReview(
    @RequestHeader("X-User-Id") userId: String,
    @Valid @RequestBody request: CreateReviewRequest
  ) : Mono<ReviewSession>{
    return reviewService.createReview(
      userId = userId,
      pullRequestUrl = request.pullRequestUrl
    )
  }

  @GetMapping
  fun getReviewList(
    @RequestHeader("X-User-Id") userId: String
  ): Mono<List<ReviewListDto>> {
    return reviewService.getReviewList(userId)
  }

  @PostMapping("/{sessionId}/status")
  fun updateReviewStatus(
    @PathVariable sessionId: String,
    @RequestParam status: ReviewStatus
  ): Mono<ReviewSession> {
    return reviewService.updateReviewStatus(sessionId, status)
  }
}