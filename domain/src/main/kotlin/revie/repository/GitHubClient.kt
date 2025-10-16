package revie.repository

import reactor.core.publisher.Mono
import revie.dto.github.GitHubPullRequest
import revie.dto.github.GitHubPullRequestDiff

interface GitHubClient {

  fun parsePullRequestUrl(url: String): Triple<String, String, Int>
  fun getPullRequest(owner: String, repo: String, number: Int): Mono<GitHubPullRequest>
  fun getPullRequestDiff(owner: String, repo: String, number: Int): Mono<GitHubPullRequestDiff>
  fun getPullRequestDiffByUrl(pullRequestUrl: String): Mono<GitHubPullRequestDiff>
  fun getPullRequestWithDiff(owner: String, repo: String, number: Int):
      Mono<Pair<GitHubPullRequest, GitHubPullRequestDiff>>
}