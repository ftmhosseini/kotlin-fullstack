// ─────────────────────────────────────────────────────────────────────────────
// SERVER — build.gradle.kts
// Spring Boot + Kotlin + H2 database
// ─────────────────────────────────────────────────────────────────────────────

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"   // makes Spring annotations work with Kotlin
    kotlin("plugin.jpa") version "1.9.25"      // makes JPA entities work with Kotlin
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")        // REST API
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")   // database ORM

    // Kotlin support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")       // JSON ↔ Kotlin data classes
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    runtimeOnly("com.h2database:h2")           // in-memory DB for development
    // runtimeOnly("org.postgresql:postgresql") // uncomment for production

    // Shared module
    implementation(project(":shared"))

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)  // match the Java version installed
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
    }
}
