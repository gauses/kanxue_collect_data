package com.nest.kanxue.sdk

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.nest.kanxue.Stat_File_Utils
import com.nest.kanxue.UploadData
import com.nest.kanxue.convertToJSONObject
import com.nest.kanxue.Utils
import com.nest.kanxue.apkinstallpath.getAPKInstallPath
import com.nest.kanxue.bootid.getBootId
import com.nest.kanxue.camera.GetCameraInfo
import com.nest.kanxue.cert.CertificateReader
import CodecInfoCollector
import com.nest.kanxue.core.Batteryutils
import com.nest.kanxue.core.CheckSensorLength
import com.nest.kanxue.core.CpuFilesCopier
import com.nest.kanxue.core.DisplayCard
import com.nest.kanxue.core.GetAllBatteryInfo
import com.nest.kanxue.core.GetAllCodec
import com.nest.kanxue.core.GetAllVulkanInfo
import com.nest.kanxue.core.GetInutService
import com.nest.kanxue.core.GetServiceList
import com.nest.kanxue.core.Power_SupplyFilesCopier
import com.nest.kanxue.core.Shell_AM_GetConfig
import com.nest.kanxue.core.Shell_PM_List_Features
import com.nest.kanxue.core.Shell_lshal
import com.nest.kanxue.core.Shell_lspci
import com.nest.kanxue.core.Shell_lsusb
import com.nest.kanxue.core.ShellGetCgroup
import com.nest.kanxue.core.ShellGetCpuInfo
import com.nest.kanxue.core.ShellGetDiskstats
import com.nest.kanxue.core.ShellGetEnviron
import com.nest.kanxue.core.ShellGetKernel
import com.nest.kanxue.core.ShellGetLinuxVersion
import com.nest.kanxue.core.ShellGetMaps
import com.nest.kanxue.core.ShellGetMeminfo
import com.nest.kanxue.core.ShellGetMountinfo
import com.nest.kanxue.core.ShellGetMounts
import com.nest.kanxue.core.ShellGetMountstats
import com.nest.kanxue.core.ShellGetProp
import com.nest.kanxue.core.TempFilesCopier
import com.nest.kanxue.core.stat.ShellGetStat_All
import com.nest.kanxue.core.stat.ShellGetStat_Odm
import com.nest.kanxue.core.stat.ShellGetStat_Product
import com.nest.kanxue.core.stat.ShellGetStat_System_ext
import com.nest.kanxue.core.stat.ShellGetStat_Vendor
import com.nest.kanxue.core.stat_F.ShellGetStat_F_Data
import com.nest.kanxue.core.stat_F.ShellGetStat_F_Odm
import com.nest.kanxue.core.stat_F.ShellGetStat_F_Odm_dlkm
import com.nest.kanxue.core.stat_F.ShellGetStat_F_Product
import com.nest.kanxue.core.stat_F.ShellGetStat_F_System_ext
import com.nest.kanxue.core.stat_F.ShellGetStat_F_Vendor
import com.nest.kanxue.deviceidentification.getDeviceIdentifiers
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import com.nest.kanxue.devicefingerprint.getDrmId
import com.nest.kanxue.devicefingerprint.getStorageInfo
import com.nest.kanxue.devicefingerprint.getSystemProp
import com.nest.kanxue.exec.InstrumentationUtil
import com.nest.kanxue.exec.ProcessGrep
import com.nest.kanxue.exec.ShellCommandExecutor
import com.nest.kanxue.hardwarerelated.getHardwareRelated
import com.nest.kanxue.inputmethodlist.getInputMethodList
import com.nest.kanxue.location.LocationHelper
import com.nest.kanxue.mcc.TelephonyPropertyCollector
import com.nest.kanxue.model_system_determination.getModelSystemDeter
import com.nest.kanxue.modifymachine.CheckInstallPackageChangerApps
import com.nest.kanxue.network.getNetworkInfo
import com.nest.kanxue.property.PropertyInfoUtils
import com.nest.kanxue.root.CheckRoot
import ScreenUtils
import com.nest.kanxue.screentoolandclick.CheckAutoClick
import com.nest.kanxue.simulators.CheckSimulators
import com.nest.kanxue.sishuiliuyun.sishuiliuyunCpuManager
import com.nest.kanxue.temperature.loadRemperatureUtils
import com.nest.kanxue.testsh.testShellBuildId
import com.nest.kanxue.testsh.testShellGetProp
import com.nest.kanxue.testsh.testShellSTAT
import com.test.ndk.SensorInfo
import com.test.ndk.Testor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 设备数据采集 SDK 统一入口。
 *
 * 推荐用法（SDK 内申请运行时权限后再采集）：
 * ```
 * DeviceCollector.collectAndUpload(
 *     activity = this, // FragmentActivity / AppCompatActivity
 *     callback = object : DeviceCollector.Callback { ... }
 * )
 * ```
 * 若宿主已自行申请权限，也可直接：
 * `DeviceCollector.collectAndUpload(applicationContext, callback)`
 *
 * 固定行为：采集全部项（含传感器/屏幕/定位/CPU 等）并上传。
 * 权限：Manifest 已在 AAR 中声明；运行时弹窗由 SDK 通过透明 Fragment 发起（需传入 Activity）。
 * 未授权项会降级跳过，不中断整体流程。
 */
