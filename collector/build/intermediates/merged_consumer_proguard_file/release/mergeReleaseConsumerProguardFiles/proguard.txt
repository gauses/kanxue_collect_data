# ==== 采集 SDK 接入方混淆规则（随 aar 自动合并到宿主 App 的 ProGuard 配置）====

# ---- JNI / native ----
# native 方法名与其所在类名必须与 C/C++ 侧注册的符号（Java_包名_类名_方法名）保持一致，禁止混淆
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
# 显式保留承载 native 方法的类（含被 JNI 反向调用的成员）
-keep class com.test.ndk.Testor { *; }
-keep class com.nest.kanxue.devicefingerprint.DrmIdFetcher { *; }
-keep class com.nest.kanxue.core.DisplayCard { *; }
-keep class com.nest.kanxue.NativeLib { *; }
-keep class com.nest.kanxue.StatFs64 { *; }

# ---- Gson 序列化的数据模型（字段名即 JSON key，混淆会破坏字段名）----
-keep class com.test.ndk.SensorInfo { *; }
-keep class com.nest.kanxue.FileStat { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- SDK 对外公开 API ----
-keep class com.nest.kanxue.sdk.DeviceCollector { *; }
-keep interface com.nest.kanxue.sdk.DeviceCollector$Callback { *; }

# ---- 上传相关（Retrofit/Gson 反射）----
-keep class com.nest.kanxue.http.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
