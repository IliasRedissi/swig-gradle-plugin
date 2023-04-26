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

    packagingOptions {
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
    implementation(projects.sample.libGroovy)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
}