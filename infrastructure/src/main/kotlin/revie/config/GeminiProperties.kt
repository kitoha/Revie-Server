package revie.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gemini")
data class GeminiProperties(
  var apiKey: String = "",
  var apiUrl: String = "https://generativelanguage.googleapis.com/v1beta",
  var model: String = "gemini-2.0-flash-exp",
  var embeddingModel: String = "text-embedding-004"
)