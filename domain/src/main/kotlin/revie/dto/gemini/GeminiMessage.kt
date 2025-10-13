package revie.dto.gemini

data class GeminiMessage(
  val role: String,
  val text: String
) {
  fun toRequestFormat(): Map<String, Any>{
    return mapOf(
      "role" to role,
      "parts" to listOf(
        mapOf("text" to text)
      ))
  }

  companion object{
    fun user(text: String) = GeminiMessage("user", text)
    fun model(text: String) = GeminiMessage("model", text)
    fun assistant(text: String) = GeminiMessage("assistant", text)
  }
}