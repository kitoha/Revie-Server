package revie.dto.github

data class GitHubDiffFile(
  val filename: String,
  val status: String,
  val additions: Int,
  val deletions: Int,
  val changes: Int,
  val patch: String?
)