object DeviceCollector {

    private const val TAG = "DeviceCollector"
    private const val SENSOR_CAPTURE_MILLIS = 5000L
    /** 定位最短等待：即使其它步骤很快结束，也至少留出 GNSS 冷启动时间。 */
    private const val LOCATION_MIN_WAIT_MILLIS = 8000L
    /** 为拿到卫星统计，最多再等到该上限（从启动定位算起）。 */
    private const val LOCATION_MAX_WAIT_MILLIS = 20000L

    /** 采集进度与结果回调。所有方法默认空实现，可按需覆写。 */
    interface Callback {
        /** 每个采集阶段开始/完成时回调，message 为可读描述。 */
        fun onProgress(stage: String) {}
        /** 采集并上传成功，zipFileName 为服务器上的文件名（不含路径）。 */
        fun onSuccess(zipFileName: String) {}
        /** 采集或上传失败。 */
        fun onError(message: String) {}
    }

    // AOSP 动态传感器元事件类型，未在当前 public SDK 中暴露常量。
    private const val TYPE_DYNAMIC_SENSOR_META = 32
    private val running = java.util.concurrent.atomic.AtomicBoolean(false)

    /**
     * 先由 SDK 申请采集所需运行时权限，再开始采集。
     * [activity] 需为 [androidx.fragment.app.FragmentActivity]（AppCompatActivity 即可）。
     * 用户拒绝部分权限时仍会继续采集（对应项降级）。
     */
    @JvmStatic
    @JvmOverloads
    fun collectAndUpload(
        activity: Activity,
        callback: Callback? = null
    ) {
        callback?.onProgress("申请权限")
        PermissionRequestFragment.request(activity) { denied ->
            if (denied.isNotEmpty()) {
                Log.w(TAG, "部分权限未授予，将降级采集: $denied")
                callback?.onProgress("部分权限未授予: ${denied.joinToString()}")
            }
            collectAndUpload(activity.applicationContext, callback)
        }
    }

    /**
     * 仅申请 SDK 所需运行时权限（不开始采集）。
     * 可用于宿主想提前授权的场景。
     */
    @JvmStatic
    fun requestPermissions(
        activity: Activity,
        onFinished: ((denied: List<String>) -> Unit)? = null
    ) {
        PermissionRequestFragment.request(activity) { denied ->
            onFinished?.invoke(denied)
        }
    }

    /** 异步执行：内部起后台线程，立即返回。固定采集全部项并上传。不弹权限框。 */
    @JvmStatic
    @JvmOverloads
    fun collectAndUpload(
        context: Context,
        callback: Callback? = null
    ) {
        if (!running.compareAndSet(false, true)) {
            callback?.onError("采集正在进行中，请勿重复调用")
            return
        }
        val appContext = context.applicationContext
        Thread {
            try {
                runCollect(appContext, callback)
            } catch (e: Throwable) {
                Log.e(TAG, "采集流程异常: ${e.message}", e)
                callback?.onError("采集流程异常: ${e.message}")
            } finally {
                running.set(false)
            }
        }.apply { name = "DeviceCollector" }.start()
    }

