import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin")
        classpath("com.novoda:bintray-release:+")
    }
}

plugins {
    kotlin("jvm") version "1.3.10"
    id("org.jetbrains.dokka") version "0.9.17"
}

apply {
    plugin("com.novoda.bintray-release")
}

group = "tech.standalonetc"
version = "0.1.2"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.mechdancer:remote:0.1.6-dev-3")
    testCompile("junit", "junit", "+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

configure<PublishExtension> {
    userOrg = "standalonetc"
    groupId = "tech.standalonetc"
    artifactId = "protocol"
    publishVersion = version.toString()
    desc = "communication protocol for StandaloneTC."
    website = "https://github.com/StandaloneTC/protocol"
}