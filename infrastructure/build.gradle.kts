plugins {
  kotlin("jvm")
  kotlin("plugin.spring")
  id("io.spring.dependency-management")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
  }
}

dependencies {
  api(project(":domain"))

  implementation("org.springframework:spring-context")
  implementation("org.springframework:spring-tx")

  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.mariadb:r2dbc-mariadb")
  implementation("io.r2dbc:r2dbc-pool")

  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

  implementation("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
}