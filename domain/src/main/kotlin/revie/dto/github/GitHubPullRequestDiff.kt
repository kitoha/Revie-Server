package revie.dto.github

data class GitHubPullRequestDiff(
  val files: List<GitHubDiffFile>
){
  val totalAdditions: Int
    get() = files.sumOf { it.additions }

  val totalDeletions: Int
    get() = files.sumOf { it.deletions }

  val totalChanges: Int
    get() = files.sumOf { it.changes }

  fun toFileMap(): Map<String, String> {
    return files
      .filter { it.patch != null }
      .associate { it.filename to it.patch!! }
  }
}