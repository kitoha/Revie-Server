package revie.dto.github

data class GitHubPullRequest(
  val number: Int,
  val title: String,
  val description: String?,
  val author: String,
  val baseBranch: String,
  val headBranch: String,
  val state: String,
  val diffUrl: String
)