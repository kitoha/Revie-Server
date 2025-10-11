plugins {
  kotlin("jvm")
}

dependencies {
  api("io.projectreactor:reactor-core")

  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

  api("jakarta.validation:jakarta.validation-api:3.0.2")
  api("io.hypersistence:hypersistence-tsid:2.1.1")

  testImplementation("io.projectreactor:reactor-test:3.5.11")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
