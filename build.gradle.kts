import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("io.gitlab.arturbosch.detekt") version "1.0.1"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
    id("org.jetbrains.dokka") version "0.9.18"
//    id("com.github.spotbugs") version "2.0.0"
    checkstyle
    `java-library`
    maven
}

group = "com.github.kuro46"
version = "0.4.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.bukkit", "bukkit", "1.12.2-R0.1-SNAPSHOT")
    api(kotlin("stdlib-jdk8"))
    api("io.arrow-kt", "arrow-core-data", "0.9.0")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.5.1")
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

// SpotBugs settings

// tasks.withType<com.github.spotbugs.SpotBugsTask> {
//     sourceSets {
//         getByName("main").java.srcDirs("src/main/java")
//         getByName("test").java.srcDirs("src/test/java")
//     }
//
//     reports.getXml().setEnabled(false)
//     reports.getHtml().setEnabled(true)
// }
