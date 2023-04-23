pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "swig-gradle-plugin"
includeBuild("swig-plugin")

include(":sample:app")
include(":sample:lib")
include(":sample:lib-groovy")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
