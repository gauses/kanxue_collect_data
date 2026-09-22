plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nest.kanxue_data"
    compileSdk = 34

    lint {
        baseline = file("lint-baseline.xml")
    }

    defaultConfig {
        applicationId = "com.nest.kanxue_data"
        minSdk = 31
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters  += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ===== 用本地 AAR 验证接入（不要用 project(":collector")）=====
    // files(aar) 不会传递依赖：下面「AAR 运行时」一组是 SDK 内部用的，宿主不写会运行期 ClassNotFound。
     implementation(project(":collector"))
//    implementation(files("libs/collector-release-20260812-135237.aar"))

    // AAR 运行时依赖（MainActivity 不直接用，但 DeviceCollector/上传链路会用）
    implementation(libs.play.services.ads.identifier)          // AAID
    implementation("com.squareup.retrofit2:retrofit:2.9.0")     // 上传
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.apache.commons:commons-compress:1.26.1") // 打包
    implementation("androidx.fragment:fragment-ktx:1.6.2") // SDK 权限 Fragment（files(aar) 需显式补）

    // Demo App UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}