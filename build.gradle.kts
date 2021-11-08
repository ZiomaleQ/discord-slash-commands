import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "me.tfo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "1.6.5"

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}