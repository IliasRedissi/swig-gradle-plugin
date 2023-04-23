plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.redissi.swig.sample"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        jniLibs {
            pickFirsts += "lib/**/libc++_shared.so"
            pickFirsts += "lib/**/libsample.so"
        }
    }
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(project(":sample:lib-groovy"))
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}