plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
}

val mockitoAgent = configurations.create("mockitoAgent")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

group = "com.github.mbayou"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.19.0")

    mockitoAgent("org.mockito:mockito-core:5.19.0") { isTransitive = false }
}

tasks.test {
    jvmArgs = listOf("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
}
