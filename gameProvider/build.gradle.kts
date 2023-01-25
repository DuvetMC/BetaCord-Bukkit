import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "de.olivermakesco"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://maven.quiltmc.org/repository/snapshot/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly("org.quiltmc:quilt-loader:0.18.1-beta.58")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
