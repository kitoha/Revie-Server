import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.0" apply false
	id("io.spring.dependency-management") version "1.1.4" apply false
	kotlin("jvm") version "1.9.21" apply false
	kotlin("plugin.spring") version "1.9.21" apply false
}

allprojects {
	group = "com.revie"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "io.spring.dependency-management")

	dependencies {
		val implementation by configurations
		val testImplementation by configurations

		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("org.jetbrains.kotlin:kotlin-stdlib")

		testImplementation("org.junit.jupiter:junit-jupiter")
		testImplementation("io.mockk:mockk:1.13.8")
		testImplementation("org.jetbrains.kotlin:kotlin-test")
	}

	tasks.withType<JavaCompile> {
		sourceCompatibility = "21"
		targetCompatibility = "21"
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs += "-Xjsr305=strict"
			jvmTarget = "21"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}