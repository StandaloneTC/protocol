import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin")
    }
}

plugins {
    kotlin("jvm") version "1.3.10"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "tech.standalonetc"
version = "0.1.0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.mechdancer:remote:0.1.6-dev-2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}