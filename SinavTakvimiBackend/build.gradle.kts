repositories {
    mavenCentral() // Bu satır eksikse kütüphaneleri bulamaz
}
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

val ktor_version="2.3.11"
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)

    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktor_version}")
    implementation("io.ktor:ktor-server-resources-jvm:${ktor_version}")
    implementation ("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
// Routing için seçtiğin eklenti
    // Exposed (SQL Server ile konuşmak için ORM)
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    // MS SQL Driver
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.2.0.jre11")
    implementation("ch.qos.logback:logback-classic:1.4.14")


    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
}
