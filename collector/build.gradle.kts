plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nest.collector"
    compileSdk = 34

    defaultConfig {
        minSdk = 31

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    ndkVersion = "21.4.7075529"
}

dependencies {
    // 用 api 暴露，宿主 App 依赖 :collector 时可传递获得
    api(libs.androidx.core.ktx)
    api("androidx.fragment:fragment-ktx:1.6.2") // 透明 Fragment 代申请运行时权限
    api(libs.play.services.ads.identifier)

    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.squareup.okhttp3:logging-interceptor:4.9.3")
    api("com.google.code.gson:gson:2.8.9")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    api("org.apache.commons:commons-compress:1.26.1")
}
