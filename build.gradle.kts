import com.novoda.gradle.release.PublishExtension
import org.jetbrains.dokka.gradle.DokkaTask
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
    kotlin("jvm") version "1.3.20"
    id("org.jetbrains.dokka") version "0.9.17"
}

apply {
    plugin("com.novoda.bintray-release")
}

group = "tech.standalonetc"
version = "0.2.4"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.mechdancer:remote:0.2.1-dev-12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    testImplementation("junit", "junit", "4.12")
    testImplementation("org.mechdancer:common-extension-log4j:v0.1.0-1")
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

task<Jar>("sourceJar") {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

task<Jar>("javadocJar") {
    classifier = "javadoc"
    from("$buildDir/javadoc")
}

tasks.withType<DokkaTask> {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

tasks["javadoc"].dependsOn("dokka")
tasks["jar"].dependsOn("sourceJar")
tasks["jar"].dependsOn("javadocJar")
