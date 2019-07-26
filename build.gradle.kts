import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
    id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
    id("org.jetbrains.dokka") version "0.9.18"
    java
    maven
}

group = "com.github.kuro46"
version = "0.3.0-SNAPSHOT"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("org.bukkit", "bukkit", "1.12.2-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.arrow-kt", "arrow-core-data", "0.9.0")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.5.0")
}

ktlint {
    enableExperimentalRules.set(true)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}
