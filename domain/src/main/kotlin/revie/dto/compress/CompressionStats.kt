package revie.dto.compress

data class CompressionStats(
  val fileCount: Int,
  val totalOriginalSize: Int,
  val totalCompressedSize: Int,
  val compressionRatio: Double
) {
  val savedBytes: Int
    get() = totalOriginalSize - totalCompressedSize

  val compressionPercentage: Double
    get() = compressionRatio * 100
}