    private fun runCollect(context: Context, cb: Callback?) {
        // 统一工作目录：应用私有 filesDir，采集与上传都基于此目录
        val workDir = File(context.filesDir, "nest_collect").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val uploadJsonArray = JSONArray()
        var locationHelper: LocationHelper? = null
        var testor: Testor? = null
        var sensorThread: Thread? = null
        val locationStartedAt = System.currentTimeMillis()

        try {
        // 1. 传感器（NDK）——后台启动，主流程继续，最后停止
        step(cb, "采集传感器") {
            testor = Testor()
            saveSensorList(context, workDir.absolutePath, testor!!)
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            // 保留完整清单；事件订阅排除厂商私有类型和动态传感器管理通道。
            val safeTypes = sm.getSensorList(Sensor.TYPE_ALL)
                .map { it.type }
                .filter { it > 0 && it < Sensor.TYPE_DEVICE_PRIVATE_BASE && it != TYPE_DYNAMIC_SENSOR_META }
                .distinct()
                .toIntArray()
            testor!!.prepare()
            sensorThread = Thread {
                try {
                    testor!!.testSensor(workDir.absolutePath, safeTypes)
                } catch (e: Throwable) {
                    Log.e(TAG, "testSensor 异常: ${e.message}")
                }
            }.apply { name = "SensorCollector"; start() }
        }

        // 1b. 定位——尽早启动，后续步骤执行期间持续刷新
        // Android 16 部分机型要求 LocationManager 的注册必须在主线程执行，
        // 否则 binder 层会抛出 IllegalStateException。
        // 用 CountDownLatch 等待主线程注册完成后再继续后续采集步骤。
        step(cb, "启动定位采集") {
            locationHelper = LocationHelper(
                context = context,
                onError = { msg -> Log.w(TAG, "定位: $msg") }
            )
            val latch = java.util.concurrent.CountDownLatch(1)
            Handler(Looper.getMainLooper()).post {
                try {
                    locationHelper!!.startLocationUpdates()
                } finally {
                    latch.countDown()
                }
            }
            latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
        }

        // 2. 设备指纹
        step(cb, "采集设备指纹") {
            val json = JSONObject()
            val arr = JSONArray()
            runCatching { arr.put(getStorageInfo.getstorage_emulated_0()) }
            runCatching { arr.put(getSystemProp.getPropertyAllInfo()) }
            runCatching { arr.put(JSONObject().put("DRMID", getDrmId.KotlingetDrmId())) }
            json.put("name", "设备指纹")
            json.put("data", encodeArray(arr))
            uploadJsonArray.put(json)
        }

        // 3. 设备标识（stat 文件 + 标识符）
        step(cb, "采集设备标识") {
            val outer = JSONObject()
            val outerArr = JSONArray()
            val statArr = JSONArray()
            STAT_FILE_PATHS.forEach { path ->
                runCatching {
                    statArr.put(JSONObject().put(path, convertToJSONObject(Stat_File_Utils.getFileStat(path))))
                }
            }
            val statJson = JSONObject()
            statJson.put("name", "statFile")
            statJson.put("data", encodeArray(statArr))
            outerArr.put(statJson)
            runCatching { outerArr.put(getDeviceIdentifiers.getInfo(context)) }
            outer.put("name", "设备标识")
            outer.put("data", encodeArray(outerArr))
            uploadJsonArray.put(outer)
        }

        // 4. 硬件相关
        step(cb, "采集硬件相关") {
            uploadJsonArray.put(named("硬件相关", getHardwareRelated.getInfo(context).toString()))
        }

        // 5. 网络相关
        step(cb, "采集网络相关") {
            uploadJsonArray.put(named("网络相关", getNetworkInfo.getInfo(context).toString()))
        }

        // 6. Native DRMID
        step(cb, "采集DRMID(native)") {
            uploadJsonArray.put(JSONObject().put("name", "DRMID").put("data", DrmIdFetcher.getDrmId()))
        }

        // 7. /proc 目录相关（boot id 等）
        step(cb, "采集/proc相关") {
            uploadJsonArray.put(named("/proc目录相关信息", getBootId.getInfo().toString()))
        }

        // 8. APK 安装路径
        step(cb, "采集APK安装路径") {
            uploadJsonArray.put(named("apkInstallPath", getAPKInstallPath.getAPKPath(context).toString()))
        }

        // 9. 输入法列表
        step(cb, "采集输入法列表") {
            uploadJsonArray.put(named("InputMethodList", Gson().toJson(getInputMethodList.getInfo(context))))
        }

        // 10. 机型与系统判定
        step(cb, "采集机型/系统判定") {
            uploadJsonArray.put(named("机型", getModelSystemDeter.getInfo(context).toString()))
        }

        // 11. 自动点击检测
        step(cb, "采集自动点击检测") {
            uploadJsonArray.put(named("AutoClick", CheckAutoClick.getInfo(context).toString()))
        }

        // 12. 模拟器特征
        step(cb, "采集模拟器特征") {
            uploadJsonArray.put(named("模拟器特征", CheckSimulators.getInfo(context).toString()))
        }

        // 13. 改机软件
        step(cb, "采集改机软件") {
            uploadJsonArray.put(named("是否安装改机软件", CheckInstallPackageChangerApps.getInfo(context).toString()))
        }

        // 14. Root
        step(cb, "采集Root状态") {
            uploadJsonArray.put(named("ROOT", Gson().toJson(CheckRoot.getInfo(context))))
        }

        // 15. 证书
        step(cb, "采集证书") {
            uploadJsonArray.put(named("证书(System + User)", CertificateReader().getInfo(context).toString()))
        }

        // 16. 屏幕（主屏 + 副屏）
        step(cb, "采集屏幕信息") {
            uploadJsonArray.put(named("屏幕(主屏+副屏)", updateDisplaysInfo(context).toString()))
        }

        // 17. 编解码器列表
        step(cb, "采集编解码器列表") {
            uploadJsonArray.put(named("系统编码器和解码器列表", CodecInfoCollector().collectCodecInfo().toString()))
        }

        // 18. 地理位置（与传感器并行采集后在此落盘）
        step(cb, "采集地理位置") {
            // 至少等 MIN；若尚无卫星统计，继续等到 MAX 或拿到 satelliteInfo
            val deadline = locationStartedAt + LOCATION_MAX_WAIT_MILLIS
            while (true) {
                val elapsed = System.currentTimeMillis() - locationStartedAt
                val helper = locationHelper
                val hasSat = (helper?.locationInfo?.satelliteCount ?: 0) > 0 &&
                    (helper?.locationInfo?.satelliteInfo?.length() ?: 0) > 0
                if (elapsed >= LOCATION_MIN_WAIT_MILLIS && (hasSat || elapsed >= LOCATION_MAX_WAIT_MILLIS)) {
                    break
                }
                if (System.currentTimeMillis() >= deadline) break
                try {
                    Thread.sleep(500L)
                } catch (_: InterruptedException) {
                    break
                }
            }
            val locJson = locationHelper?.toCollectJson() ?: JSONObject()
            Log.d(
                TAG,
                "地理位置落盘: sat=${locationHelper?.locationInfo?.satelliteCount}, " +
                    "waited=${System.currentTimeMillis() - locationStartedAt}ms, jsonLen=${locJson.toString().length}"
            )
            uploadJsonArray.put(named("地理位置", locJson.toString()))
            // stopLocationUpdates 也必须在主线程调用，与 start 对称
            val helper = locationHelper
            locationHelper = null
            if (helper != null) {
                Handler(Looper.getMainLooper()).post { helper.stopLocationUpdates() }
            }
        }

        // 19. shell 相关
        step(cb, "采集shell相关") {
            val sub = JSONObject()
            runCatching { sub.put("sh -c /system/bin/getprop", testShellGetProp.getSystemProps()) }
            runCatching { sub.put("sh -c getprop ro.system.build.id", testShellBuildId.getSystemBuildId()) }
            runCatching { sub.put("sh -c stat", testShellSTAT.getPathStatsAsJson()) }
            uploadJsonArray.put(named("shell相关", sub.toString()))
        }

        // 20. exec 相关
        step(cb, "采集exec相关") {
            val executor = ShellCommandExecutor()
            val sub = JSONObject()
            runCatching { sub.put("exec sh -c pm path com.tencent.mm", executor.getPackagePath("com.tencent.mm")) }
            runCatching { sub.put("exec sh -c ps | grep adbd", ProcessGrep().executeShellCommandAlternative()) }
            runCatching { sub.put("exec sh -c pm path com.xiaomi.market", executor.getPackagePath("com.xiaomi.market")) }
            runCatching { sub.put("exec pm list instrumentation", InstrumentationUtil().getInstrumentationList()) }
            runCatching { sub.put("exec sh -c pm path com.android.vending", executor.getPackagePath("com.android.vending")) }
            uploadJsonArray.put(named("exec sh相关", sub.toString()))
        }

        // 21. JNI（Cname + BootTime + statfs64）
        step(cb, "采集JNI信息") {
            val sub = JSONObject()
            runCatching { sub.put("Cname info - Hex", DrmIdFetcher.getCnameInfoHex()) }
            runCatching {
                val bootTime = DrmIdFetcher.getBootTime()
                sub.put("Boot Time - seconds", bootTime?.get(0))
                sub.put("Boot Time - nanoseconds", bootTime?.get(1))
            }
            val statfs = JSONObject()
            STATFS64_PATHS.forEach { p ->
                runCatching { statfs.put(p, DrmIdFetcher.getStatFsInfo(p)) }
            }
            sub.put("statfs64", statfs)
            uploadJsonArray.put(named("通过JNI读取Cname + BootTime", sub.toString()))
        }

        // 22. SIM 卡 / 运营商
        step(cb, "采集SIM卡信息") {
            val result = TelephonyPropertyCollector(context).getTelephonyPropertiesJson()
            uploadJsonArray.put(named("SIM卡信息相关", result.toString()))
        }

        // 23. sishuiliuyun 系统属性
        step(cb, "采集sishuiliuyun属性") {
            // 已是 JSONObject，直接 toString，避免 Gson 反射内部 Map 时与其它线程冲突
            val info = sishuiliuyunCpuManager().getInfo(context).toString()
            uploadJsonArray.put(named("sishuiliuyun-系统相关属性", info))
        }

        // 写 allData.txt
        val fileSuffix = Build.MODEL + "_" + Utils.getCurrentDateTime()
        step(cb, "写入allData.txt") {
            File(workDir, "${fileSuffix}_allData.txt").writeText(uploadJsonArray.toString())
        }

        // 保存各类文件到 workDir
        step(cb, "保存温度信息") { loadRemperatureUtils.saveTemperatureInfo(workDir) }
        step(cb, "复制property_info") { PropertyInfoUtils.copyPropertyInfo(workDir) }
        step(cb, "保存getprop") { ShellGetProp.saveSystemPropsToFile(workDir) }

        step(cb, "保存stat-f") {
            val statF = File(workDir, "stat-f")
            runCatching { ShellGetStat_F_Data.saveSystemPropsToFile(statF) }
            runCatching { ShellGetStat_F_Odm.saveSystemPropsToFile(statF) }
            runCatching { ShellGetStat_F_Odm_dlkm.saveSystemPropsToFile(statF) }
            runCatching { ShellGetStat_F_Product.saveSystemPropsToFile(statF) }
            runCatching { ShellGetStat_F_System_ext.saveSystemPropsToFile(statF) }
            runCatching { ShellGetStat_F_Vendor.saveSystemPropsToFile(statF) }
        }

        step(cb, "保存stat") {
            val statFile = File(workDir, "stat")
            runCatching { ShellGetStat_All.saveSystemPropsToFile(statFile) }
            runCatching { ShellGetStat_Odm.saveSystemPropsToFile(statFile) }
            runCatching { ShellGetStat_Product.saveSystemPropsToFile(statFile) }
            runCatching { ShellGetStat_System_ext.saveSystemPropsToFile(statFile) }
            runCatching { ShellGetStat_Vendor.saveSystemPropsToFile(statFile) }
        }

        step(cb, "保存cgroup") { ShellGetCgroup.saveSystemPropsToFile(workDir) }
        step(cb, "保存am get-config") { Shell_AM_GetConfig.saveSystemPropsToFile(workDir) }
        step(cb, "保存pm list features") { Shell_PM_List_Features.saveSystemPropsToFile(workDir) }
        step(cb, "保存mounts") { ShellGetMounts.saveSystemPropsToFile(workDir) }
        step(cb, "保存diskstats") { ShellGetDiskstats.saveSystemPropsToFile(workDir) }
        step(cb, "保存cpuinfo") { ShellGetCpuInfo.saveSystemPropsToFile(workDir) }
        step(cb, "保存mountinfo") { ShellGetMountinfo.saveSystemPropsToFile(workDir) }
        step(cb, "保存maps") { ShellGetMaps.saveSystemPropsToFile(workDir) }
        step(cb, "保存mountstats") { ShellGetMountstats.saveSystemPropsToFile(workDir) }
        step(cb, "保存environ") { ShellGetEnviron.saveSystemPropsToFile(workDir) }
        step(cb, "保存meminfo") { ShellGetMeminfo.saveSystemPropsToFile(workDir) }
        step(cb, "保存version") { ShellGetLinuxVersion.saveVersionToFile(workDir) }
        step(cb, "保存kernel") { ShellGetKernel.saveSystemPropsToFile(workDir) }
        step(cb, "保存lspci") { Shell_lspci.saveSystemPropsToFile(workDir) }
        step(cb, "保存lsusb") { Shell_lsusb.saveSystemPropsToFile(workDir) }
        step(cb, "保存lshal") { Shell_lshal.saveSystemPropsToFile(workDir) }
        step(cb, "保存camera") { GetCameraInfo.saveSystemPropsToFile(context, workDir) }
        step(cb, "复制camera.txt") {
            ShellCommandExecutor().executeShellCommand("cp /data/local/tmp/camera.txt ${workDir.absolutePath}/camera.txt")
        }
        step(cb, "复制dumpsys_input.txt") {
            ShellCommandExecutor().executeShellCommand("cp /data/local/tmp/dumpsys_input.txt ${workDir.absolutePath}/dumpsys_input.txt")
        }
        step(cb, "保存电池信息") { Batteryutils.getAllBatteryInfo(context, workDir) }

        // CPU/温度 大文件
        step(cb, "复制CPU文件") {
            val sourceDir = File("/sys/devices/system/cpu")
            if (sourceDir.exists()) {
                CpuFilesCopier(context, File(workDir, "cpu")).copyCpuFiles()
            }
            TempFilesCopier(context, File(workDir, "temp")).copyFiles()
        }

        step(cb, "保存显卡信息") { DisplayCard.saveDisplayCardInfo(workDir) }
        step(cb, "保存input service") { GetInutService.saveInutServiceToFile(context, workDir) }
        step(cb, "保存service list") { GetServiceList.saveServiceListToFile(context, workDir) }
        step(cb, "保存全部codec") { GetAllCodec.saveAllCodecToFile(context, workDir) }
        step(cb, "保存全部电池信息") { GetAllBatteryInfo.saveAllBatteryInfoToFile(context, workDir) }
        step(cb, "保存全部vulkan信息") { GetAllVulkanInfo.saveAllVulkanInfoToFile(context, workDir) }
        step(cb, "复制power_supply") { Power_SupplyFilesCopier(context, File(workDir, "power_supply")).copyPower_SupplyFiles() }

        // 等待传感器采集完成再停止
        try {
            Thread.sleep(SENSOR_CAPTURE_MILLIS)
        } catch (_: InterruptedException) {
        }
        runCatching { testor?.stop() }
        sensorThread?.join()

        // 传感器文件长度校验（仅记录日志）
        step(cb, "校验传感器文件") {
            val checkJson = CheckSensorLength.check(workDir)
            Log.d(TAG, "checkSensorJSON = $checkJson")
        }

        cb?.onProgress("开始上传...")
        UploadData.upload(
            context,
            workDir.absolutePath,
            fileSuffix,
            onSuccess = { cb?.onSuccess("$fileSuffix.zip") },
            onFailure = { msg ->
                // 兼容原逻辑：服务器返回非字符串 body 时视为成功
                if (msg.contains("Expected a string but was BEGIN_OBJECT")) {
                    cb?.onSuccess("$fileSuffix.zip")
                } else {
                    cb?.onError(msg)
                }
            }
        )
        } finally {
            runCatching {
                val helper = locationHelper
                locationHelper = null
                if (helper != null) {
                    Handler(Looper.getMainLooper()).post { helper.stopLocationUpdates() }
                }
            }
            runCatching { testor?.stop() }
            // 异常路径也等待写入线程退出，再允许下一次采集清理目录。
            var interrupted = false
            while (sensorThread?.isAlive == true) {
                try {
                    sensorThread?.join()
                } catch (_: InterruptedException) {
                    interrupted = true
                }
            }
            if (interrupted) Thread.currentThread().interrupt()
        }
    }

