package revie.revie.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import revie.dto.github.GitHubPullRequest
import revie.dto.github.GitHubPullRequestDiff
import revie.repository.GitHubClient
import revie.revie.resonse.ApiResponse
import revie.service.file.PrDiffService

@RestController
@RequestMapping("/api/github")
class GitHubController(
  private val gitHubClient: GitHubClient,
  private val prDiffService: PrDiffService
) {

  @GetMapping("/pr")
  fun getPullRequest(
    @RequestParam url: String
  ): Mono<ApiResponse<GitHubPullRequest>> {
    val (owner, repo, number) = gitHubClient.parsePullRequestUrl(url)

    return gitHubClient.getPullRequest(owner, repo, number)
      .map { pr ->
        ApiResponse.success(
          data = pr,
          message = "PR 정보를 가져왔습니다: ${pr.title}"
        )
      }
  }

  @GetMapping("/pr/diff")
  fun getPullRequestDiff(
    @RequestParam url: String
  ): Mono<ApiResponse<GitHubPullRequestDiff>> {
    return gitHubClient.getPullRequestDiffByUrl(url)
      .map { prDiff ->
        ApiResponse.success(
          data = prDiff,
          message = "${prDiff.files.size}개의 파일 변경 사항을 가져왔습니다"
        )
      }
  }

  @PostMapping("/pr/import")
  fun importPullRequest(
    @RequestParam sessionId: String,
    @RequestParam pullRequestUrl: String
  ): Mono<ApiResponse<String>> {
    return gitHubClient.getPullRequestDiffByUrl(pullRequestUrl)
      .flatMapMany { prDiff ->
        val fileMap = prDiff.toFileMap()
        prDiffService.saveDiffs(sessionId, fileMap)
      }
      .collectList()
      .map { diffs ->
        ApiResponse.success(
          "${diffs.size}개 파일의 Diff가 저장되었습니다",
          "저장 완료"
        )
      }
  }
}