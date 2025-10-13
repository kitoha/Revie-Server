package revie.config

import com.google.genai.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig(
  private val geminiProperties: GeminiProperties
) {

  @Bean
  fun geminiClient(): Client{
    require(geminiProperties.apiKey.isNotBlank()) {
      "Gemini API key must be provided. Please set GEMINI_API_KEY environment variable."
    }

    return Client.builder()
      .apiKey(geminiProperties.apiKey)
      .build()
  }
}