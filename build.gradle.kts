import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
}

group = "de.olivermakesco"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://maven.quiltmc.org/repository/snapshot/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.proxyfox.dev/")
}

dependencies {
    compileOnly(files("./craftbukkit_BETA_1.7.3.jar"))
    implementation("dev.kord:kord-core:0.8.0-M16")
    shadow("dev.kord:kord-core:0.8.0-M16")
    implementation("dev.proxyfox:proxyfox-command:1.7")
    shadow("dev.proxyfox:proxyfox-command:1.7")

    runtimeOnly(files("./craftbukkit_BETA_1.3.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = java.targetCompatibility.toString()
}
