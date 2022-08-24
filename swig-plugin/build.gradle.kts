@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.10.0"
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    signing
}

group = "com.redissi.plugin"
version = "0.3.0-SNAPSHOT"

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
    plugins {
        create("swigPlugin") {
            id = "com.redissi.swig.plugin"
            implementationClass = "com.redissi.swig.plugin.SwigPlugin"
        }
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

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("swigPluginPluginMarkerMaven") {
                pom {
                    name.set("SWIG Gradle Plugin")
                    description.set("Gradle plugin to integrate SWIG generated sources in an Android project")
                    url.set("https://github.com/IliasRedissi/swig-gradle-plugin")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://spdx.org/licenses/MIT.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("iliasredissi")
                            name.set("Ilias Redissi")
                            url.set("https://github.com/IliasRedissi")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/IliasRedissi/swig-gradle-plugin.git")
                        developerConnection.set("scm:git:ssh://git@github.com:IliasRedissi/swig-gradle-plugin.git")
                        url.set("https://github.com/IliasRedissi/swig-gradle-plugin")
                    }
                }
            }
            named<MavenPublication>("pluginMaven") {
                pom {
                    name.set("SWIG Gradle Plugin")
                    description.set("Gradle plugin to integrate SWIG generated sources in an Android project")
                    url.set("https://github.com/IliasRedissi/swig-gradle-plugin")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://spdx.org/licenses/MIT.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("iliasredissi")
                            name.set("Ilias Redissi")
                            url.set("https://github.com/IliasRedissi")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/IliasRedissi/swig-gradle-plugin.git")
                        developerConnection.set("scm:git:ssh://git@github.com:IliasRedissi/swig-gradle-plugin.git")
                        url.set("https://github.com/IliasRedissi/swig-gradle-plugin")
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}