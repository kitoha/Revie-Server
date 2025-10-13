package revie.client

import com.google.genai.Client
import com.google.genai.types.EmbedContentConfig
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GenerationConfig
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import revie.config.GeminiProperties
import revie.dto.gemini.GeminiMessage
import kotlin.jvm.optionals.getOrNull

@Component
class GeminiClient(
  private val client: Client,
  private val geminiProperties: GeminiProperties
) {

  private fun createConfig(): GenerateContentConfig {
    return GenerateContentConfig.builder()
      .temperature(geminiProperties.temperature)
      .maxOutputTokens(geminiProperties.maxOutputTokens)
      .build()
  }

  private fun createEmbedConfig(): EmbedContentConfig {
    return EmbedContentConfig.builder()
      .taskType("SEMANTIC_SIMILARITY")
      .build()
  }

  fun generateContentStream(message: List<GeminiMessage>): Flux<String>{
    return Flux.create { sink ->
      try {
        val prompt = buildPrompt(message)

        val stream = client.models.generateContentStream(
          geminiProperties.model,
          prompt,
          createConfig()
        )

        stream.forEach { response ->
          response.text()?.let { text ->
            sink.next(text)
          }
        }

        sink.complete()
      } catch (e: Exception) {
        sink.error(RuntimeException("Gemini streaming failed: ${e.message}", e))
      }
    }
  }

  fun generateContent(message: List<GeminiMessage>): Mono<String>{
    return Mono.create { sink ->
      try {
        val prompt = buildPrompt(message)

        val response = client.models.generateContent(
          geminiProperties.model,
          prompt,
          createConfig()
        )

        response.text()?.let { text ->
          sink.success(text)
        } ?: sink.error(RuntimeException("Gemini response text is null"))
      } catch (e: Exception) {
        sink.error(RuntimeException("Gemini request failed: ${e.message}", e))
      }
    }
  }

  fun generateContent(prompt: String): Mono<String>{
    return Mono.fromCallable {
      try{
        val response = client.models.generateContent(
          geminiProperties.model,
          prompt,
          createConfig()
        )
        response.text().orEmpty()
      } catch (e: Exception) {
        throw RuntimeException("Gemini request failed: ${e.message}", e)
      }
    }
  }

  fun createEmbedding(text: String): Mono<List<Float>> {
    return Mono.fromCallable {
      try {
        val response = client.models.embedContent(
          geminiProperties.embeddingModel,
          text,
          createEmbedConfig()
        )

        response.embeddings()
          .orElse(emptyList())
          .firstOrNull()
          ?.values()
          ?.orElse(emptyList())
          ?: emptyList()
      } catch (e: Exception) {
        throw RuntimeException("Embedding generation failed: ${e.message}", e)
      }
    }
  }

  fun createEmbeddingBatch(texts: List<String>): Flux<List<Float>> {
    return Flux.fromIterable(texts)
      .flatMap { text ->
        createEmbedding(text)
      }
  }

  private fun buildPrompt(messages: List<GeminiMessage>): String {
    return messages.joinToString(separator = "\n\n"){
      when(it.role.lowercase()){
        "user" -> "User: ${it.text}"
        "model" -> "Model: ${it.text}"
        else -> it.text
      }
    }
  }

}