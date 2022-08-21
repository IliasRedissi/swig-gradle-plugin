@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.10.0"
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "com.redissi.plugin"
version = "0.1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.android.tools.build:gradle:7.2.2")
    compileOnly("com.android.tools.build:gradle-api:7.2.2")
}

gradlePlugin {
    // Define the plugin
    @Suppress("UNUSED_VARIABLE")
    val swigPlugin by plugins.creating {
        id = "com.redissi.swig.plugin"
        implementationClass = "com.redissi.swig.plugin.SwigPlugin"
    }
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.6"
        languageVersion = "1.6"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "swig-plugin"

            from(components["java"])
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}