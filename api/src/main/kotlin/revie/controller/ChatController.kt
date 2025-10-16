package revie.revie.controller

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.dto.chat.ConversationHistory
import revie.revie.request.ChatRequest
import revie.revie.resonse.ApiResponse
import revie.service.chat.ChatService

@RestController
@RequestMapping("/api/chat")
class ChatController (
  private val chatService: ChatService
){

  @PostMapping("/{sessionId}")
  fun chat(@PathVariable sessionId: String,
    @Valid @RequestBody request: ChatRequest
  ): Mono<ApiResponse<String>>{
    return chatService.chat(sessionId, request.message)
      .map { aiResponse ->
        ApiResponse.success(
          data = aiResponse,
          message = "AI 응답을 가져왔습니다."
        )
      }
  }

  @PostMapping(value = ["/{sessionId}/stream"],
    produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  fun chatStream(@PathVariable sessionId: String,
    @Valid @RequestBody request: ChatRequest): Flux<ServerSentEvent<String>>{
    return chatService.chatStream(sessionId, request.message)
      .map { chunk ->
        ServerSentEvent.builder<String>()
          .data(chunk)
          .build()
      }
      .concatWith(
        Mono.just(
          ServerSentEvent.builder<String>()
            .event("complete")
            .data("[DONE]")
            .build()
        )
      )
  }

  @GetMapping("/{sessionId}/history")
  fun getHistory(@PathVariable sessionId: String): Mono<ApiResponse<ConversationHistory>>{
    return chatService.getConversationHistory(sessionId)
      .map { history ->
        ApiResponse.success(history)
      }
  }

  @DeleteMapping("/{sessionId}/history")
  fun clearHistory(@PathVariable sessionId: String): Mono<ApiResponse<String>>{
    return chatService.clearConversationHistory(sessionId)
      .then(Mono.just(ApiResponse.success("대화 히스토리가 삭제되었습니다")))
  }

  @PostMapping("/simple/{sessionId}")
  fun simpleChat(
    @PathVariable sessionId: String,
    @Valid @RequestBody request: ChatRequest
  ): Mono<ApiResponse<String>> {
    return chatService.simpleChat(sessionId, request.message)
      .map { response  ->
        ApiResponse.success(response)
      }
  }
}