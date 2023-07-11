plugins {
    alias(libs.plugins.android.library)
    id("com.redissi.swig")
}

android {
    namespace = "com.redissi.swig.sample.lib"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared", "-DCOMPANY_YEAR=TRUE")
            }
        }
    }

    swig {
        javaWrapper {
            create("SampleWrapper") {
                packageName = "com.redissi.sample"
                interfaceFile = file("src/main/swig/Sample.i")
                sourceFolders = files("src/main/cpp/include")
                extraArguments("-fvirtual")
                cppProcessing = false
            }
        }
    }

    ndkVersion = "21.4.7075529"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.18.1"
        }
    }

    buildFeatures {
        prefabPublishing = true
    }

    prefab {
        create("sample") {
            headers = "src/main/cpp/include"
            headerOnly = true
        }
    }
}