    // ---------- 辅助方法 ----------

    private inline fun step(cb: Callback?, stage: String, block: () -> Unit) {
        cb?.onProgress(stage)
        try {
            block()
        } catch (e: Throwable) {
            Log.e(TAG, "[$stage] 失败: ${e.message}", e)
        }
    }

    private fun named(name: String, data: String): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("data", Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
    }

    private fun encodeArray(arr: JSONArray): String {
        val list: List<*>? = Gson().fromJson(arr.toString(), List::class.java)
        val jsonString = Gson().toJson(list)
        return Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    private fun updateDisplaysInfo(context: Context): JSONObject {
        val jsonObject = JSONObject()
        val displayMode = ScreenUtils(context).getDisplayMode()
        val (main, secondary) = ScreenUtils(context).getScreensInfo()
        jsonObject.put("主屏", main.toString())
        jsonObject.put("主屏正在scrcpy或其他工具进行镜像显示", displayMode.isScreenMirroring)
        if (secondary.isEmpty()) {
            // 无副屏
        } else {
            secondary.forEachIndexed { index, screen ->
                jsonObject.put("副屏 - $index", screen.toString())
            }
        }
        return jsonObject
    }

    private fun saveSensorList(context: Context, sensorFileDir: String, testor: Testor) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sm.getSensorList(Sensor.TYPE_ALL)
        val list = ArrayList<SensorInfo>()
        for (sensor in sensors) {
            list.add(
                SensorInfo(
                    testor.getSensorHandle(sensor.name, sensor.type),
                    sensor.name,
                    sensor.type,
                    sensor.vendor,
                    sensor.resolution,
                    sensor.stringType,
                    sensor.reportingMode,
                    sensor.maximumRange,
                    sensor.maxDelay,
                    sensor.fifoReservedEventCount,
                    sensor.highestDirectReportRateLevel,
                    sensor.fifoMaxEventCount,
                    sensor.power,
                    sensor.minDelay,
                    sensor.version,
                    sensor.isWakeUpSensor
                )
            )
        }
        File("$sensorFileDir/sensors.txt").writeText(Gson().toJson(list))
    }

