import com.novoda.gradle.release.PublishExtension
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
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
version = "0.2.3"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.mechdancer:remote:0.2.1-dev-11")
    compile ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
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
