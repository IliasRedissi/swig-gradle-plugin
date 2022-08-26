plugins {
    id("com.android.application") version "7.2.2"
    id("org.jetbrains.kotlin.android") version "1.7.10"
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        jniLibs {
            pickFirsts += "lib/**/libc++_shared.so"
            pickFirsts += "lib/**/libsample.so"
        }
    }
}

dependencies {
    implementation(project(":sample:lib-groovy"))
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}