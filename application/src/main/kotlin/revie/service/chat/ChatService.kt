package revie.service.chat

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.client.GeminiService
import revie.dto.chat.ChatMessage
import revie.dto.chat.ConversationHistory
import revie.dto.gemini.GeminiMessage
import revie.dto.review.ReviewContext
import revie.enums.MessageRole
import revie.repository.ConversationHistoryRepository
import revie.service.search.SearchService
import java.nio.charset.StandardCharsets

@Service
class ChatService (
  private val geminiService: GeminiService,
  private val conversationHistoryRepository: ConversationHistoryRepository,
  private val searchService: SearchService,
  @Value("classpath:prompts/code-review-system.md")
  private val systemPromptResource: Resource,
  @Value("classpath:prompts/code-context.md")
  private val contextPromptResource: Resource
){

  private val systemPrompt: String by lazy {
    systemPromptResource.getContentAsString(StandardCharsets.UTF_8)
  }

  private val contextPromptTemplate: String by lazy {
    contextPromptResource.getContentAsString(StandardCharsets.UTF_8)
  }


  fun chat(sessionId: String, userMessage: String): Mono<String>{
    return searchService.buildReviewContext(sessionId, userMessage)
      .flatMap { context ->
        val messages = buildMessages(context, userMessage)

        geminiService.generateContent(messages)
          .flatMap { aiResponse ->
            saveConversation(sessionId, userMessage, aiResponse)
              .thenReturn(aiResponse)
          }
      }
  }

  fun chatStream(sessionId:  String, userMessage: String): Flux<String>{
    return searchService.buildReviewContext(sessionId, userMessage)
      .flatMapMany { context ->
        val messages = buildMessages(context, userMessage)

        val responseBuilder = StringBuilder()

        geminiService.generateContentStream(messages)
          .doOnNext { chunk ->
            responseBuilder.append(chunk)
          }
          .doOnComplete {
            val aiResponse = responseBuilder.toString()
            saveConversation(sessionId, userMessage, aiResponse).subscribe()
          }
      }
  }

  fun simpleChat(sessionId: String, userMessage: String): Mono<String>{
    return geminiService.generateContent(userMessage)
      .flatMap { aiResponse ->
        saveConversation(sessionId, userMessage, aiResponse)
          .thenReturn(aiResponse)
      }
  }

  fun getConversationHistory(sessionId: String): Mono<ConversationHistory> {
    return conversationHistoryRepository.findBySessionId(sessionId)
      .switchIfEmpty(
        Mono.just(
          ConversationHistory(
            sessionId = sessionId,
            messages = emptyList(),
            createdAt = null,
            updatedAt = null
          )
        )
      )
  }

  fun clearConversationHistory(sessionId: String): Mono<Void> {
    return conversationHistoryRepository.deleteBySessionId(sessionId)
  }

  private fun buildMessages(
    context: ReviewContext,
    userMessage: String
  ): List<GeminiMessage> {
    val messages = mutableListOf<GeminiMessage>()

    messages.add(GeminiMessage.user(systemPrompt))

    if (context.hasMessages) {
      context.recentMessages.takeLast(5).forEach { msg ->
        when (msg.role) {
          MessageRole.USER -> messages.add(GeminiMessage.user(msg.content))
          MessageRole.ASSISTANT -> messages.add(GeminiMessage.assistant(msg.content))
          else -> {}
        }
      }
    }

    if (context.hasDiffs) {
      val contextText = searchService.formatContextForPrompt(context)
      val contextMessage = contextPromptTemplate.replace("{{context}}", contextText)
      messages.add(GeminiMessage.user(contextMessage))
    }

    messages.add(GeminiMessage.user(userMessage))

    return messages
  }

  private fun saveConversation(
    sessionId: String,
    userMessage: String,
    aiResponse: String
  ): Mono<ConversationHistory> {
    return conversationHistoryRepository.findBySessionId(sessionId)
      .switchIfEmpty(
        Mono.just(
          ConversationHistory(
            sessionId = sessionId,
            messages = emptyList(),
            createdAt = null,
            updatedAt = null
          )
        )
      )
      .flatMap { history ->
        val updatedMessages = history.messages + listOf(
          ChatMessage.userMessage(userMessage),
          ChatMessage.assistantMessage(aiResponse)
        )

        val updatedHistory = history.copy(messages = updatedMessages)
        conversationHistoryRepository.save(updatedHistory)
      }
  }
}