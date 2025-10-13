package revie.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import revie.dto.github.GitHubDiffFile
import revie.dto.github.GitHubPullRequest
import revie.dto.github.GitHubPullRequestDiff

@Component
class GitHubClient(
  @Qualifier("gitHubWebClient") private val webClient: WebClient
) {

  fun parsePullRequestUrl(url: String): Triple<String, String, Int> {
    val regex = Regex("github\\.com/([^/]+)/([^/]+)/pull/(\\d+)")
    val matchResult = regex.find(url)
      ?: throw IllegalArgumentException("Invalid GitHub PR URL: $url")

    val (owner, repo, number) = matchResult.destructured
    return Triple(owner, repo, number.toInt())
  }

  fun getPullRequest(owner: String, repo: String, number: Int): Mono<GitHubPullRequest> {
    return webClient.get()
      .uri("/repos/$owner/$repo/pulls/$number")
      .retrieve()
      .bodyToMono<Map<String, Any>>()
      .map { response ->
        GitHubPullRequest(
          number = response["number"] as Int,
          title = response["title"] as String,
          description = response["body"] as? String,
          author = (response["user"] as? Map<*, *>)?.get("login") as? String ?: "unknown",
          baseBranch = (response["base"] as? Map<*, *>)?.get("ref") as? String ?: "main",
          headBranch = (response["head"] as? Map<*, *>)?.get("ref") as? String ?: "unknown",
          state = response["state"] as? String ?: "open",
          diffUrl = response["diff_url"] as? String ?: ""
        )
      }
  }

  fun getPullRequestDiff(owner: String, repo: String, number: Int): Mono<GitHubPullRequestDiff> {
    return webClient.get()
      .uri("/repos/$owner/$repo/pulls/$number/files")
      .retrieve()
      .bodyToMono<List<Map<String, Any>>>()
      .map { files ->
        GitHubPullRequestDiff(
          files = files.map { file ->
            GitHubDiffFile(
              filename = file["filename"] as String,
              status = file["status"] as String,
              additions = file["additions"] as Int,
              deletions = file["deletions"] as Int,
              changes = file["changes"] as Int,
              patch = file["patch"] as? String
            )
          }
        )
      }
  }

  fun getPullRequestDiffByUrl(pullRequestUrl: String): Mono<GitHubPullRequestDiff> {
    val (owner, repo, number) = parsePullRequestUrl(pullRequestUrl)
    return getPullRequestDiff(owner, repo, number)
  }

  fun getPullRequestWithDiff(owner: String, repo: String, number: Int):
      Mono<Pair<GitHubPullRequest, GitHubPullRequestDiff>> {
    return Mono.zip(
      getPullRequest(owner, repo, number),
      getPullRequestDiff(owner, repo, number)
    ).map { tuple-> Pair(tuple.t1, tuple.t2) }
  }

}