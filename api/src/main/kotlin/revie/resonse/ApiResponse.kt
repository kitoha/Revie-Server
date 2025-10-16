package revie.revie.resonse

import java.time.LocalDateTime

data class ApiResponse<T>(
  val success: Boolean,
  val data: T? = null,
  val message: String? = null,
  val timestamp: LocalDateTime = LocalDateTime.now()
) {
  companion object {
    fun <T> success(data: T): ApiResponse<T> {
      return ApiResponse(
        success = true,
        data = data
      )
    }

    fun <T> success(data: T, message: String): ApiResponse<T> {
      return ApiResponse(
        success = true,
        data = data,
        message = message
      )
    }

    fun <T> error(message: String): ApiResponse<T> {
      return ApiResponse(
        success = false,
        message = message
      )
    }
  }
}