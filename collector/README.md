# DeviceCollector SDK 接入说明

设备指纹 / 硬件信息采集 SDK，打包为 Android Library（AAR），含 Kotlin/Java API、native `.so`、权限 Manifest 与 ProGuard consumer 规则。

## 产物

| 文件 | 说明 |
|------|------|
| `collector-release.aar` | Gradle 默认产物（含 `jni/arm64-v8a/libndk_kanxue.so`） |
| Gradle 输出 | `collector/build/outputs/aar/collector-release.aar` |
| 一键脚本输出 | `collector/collector-release-yyyyMMdd-HHmmss.aar`（带时间戳） |

**要求**：`minSdk 31`，目前仅打包 `arm64-v8a`。

## 一键打包

详细步骤见：[PACKAGE_AAR.md](./PACKAGE_AAR.md)

在 `collector` 目录执行：

```powershell
cd d:\Project\kanxue_collect_data\collector
.\package_aar.ps1
```

脚本会自动探测/设置 `JAVA_HOME`，在仓库根目录执行 `:collector:assembleRelease`，并把 AAR 复制到当前 `collector/` 目录，文件名形如 `collector-release-20260812-113000.aar`。

手动打包：

```bat
gradlew.bat :collector:assembleRelease
```

## 接入步骤

### 1. 放入 AAR

将 `collector-release.aar` 放到宿主工程，例如 `app/libs/`。

### 2. Gradle 依赖

```kotlin
// settings.gradle.kts（若尚未启用）
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
android {
    // ...
}

dependencies {
    implementation(files("libs/collector-release.aar"))

    // AAR 虽用 api 声明了依赖，扁平 files() 接入时通常不会自动传递，建议显式补上：
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
}
```

同源工程也可直接依赖模块：

```kotlin
implementation(project(":collector"))
```

### 3. 权限

**两层分工：**

| 层级 | 谁负责 |
|------|--------|
| Manifest 声明 | AAR 已带，自动合并进宿主 |
| 运行时弹窗 | **SDK 负责**：传入 `Activity` 时由透明 Fragment 申请 |

推荐直接传 `Activity`（`AppCompatActivity` / `FragmentActivity`）：

```kotlin
DeviceCollector.collectAndUpload(activity = this, callback = ...)
```

也可只申请不采集：`DeviceCollector.requestPermissions(this) { denied -> }`

若宿主已自行申请完权限，可传 `Context`：`collectAndUpload(applicationContext, callback)`（不再弹窗）。

未授权项会降级跳过，不中断整体流程。特权权限（如 `DUMP`）普通 App 无法获得，SDK 会自动降级。

`files(aar)` 接入时请额外依赖：`androidx.fragment:fragment-ktx:1.6.2`。

### 4. ProGuard / R8

AAR 已带 `consumer-rules.pro`，开启混淆时会自动合并。若需手动引用，规则文件在模块根目录 `collector/consumer-rules.pro`。

## 调用示例

入口类：`com.nest.kanxue.sdk.DeviceCollector`

固定行为：**采集全部项（含传感器、屏幕、定位、CPU/温度等大文件）并上传**。无需再传配置参数。

### Kotlin

```kotlin
import com.nest.kanxue.sdk.DeviceCollector

// 推荐：SDK 内申请权限后再采集
DeviceCollector.collectAndUpload(
    activity = this,
    callback = object : DeviceCollector.Callback {
        override fun onProgress(stage: String) {
            // 切回主线程更新 UI
        }

        override fun onSuccess(zipFileName: String) {
            // 服务端 zip 文件名
        }

        override fun onError(message: String) {
            // 失败原因
        }
    }
)
```

### Java

```java
import com.nest.kanxue.sdk.DeviceCollector;

// 推荐：传入 Activity，由 SDK 申请权限
DeviceCollector.collectAndUpload(
    this,
    new DeviceCollector.Callback() {
        @Override
        public void onProgress(String stage) { }

        @Override
        public void onSuccess(String zipFileName) { }

        @Override
        public void onError(String message) { }
    }
);
```

说明：

- 传 `Activity`：SDK 先弹权限，再采集；传 `Context`：不弹权限，直接采集。
- `collectAndUpload` 采集阶段在后台线程，调用后立即返回。
- 同一时刻只允许一次采集；重复调用会走 `onError("采集正在进行中…")`。
- 回调不保证在主线程，更新 UI 请自行 `runOnUiThread` / `Handler`。

## Demo

本仓库 `:app` 为演示壳（依赖本地 AAR，不依赖 `:collector` 源码模块）：

- **仅申请权限** → `DeviceCollector.requestPermissions(activity)`
- **采集并上传** → `DeviceCollector.collectAndUpload(activity, callback)`（SDK 内先申请权限）
- 界面滚动日志展示 `onProgress` / 未授权列表 / 成功失败

安装 Debug 包后即可验证完整接入路径。
