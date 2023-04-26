@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    `maven-publish`
    alias(libs.plugins.gradle.nexus.publish)
    signing
}

group = "com.redissi.plugin"
version = "0.4.0-SNAPSHOT"

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.android.gradle.api)
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
    jvmToolchain(11)
    explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        apiVersion = "1.8"
        languageVersion = "1.8"
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
    this.repositories {
        sonatype()
    }
}