    private val STAT_FILE_PATHS = listOf(
        "/data", "/", "/vendor/etc/camera", "/mnt", "/system/bin/idmap2d",
        "/dev/socket", "/dev/console", "/dev/dri/renderD128",
        "/proc/self/fdinfo/", "/proc/asound/", "/proc/cpuinfo", "/proc/stat/",
        "/proc/self/mounts", "/proc/self/net/tcp", "/sys/class/net/wlan0/address",
        "/sys/block/mmcblk0/device/cid", "/proc/version", "/proc/self/status",
        "/proc/self/maps", "/proc/self/stat", "/proc/1/cgroup", "/proc/self/cgroup",
        "/proc/self/environ", "/proc/self/limits", "/proc/self/mountinfo",
        "/proc/self/wchan", "/proc/self/cmdline", "/proc/self/comm",
        "/proc/self/attr/current", "/proc/self/oom_score", "/proc/self/oom_score_adj",
        "/proc/self/loginuid", "/proc/self/sessionid", "/proc/self/coredump_filter",
        "/proc/self/task", "/proc/self/fd"
    )

    private val STATFS64_PATHS = listOf(
        "/data", "/odm", "/odm_dlkm", "/product",
        "/system", "/system_ext", "/vendor", "/vendor_dlkm"
    )
}
