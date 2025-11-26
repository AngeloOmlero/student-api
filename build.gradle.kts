plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"

    id("org.flywaydb.flyway") version "10.17.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
    // new implementations
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    implementation("org.apache.commons:commons-lang3:3.18.0") // latest safe version
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation ("org.springframework.boot:spring-boot-starter-websocket")


    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    jvmArgs("-Xshare:off")
}
