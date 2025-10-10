package revie.dto

import revie.enums.ReviewStatus

data class ReviewSession(
  val id: String,
  val userId: String,
  val pullRequestUrl: String,
  val title: String,
  val status: ReviewStatus
){
}
