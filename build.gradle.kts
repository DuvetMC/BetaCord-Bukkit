import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

val include by configurations.register("include")

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
    implementation("dev.kord:kord-core:0.10.0")
    include("dev.kord:kord-core:0.10.0")
    implementation("dev.proxyfox:proxyfox-command:1.8")
    include("dev.proxyfox:proxyfox-command:1.8")
    implementation("dev.proxyfox:pluralkt:1.8")
    include("dev.proxyfox:pluralkt:1.8")
    implementation("io.arrow-kt:arrow-core:1.2.0")
    include("io.arrow-kt:arrow-core:1.2.0")
    include("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.5.1")
    include("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.5.1")

    runtimeOnly(files("./craftbukkit_BETA_1.3.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = java.targetCompatibility.toString()
}

tasks.withType<Jar> {
    from(include.map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
