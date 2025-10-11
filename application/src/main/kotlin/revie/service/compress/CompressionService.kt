package revie.service.compress

import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Service
class CompressionService {

  fun compress(content: String): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()

    GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
      gzipOutputStream.write(content.toByteArray(Charsets.UTF_8))
    }

    return byteArrayOutputStream.toByteArray()
  }

  fun decompress(compressedData: ByteArray): String{
    val byteArrayInputStream = ByteArrayInputStream(compressedData)
    val byteArrayOutputStream = ByteArrayOutputStream()

    GZIPInputStream(byteArrayInputStream).use { gzipInputStream ->
      val buffer = ByteArray(1024)
      var len: Int
      while (gzipInputStream.read(buffer).also { len = it } != -1) {
        byteArrayOutputStream.write(buffer, 0, len)
      }
    }

      return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name())
  }

  fun calculateCompressionRatio(original: String, compressed: ByteArray): Double {
    val originalSize = original.toByteArray(StandardCharsets.UTF_8).size
    val compressedSize = compressed.size

    return if (originalSize == 0) 0.0
    else 1.0 - (compressedSize.toDouble() / originalSize.toDouble())
  }

  fun formatSize(bytes: ByteArray): String {
    val size = bytes.size
    return when {
      size < 1024 -> "$size B"
      size < 1024 * 1024 -> String.format("%.2f KB", size / 1024.0)
      else -> String.format("%.2f MB", size / (1024.0 * 1024.0))
    }
  }
}