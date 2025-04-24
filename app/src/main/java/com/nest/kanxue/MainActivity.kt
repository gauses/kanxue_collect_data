package com.nest.kanxue

import CodecInfoCollector
import LocationHelper
import ScreenUtils
import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nest.kanxue.apkinstallpath.getAPKInstallPath
import com.nest.kanxue.bootid.getBootId
import com.nest.kanxue.bootid.getBootId.getFileContentUsingFile
import com.nest.kanxue.camera.GetCameraInfo
import com.nest.kanxue.cert.CertificateReader
import com.nest.kanxue.core.Batteryutils
import com.nest.kanxue.core.CheckSensorLength
import com.nest.kanxue.core.CpuFilesCopier
import com.nest.kanxue.core.DisplayCard
import com.nest.kanxue.core.OpenFrameWorkJar
import com.nest.kanxue.core.Power_SupplyFilesCopier
import com.nest.kanxue.core.ShellGetCgroup
import com.nest.kanxue.core.Shell_AM_GetConfig
import com.nest.kanxue.core.ShellGetCpuInfo
import com.nest.kanxue.core.ShellGetDiskstats
import com.nest.kanxue.core.ShellGetKernel
import com.nest.kanxue.core.ShellGetLinuxVersion
import com.nest.kanxue.core.ShellGetMeminfo
import com.nest.kanxue.core.ShellGetMounts
import com.nest.kanxue.core.ShellGetProp
import com.nest.kanxue.core.Shell_PM_List_Features
import com.nest.kanxue.core.Shell_lshal
import com.nest.kanxue.core.Shell_lspci
import com.nest.kanxue.core.Shell_lsusb
import com.nest.kanxue.core.TempFilesCopier
import com.nest.kanxue.core.stat.ShellGetStat_F_Data
import com.nest.kanxue.core.stat.ShellGetStat_F_Odm
import com.nest.kanxue.core.stat.ShellGetStat_F_Odm_dlkm
import com.nest.kanxue.core.stat.ShellGetStat_F_Product
import com.nest.kanxue.core.stat.ShellGetStat_F_System_ext
import com.nest.kanxue.core.stat.ShellGetStat_F_Vendor
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import com.nest.kanxue.devicefingerprint.getDrmId
import com.nest.kanxue.devicefingerprint.getStorageInfo
import com.nest.kanxue.devicefingerprint.getSystemProp
import com.nest.kanxue.deviceidentification.getDeviceIdentifiers
import com.nest.kanxue.exec.InstrumentationUtil
import com.nest.kanxue.exec.ProcessGrep
import com.nest.kanxue.exec.ShellCommandExecutor
import com.nest.kanxue.hardwarerelated.CustomGLSurfaceView
import com.nest.kanxue.hardwarerelated.getHardwareRelated
import com.nest.kanxue.inputmethodlist.getInputMethodList
import com.nest.kanxue.mcc.TelephonyPropertyCollector
import com.nest.kanxue.model_system_determination.getModelSystemDeter
import com.nest.kanxue.modifymachine.CheckInstallPackageChangerApps
import com.nest.kanxue.network.getNetworkInfo
import com.nest.kanxue.root.CheckRoot
import com.nest.kanxue.screentoolandclick.CheckAutoClick
import com.nest.kanxue.simulators.CheckSimulators
import com.nest.kanxue.sishuiliuyun.sishuiliuyunCpuManager
import com.nest.kanxue.statprocpath.FileStatsAdapter
import com.nest.kanxue.temperature.loadRemperatureUtils
import com.nest.kanxue.testsh.testShellBuildId
import com.nest.kanxue.testsh.testShellGetProp
import com.nest.kanxue.testsh.testShellSTAT
import com.nest.kanxue.utils.ByteArrayConverter
import com.nest.kanxue_data.R
import com.nest.kanxue_data.databinding.ActivityMainBinding
import com.test.ndk.SensorInfo
import com.test.ndk.Testor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    val testor = Testor()

    private lateinit var mCpuFilesCopier: com.nest.kanxue.core.CpuFilesCopier
    private lateinit var mTempFilesCopier: com.nest.kanxue.core.TempFilesCopier

    private lateinit var mPower_SupplyFilesCopier: Power_SupplyFilesCopier


    private lateinit var locationHelper: LocationHelper


    companion object {
        private const val PERMISSION_REQUEST_CODE = 100000  // 可以是任意整数，通常从100开始
        private const val PACKAGE_USAGE_STATS_REQUEST = 10012
        private const val requestCodeCameraPermission = 100013

    }
    // 检查并请求所需权限
    private fun checkAndRequestPermissions(context: Context) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.CAMERA
        )

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startLocationTracking()
        }
    }

    private fun startLocationTracking() {
        if (!checkLocationEnabled()) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            return
        }
        locationHelper.startLocationUpdates()
    }

    private fun checkLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var confirmButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var statsAdapter: FileStatsAdapter
    private lateinit var fileContent : TextView
    private lateinit var cpuCaptureSwitch: android.widget.Switch
    private var isCpuCapturing = true  // 设置为true以匹配Switch的默认状态


    //地理位置属性
    var locationJSONObject = JSONObject();




    private lateinit var binding: ActivityMainBinding

    var externalDir = ""
    var uploadJsonArray: JSONArray = JSONArray()

    var stat_file_path = arrayOf(
        "/sdcard/Android/data/.nomedia",
        "/sdcard/Android/data/com.google.android.gms",
        "/sdcard/",
        "/storage/emulated/0",
        "/data/system",
        "/vendor/firmware",
        "/system/bin",
        "/vendor/lib",
        "/system/framework",
        "/system/fonts",
        "/proc/self/mounts"
//        "/proc/stat",
//        "/sys/firmware/devicetree/base/compatible",
//        "/dev/fuse"
    )

    // request to write external storage
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RESULT_OK){
            // get external storage dir path
            getExternalFilesDir(null)?.let {
                externalDir = it.absolutePath
                Log.d("sb", "external path=$externalDir")
            }?:{
                Log.e("sb", "external path=null")
            }
        }
    }

    private var mySurfaceView: CustomGLSurfaceView? = null

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PACKAGE_USAGE_STATS_REQUEST) {
            if (hasUsageStatsPermission()) {
                getMemoryInfo()
            } else {
                Toast.makeText(this, "Permission required to get memory info", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, PACKAGE_USAGE_STATS_REQUEST)
    }

    private fun getMemoryInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取 ActivityManager
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                // 获取所有运行中的进程
                val runningProcesses = activityManager.runningAppProcesses

                // 查找 adbd 进程
                val adbdProcess = runningProcesses?.find { it.processName.contains("adbd") }

                if (adbdProcess != null) {
                    // 获取进程的内存信息
                    val memoryInfo = Debug.MemoryInfo()
//                    Debug.getMemoryInfo(adbdProcess.pid, memoryInfo)

                    val processMemoryInfo = """
                        Process: ${adbdProcess.processName}
                        PID: ${adbdProcess.pid}
                        Total PSS: ${memoryInfo.totalPss}kB
                        Native PSS: ${memoryInfo.nativePss}kB
                        Java PSS: ${memoryInfo.dalvikPss}kB
                        Other PSS: ${memoryInfo.otherPss}kB
                        
                    """.trimIndent()


                } else {
                    Log.d("adbd",  "adbd process not found")
                }
            } catch (e: Exception) {
                Log.d("adbd",  "adbd error = " + e.message)

            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySurfaceView = CustomGLSurfaceView(this)
        val frame = findViewById<FrameLayout>(R.id.SurfaceViewFrame)
        frame.addView(mySurfaceView)

        val lastTargetDir_suffix = "nest_" + System.currentTimeMillis()/1000
        //整个要上传的目录
        val lastTargetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), lastTargetDir_suffix)
        //整个要上传的cpu目录
        val lastCPUTargetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), lastTargetDir_suffix + "/cpu")
        val lastTempTargetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), lastTargetDir_suffix + "/temp")

        //确定是否要上传cpu文件
        cpuCaptureSwitch = findViewById(R.id.cpu_capture_switch)
        cpuCaptureSwitch.setOnCheckedChangeListener { _, isChecked ->
            isCpuCapturing = isChecked
            if (isChecked) {
                Toast.makeText(this, "需要上传CPU文件", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "不需要上传cpu文件", Toast.LENGTH_SHORT).show()
            }
        }


        thread {
            //先清除
            Utils.clearFilesDir(this)
            //写入sensor
            saveSensorList(this, this.filesDir.absolutePath)
            testor.testSensor(this.filesDir.absolutePath,  intArrayOf(Sensor.TYPE_ALL))
        }

        locationHelper = LocationHelper(
            context = this,
            onLocationUpdate = { location ->
                Log.d("LocationActivity", "Location updated: $location")
                locationJSONObject.apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("altitude", location.altitude)
                    put("speed", location.speed)
                    put("speedAccuracyMetersPerSecond", location.speedAccuracyMetersPerSecond)
                    put("bearingAccuracyDegrees", location.bearingAccuracyDegrees)
                    put("accuracy", location.accuracy)
                    put("verticalAccuracy", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        location.verticalAccuracy
                    } else {
                        0f
                    })
                    put("horizontalAccuracy", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        location.accuracy
                    } else {
                        0f
                    })
                }

            },
            onSatelliteUpdate = { satelliteInfo ->
                Log.d("LocationActivity", "Satellite info updated: $satelliteInfo")
                locationJSONObject.apply {
                    put("satelliteInfo", locationHelper.currentSatelliteInfo)
                    put("number of satellite", locationHelper.currentSatelliteCount)
                    put("PDOP", locationHelper.currentPdop)
                    put("HDOP", locationHelper.currentHdop)
                    put("VDOP", locationHelper.currentVdop)

                }
            },
            onError = { error ->
                Log.e("LocationActivity", "Location error: $error")
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                // 如果是GPS未开启，提示用户去设置
                if (error.contains("GPS is disabled")) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            }
        )

        checkAndRequestPermissions(this)


        val uploadStatus = findViewById<TextView>(R.id.uploadStatusText)
        val checkSensorText = findViewById<TextView>(R.id.checkSensorText)
        val copyCPUResult = findViewById<TextView>(R.id.copyCPUResult)



        autoCompleteTextView = findViewById(R.id.pathInput)
        // 获取/proc目录下的所有文件和目录
        fun getProcPaths(): List<String> {
            val paths = mutableListOf<String>()
            try {
                val procDir = File("/proc")
                procDir.listFiles()?.forEach { file ->
                    paths.add("/proc/${file.name}")
                    // 如果是目录，添加其子目录
                    if (file.isDirectory) {
                        file.listFiles()?.forEach { subFile ->
                            paths.add("/proc/${file.name}/${subFile.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return paths
        }
        // 创建适配器
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getProcPaths()
        )
        // 设置适配器
        autoCompleteTextView.setAdapter(adapter)
        // 设置触发自动完成的最小字符数
        autoCompleteTextView.threshold = 1
        // 设置输入监听
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 如果输入的不是以/proc开头，自动添加/proc/
//                if (!s.toString().startsWith("/proc")) {
//                    autoCompleteTextView.setText("/proc/${s.toString()}")
//                    autoCompleteTextView.setSelection(autoCompleteTextView.text.length)
//                }
            }
        })


        confirmButton = findViewById(R.id.confirmButton)
        recyclerView = findViewById(R.id.recyclerView)
        fileContent = findViewById(R.id.file_content)
//        autoCompleteTextView.setText("/proc/self/net/arp")
//        autoCompleteTextView.setText("/proc/self/mounts")
        recyclerView.layoutManager = LinearLayoutManager(this)
        statsAdapter = FileStatsAdapter()
        recyclerView.adapter = statsAdapter




        confirmButton.setOnClickListener {
            val path = autoCompleteTextView.text.toString()
            if (path.isNotEmpty() ) {
                val file = File(path)
                if (file.exists()) {
                    val fileList = mutableListOf<FileStat>()
                    val statFile = Stat_File_Utils.getFileStat(path)
                    Log.d("sb" , "confirmButton statFile = $statFile")
                    fileList.add(statFile)
                    Log.d("sb" , "confirmButton statFile fileList = ${fileList.size}")

                    statsAdapter.submitList(fileList)

                    fileContent.text = getFileContentUsingFile(path)

                }
            }
        }

        val testbutton = findViewById<Button>(R.id.test)
        testbutton.setOnClickListener{

            //Framework.jar
            val success = OpenFrameWorkJar.copyFrameworkJar(lastTargetDir)
            if (success) {
                Log.d("OpenFrameWorkJar", "Framework.jar copied successfully")
            } else {
                Log.e("OpenFrameWorkJar", "Failed to copy framework.jar")
            }

            // 获取 framework.jar 信息
            val info = OpenFrameWorkJar.getFrameworkJarInfo()
            Log.d("OpenFrameWorkJar", info)


            val displayCardInfo = DisplayCard.getDisplayCardInfo()
            Log.d("displayCardInfo", "显卡数据 = " + DisplayCard.getDisplayCardInfo())
            Log.d("displayCardInfo", "显卡打印数据 = " + DisplayCard.printDeviceInfo())



            Log.d("loadRemperatureUtils", "温度传感器 = " + loadRemperatureUtils.getTemperatureInfo())

//            // 打印电池信息
//            val batteryInfo = Batteryutils.getAllBatteryInfo(this@MainActivity, lastTargetDir)
//            val batteryStatus = Batteryutils.getBatteryStatusInfo(this@MainActivity)
//
//            Log.d("BatteryInfo", "═══════════════ 电池基本信息 ═══════════════")
//            batteryInfo.forEach { (key, value) ->
//                Log.d("BatteryInfo", "$key = $value")
//            }
//
//            Log.d("BatteryInfo", "═══════════════ 电池状态信息 ═══════════════")
//            batteryStatus.forEach { (key, value) ->
//                Log.d("BatteryInfo", "$key = $value")
//            }




//            if (!lastTargetDir.exists()) {
//                if (!lastTargetDir.mkdirs()) {
//                    Toast.makeText(this@MainActivity, "无法创建目标目录: ${lastTargetDir.absolutePath}", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//            }
//            mCpuFilesCopier = CpuFilesCopier(this@MainActivity, lastCPUTargetDir)
//
//
//            val sourceDir = File("/sys/devices/system/cpu")
//            runOnUiThread {
//                Toast.makeText(this@MainActivity, "sourceDir存在 =" +sourceDir.exists() , Toast.LENGTH_SHORT).show()
//            }
//            if (sourceDir.exists()) {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "cpu文件个数="+mCpuFilesCopier.countFilesInDirectory(sourceDir), Toast.LENGTH_SHORT).show()
//                    uploadStatus.text = "开始执行mCpuFilesCopier.copyCpuFiles()"
//                }
//                val resultJson = mCpuFilesCopier.copyCpuFiles()
//                runOnUiThread {
//                    uploadStatus.text = "执行mCpuFilesCopier结果 = $resultJson"
//                    Thread.sleep(3000)
//                }
//            }else{
//                runOnUiThread {
//                    uploadStatus.text = "/sys/devices/system/cpu 路径不存在，跳过CPU复制"
//                    Thread.sleep(2000)
//                }
//            }







//            Log.d("NDK_DRM_ID", ""+DrmIdFetcher.getDrmId())
//
//
//            Log.d("getStorageInfo.getTotalLong", ""+getStorageInfo.getTotalLong(Environment.getDataDirectory().absolutePath))
//
//
//            val cameraInfo = GetCameraInfo.getCameraResolutions(this)
//            if (cameraInfo.has("error")) {
//                // 处理错误
//                Log.e("Camera", cameraInfo.getString("error"))
//            } else {
//                // 处理相机信息
//                Log.d("Camera", cameraInfo.toString()
//                )
//                val cameras = cameraInfo.getJSONArray("cameras")
//                // ... 使用相机信息
//
//            }
//
//
//            val bootTime: LongArray? = DrmIdFetcher.getBootTime()
//            System.out.println("Boot Time: " + bootTime?.get(0) + " seconds, " + bootTime?.get(1) + " nanoseconds");
//
//
//            val reader = TunInfoReader()
//
//            try {
//                // 读取所有 TUN 信息
//                val allInfo = reader.readTunInfo()
//                println("所有 TUN 信息:")
//                allInfo.forEach { (name, value) ->
//                    println("$name: $value")
//                }
//
//                // 读取特定属性，例如 dev_id
//                val devId = reader.readTunProperty("dev_id")
//                println("\n设备 ID: $devId")
//
//            } catch (e: IOException) {
//                println("错误: ${e.message}")
//            }
//
//
//            Log.d("sb" , "getCgroupUsingCat= "+getBootId.getARPUsingFile())
//            uploadStatus.text = getBootId.getARPUsingFile()
//
//            // 在 Activity 或其他地方使用
//            val collector = CodecInfoCollector()
//            // 收集所有编解码器信息
//            val allCodecInfo = collector.collectCodecInfo()
//            Log.d("sb" , "allCodecInfo= $allCodecInfo")
//            collector.logCodecInfo()



        }




        val button = findViewById<Button>(R.id.stat_file_btn)

        getDeviceIdentifiers.fetchAdIdWithLatency(this) { adId, latency, error ->
            println("Ad ID: $adId")
            println("Fetch Ad ID Latency: ${latency}ms")

            Thread.sleep(2000)

//            button.performClick()
        }

        // 设置点击事件
        button.setOnClickListener {
            // 使用协程来处理耗时操作
            CoroutineScope(Dispatchers.IO).launch {
                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    uploadStatus.text = "开始采集sensor，等待5秒钟————————>"
                }

            //4.设备指纹
                withContext(Dispatchers.Main) {
            uploadStatus.text = "开始采集设备指纹————————>"
                }

                val devicefingerprintson = JSONObject()
                val devicefingerprintJsonArray = JSONArray()
            devicefingerprintJsonArray.put(getStorageInfo.getstorage_emulated_0())
            devicefingerprintJsonArray.put(getSystemProp.getPropertyAllInfo())


            with(ByteArrayConverter) {
                // 1. 转换成十六进制
                    val DrmId = getDrmId.KotlingetDrmId()
                    devicefingerprintJsonArray.put(JSONObject().put("DRMID", DrmId))
            }
            devicefingerprintson.put("name", "设备指纹") ;
            val devicefingerprintList: List<*>? = Gson().fromJson(devicefingerprintJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val devicefingerprintJsonString = Gson().toJson(devicefingerprintList) // 将 List 转换为 JSON 字符串
            devicefingerprintson.put("data", Base64.encodeToString(devicefingerprintJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(devicefingerprintson)


            //5.设备标识
                withContext(Dispatchers.Main) {
            uploadStatus.text = "开始采集设备标识————————>"
                }

            val DeviceIdentifiersJson = JSONObject();
            val DeviceIdentifiersJsonArray = JSONArray();

            val statJsonArray = JSONArray();
            stat_file_path.forEach { fileName ->
                val fileStat = Stat_File_Utils.getFileStat(fileName)

//                statJsonArray.put(JSONObject().put(fileName, fileStat))
                statJsonArray.put(JSONObject().put(fileName, convertToJSONObject(fileStat)))
            }

            val statJson = JSONObject();
            statJson.put("name", "statFile") ;
            val gson = Gson()
            val list: List<*>? = gson.fromJson(statJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val jsonString = gson.toJson(list) // 将 List 转换为 JSON 字符串
            statJson.put("data", Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            DeviceIdentifiersJson.put("name", "设备标识") ;
            DeviceIdentifiersJsonArray.put(statJson) ;
                DeviceIdentifiersJsonArray.put(getDeviceIdentifiers.getInfo(this@MainActivity)) ;
            val deviceIdentifiersList: List<*>? = Gson().fromJson(DeviceIdentifiersJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val deviceIdentifiersJsonString = Gson().toJson(deviceIdentifiersList) // 将 List 转换为 JSON 字符串
            DeviceIdentifiersJson.put("data", Base64.encodeToString(deviceIdentifiersJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(DeviceIdentifiersJson)


            //6.硬件相关
                withContext(Dispatchers.Main) {
            uploadStatus.text = "开始采集硬件相关————————>"
                }

            val hardwareJson = JSONObject();
//            hardwareJson.put("name", "HardwareRelated") ;
            hardwareJson.put("name", "硬件相关") ;
                hardwareJson.put("data", Base64.encodeToString(getHardwareRelated.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(hardwareJson)



            //7.网络相关
                withContext(Dispatchers.Main) {
            uploadStatus.text = "开始采集网络相关————————>"
                }

            val networkJson = JSONObject();
            networkJson.put("name", "网络相关") ;
            networkJson.put("data", Base64.encodeToString(getNetworkInfo.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(networkJson)


                //NATIVE获取DRMID
                val DRMIDJson = JSONObject();
                DRMIDJson.put("name", "DRMID") ;
                DRMIDJson.put("data", DrmIdFetcher.getDrmId()) ;
                uploadJsonArray.put(DRMIDJson)


            //8.boot id
            //cat命令读取/proc/self/mounts
            //cat命令读取/proc/sys/kernel/random/boot_id
            ///proc/meminfo
            val BootIdJson = JSONObject();
            BootIdJson.put("name", "/proc目录相关信息") ;
            BootIdJson.put("data", Base64.encodeToString(getBootId.getInfo().toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(BootIdJson)




            //9.APK Install Path
                withContext(Dispatchers.Main) {
            uploadStatus.text = "开始采集APK Install Path————————>"
                }

            val apkInstallPathJson = JSONObject();
            apkInstallPathJson.put("name", "apkInstallPath") ;
                apkInstallPathJson.put("data", Base64.encodeToString(getAPKInstallPath.getAPKPath(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(apkInstallPathJson)


            //10.输入法
            val InputMethodListJson = JSONObject();
            InputMethodListJson.put("name", "InputMethodList") ;
                InputMethodListJson.put("data", Base64.encodeToString(Gson().toJson(getInputMethodList.getInfo(this@MainActivity)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(InputMethodListJson)


            //11.机型和系统判定 + Bootloader解锁状态 + SIM卡
            val deviceOSListJson = JSONObject();
//            deviceOSListJson.put("name", "deviceOS") ;
            deviceOSListJson.put("name", "机型") ;
                deviceOSListJson.put("data", Base64.encodeToString(getModelSystemDeter.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(deviceOSListJson)


            //12.截图和模拟点击
            val AutoClickerJson = JSONObject();
            AutoClickerJson.put("name", "AutoClick") ;
                AutoClickerJson.put("data", Base64.encodeToString(CheckAutoClick.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(AutoClickerJson)


            //13.模拟器 :扫描常见的模拟器特征
            val simulatorsJson = JSONObject();
            simulatorsJson.put("name", "模拟器特征") ;
                simulatorsJson.put("data", Base64.encodeToString(CheckSimulators.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(simulatorsJson)

            //14.改机软件
            val chageAppsJson = JSONObject();
            chageAppsJson.put("name", "是否安装改机软件") ;
                chageAppsJson.put("data", Base64.encodeToString(CheckInstallPackageChangerApps.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(chageAppsJson)



            //15.root
            val rootJson = JSONObject();
            rootJson.put("name", "ROOT") ;
                rootJson.put("data", Base64.encodeToString(Gson().toJson(CheckRoot.getInfo(this@MainActivity)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(rootJson)


            //16.证书
            val certJson = JSONObject();
            certJson.put("name", "证书(System + User)") ;
                certJson.put("data", Base64.encodeToString(CertificateReader().getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(certJson)


            //17.screen (主屏 + 副屏)
            val screenJson = JSONObject();
            screenJson.put("name", "屏幕(主屏+副屏)") ;
            screenJson.put("data", Base64.encodeToString(updateDisplaysInfo().toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(screenJson)


            //18.codec (系统编码器和解码器列表)
            val codecJson = JSONObject();
            codecJson.put("name", "系统编码器和解码器列表") ;
            codecJson.put("data", Base64.encodeToString(CodecInfoCollector().collectCodecInfo().toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(codecJson)


            //19.location
            val locationInfoJson = JSONObject();
            Log.d("sb" , "locationJSONObject = $locationJSONObject")
            locationInfoJson.put("name", "地理位置") ;
            locationInfoJson.put("data", Base64.encodeToString(locationJSONObject.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(locationInfoJson)


            //20、一些shell相关的内容
//            Log.d("testShellGetProp" , "getSystemProperties= "+ testShellGetProp.getSystemProps())
//            Log.d("testShellGetProp" , "getSystemBuildId= "+ testShellBuildId.getSystemBuildId())
//            Log.d("testShellGetProp" , "getPathStatsAsJson= "+ testShellSTAT.getPathStatsAsJson())
            val shellJson = JSONObject();
            shellJson.put("name", "shell相关") ;
            var subShellJson = JSONObject()
            subShellJson.put("sh -c /system/bin/getprop", testShellGetProp.getSystemProps())
//            subShellJson.put("sh -c /system/bin/getprop", testShellGetProp.getSystemProps1())
            subShellJson.put("sh -c getprop ro.system.build.id", testShellBuildId.getSystemBuildId())
            subShellJson.put("sh -c stat", testShellSTAT.getPathStatsAsJson())
            shellJson.put("data", Base64.encodeToString(subShellJson.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(shellJson)


            //21、一些exec相关的内容
            val executor = ShellCommandExecutor()

            val execJson = JSONObject();
            execJson.put("name", "exec sh相关") ;
            var subExecJson = JSONObject()
            subExecJson.put("exec sh -c pm path com.tencent.mm", executor.getPackagePath("com.tencent.mm"))
            subExecJson.put("exec sh -c ps | grep adbd", ProcessGrep().executeShellCommandAlternative())
            subExecJson.put("exec sh -c pm path com.xiaomi.market", executor.getPackagePath("com.xiaomi.market"))
            subExecJson.put("exec pm list instrumentation", InstrumentationUtil().getInstrumentationList())
            subExecJson.put("exec sh -c pm path com.android.vending", executor.getPackagePath("com.android.vending"))
            execJson.put("data", Base64.encodeToString(subExecJson.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(execJson)


            //22、通过JNI读取内容

//            Log.d("sb" , "get Cname info = "+DrmIdFetcher.getCnameInfoHex())
            val bootTime: LongArray? = DrmIdFetcher.getBootTime()
            System.out.println("Boot Time: " + bootTime?.get(0) + " seconds, " + bootTime?.get(1) + " nanoseconds");
            val jniJson = JSONObject();

            val jniDataInfo  = JSONObject();
            jniDataInfo.put("Cname info - Hex", DrmIdFetcher.getCnameInfoHex())
            jniDataInfo.put("Boot Time - seconds", bootTime?.get(0))
            jniDataInfo.put("Boot Time - nanoseconds", bootTime?.get(1))


            val paths = listOf(
                "/data", "/odm", "/odm_dlkm", "/product",
                "/system", "/system_ext", "/vendor", "/vendor_dlkm"
            )
            val statfs64JSON = JSONObject()
            paths.forEach { path ->
                try {
                    val hexOutput = DrmIdFetcher.getStatFsInfo(path)
                    println("Path: $path")
                    println("StatFs64 Hex Dump:\n$hexOutput")
                    statfs64JSON.put(path, hexOutput)
                } catch (e: Exception) {
                    println("Failed to fetch statfs64 info for path: $path")
                    println("Error: ${e.message}")
                }
            }
            jniDataInfo.put("statfs64", statfs64JSON)


            jniJson.put("name", "通过JNI读取Cname + BootTime") ;
            jniJson.put("data", Base64.encodeToString(jniDataInfo.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(jniJson)




//            //22、读取CPU和battery
//            val cpu_batteryJson = JSONObject();
//            cpu_batteryJson.put("name", "CPU和Battery") ;
//            var sub_cpu_batteryJson = JSONObject()
//            sub_cpu_batteryJson.put("/sys/devices/system/cpu", CpuReader.readCpuDevices())
//            sub_cpu_batteryJson.put("/sys/class/power_supply/battery", BatteryReader.readBatteryInfo())
//            cpu_batteryJson.put("data", Base64.encodeToString(sub_cpu_batteryJson.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
//            uploadJsonArray.put(cpu_batteryJson)


            //23、读取SIM卡
            // 获取运营商信息 , 获取所有属性并以JSON格式输出
                val propertyCollector = TelephonyPropertyCollector(this@MainActivity)
            val jsonResult = propertyCollector.getTelephonyPropertiesJson()
            val simJson = JSONObject();
            simJson.put("name", "SIM卡信息相关") ;
            simJson.put("data", Base64.encodeToString(jsonResult.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(simJson)



            //18.https://www.cnblogs.com/sishuiliuyun/p/3245599.html
            try {
                val sishuiliuyunJson = JSONObject();
                sishuiliuyunJson.put("name", "sishuiliuyun-系统相关属性") ;
                val sishuiliuyun = Gson().toJson(sishuiliuyunCpuManager().getInfo(this@MainActivity))
                Log.d("sb", "sishuiliuyun = $sishuiliuyun")
                sishuiliuyunJson.put("data", Base64.encodeToString(sishuiliuyun.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
                uploadJsonArray.put(sishuiliuyunJson)
            }catch (e: Exception){
                e.printStackTrace()
            }





            //19.读取/proc/stat下的所有内容:没有权限
//            Log.d("sb", "ProcStatReader.readProcStat() = " + ReadProcStat.getInfo())


            val allDataFileNameSuffix = Build.MODEL + "_" + Utils.getCurrentDateTime()
                val allDataFileName = allDataFileNameSuffix + "_" + "allData.txt"
            val uploadTxTtoServerState = "开始保存数据到本地，文件名称是$allDataFileName————————>"
                runOnUiThread {
            uploadStatus.text = uploadTxTtoServerState
                }
                var externalDir111 = this@MainActivity.filesDir ;

//                val externalDir111 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "nest")
//                if (externalDir111.exists()) {
//                    if (externalDir111.deleteRecursively()) {
//                        Toast.makeText(this@MainActivity, "目录已经存在，先删除这个文件原有文件", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                if (!externalDir111.exists()) {
//                    if (!externalDir111.mkdirs()) {
//                        Toast.makeText(this@MainActivity, "无法创建本地目标目录: ${externalDir111.absolutePath}", Toast.LENGTH_LONG).show()
//                        return@launch
//                    }
//                }

                //写入
            java.io.File("$externalDir111/$allDataFileName").writeText(uploadJsonArray.toString())



            //20.NDK - 传感器
                runOnUiThread {
                    uploadStatus.text = "开始收集传感器文件..."
                }
            val sensorFileDir = File(externalDir111.absolutePath)

                delay(3000)

                //21.唐哥要求的本地文件
                // 使用协程处理延迟和文件操作
                CoroutineScope(Dispatchers.IO).launch {
//                    mCpuFilesCopier = CpuFilesCopier(this@MainActivity, File(externalDir111.absolutePath + "/cpu"))


                    runOnUiThread {
                        uploadStatus.text = "开始创建download/nest/cpu..."
                    }
//                    val externalCPUDir111 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "nest")
//                    if (!externalCPUDir111.exists()) {
//                        if (!externalCPUDir111.mkdirs()) {
//                            Toast.makeText(this@MainActivity, "无法创建目标目录: ${externalCPUDir111.absolutePath}", Toast.LENGTH_SHORT).show()
//                            return@launch
//                        }
//                    }
                    mCpuFilesCopier = CpuFilesCopier(this@MainActivity, lastCPUTargetDir)
                    mTempFilesCopier = TempFilesCopier(this@MainActivity, lastTempTargetDir)

                    loadRemperatureUtils.saveTemperatureInfo(File(externalDir111.absolutePath ))





                    mPower_SupplyFilesCopier = Power_SupplyFilesCopier(this@MainActivity, File(externalDir111.absolutePath + "/power_supply"))
                    withContext(Dispatchers.IO) {
                        runOnUiThread {
                            uploadStatus.text = "开始复制cpu，电池等文件到本地...."
                        }

                        runOnUiThread {
                            uploadStatus.text = "开始执行Runtime.getRuntime().exec(arrayOf(\"sh\", \"-c\", \"/system/bin/getprop\"))"
                        }
                        ShellGetProp.saveSystemPropsToFile(File(externalDir111.absolutePath ))

                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/stat -f"
                        }
                        ShellGetStat_F_Data.saveSystemPropsToFile(File(externalDir111.absolutePath))
                        ShellGetStat_F_Odm.saveSystemPropsToFile(File(externalDir111.absolutePath))
                        ShellGetStat_F_Odm_dlkm.saveSystemPropsToFile(File(externalDir111.absolutePath))
                        ShellGetStat_F_Product.saveSystemPropsToFile(File(externalDir111.absolutePath))
                        ShellGetStat_F_System_ext.saveSystemPropsToFile(File(externalDir111.absolutePath))
                        ShellGetStat_F_Vendor.saveSystemPropsToFile(File(externalDir111.absolutePath))


                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/self/cgroup\")"
                        }
                        ShellGetCgroup.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "Runtime.getRuntime().exec(arrayOf(\"sh\", \"-c\", \"am get-config\"))"
                        }
                        Shell_AM_GetConfig.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "Runtime.getRuntime().exec(arrayOf(\"sh\", \"-c\", \"pm list features\"))"
                        }
                        Shell_PM_List_Features.saveSystemPropsToFile(File(externalDir111.absolutePath))


                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/mounts\")"
                        }
                        ShellGetMounts.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/diskstats\")"
                        }
                        ShellGetDiskstats.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/cpuinfo\")"
                        }
                        ShellGetCpuInfo.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/meminfo\")"
                        }
                        ShellGetMeminfo.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行File(\"/proc/version\")"
                        }
                        ShellGetLinuxVersion.saveVersionToFile(File(externalDir111.absolutePath))


                        runOnUiThread {
                            uploadStatus.text = "开始执行getCnameInfoHex"
                        }
                        ShellGetKernel.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行Runtime.getRuntime().exec(\"lspci\")"
                        }
                        Shell_lspci.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行Runtime.getRuntime().exec(\"lsusb\")"
                        }
                        Shell_lsusb.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行Runtime.getRuntime().exec(\"lshal\")"
                        }
                        Shell_lshal.saveSystemPropsToFile(File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行GetCameraInfo"
                        }
                        GetCameraInfo.saveSystemPropsToFile(this@MainActivity, File(externalDir111.absolutePath))

                        runOnUiThread {
                            uploadStatus.text = "开始执行cp /data/local/tmp/camera.txt"
                        }
                        ShellCommandExecutor().executeShellCommand("cp /data/local/tmp/camera.txt ${externalDir111.absolutePath}/camera.txt")


                        //电池信息
                        val batteryInfo = Batteryutils.getAllBatteryInfo(this@MainActivity, File(externalDir111.absolutePath))
                        Log.d("BatteryInfo", "═══════════════ 电池基本信息 ═══════════════")
                        batteryInfo.forEach { (key, value) ->
                            Log.d("BatteryInfo", "$key = $value")
                        }


                        if (isCpuCapturing) {
                            val sourceDir = File("/sys/devices/system/cpu")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "sourceDir存在 =" +sourceDir.exists() , Toast.LENGTH_SHORT).show()
                            }
                            if (sourceDir.exists()) {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "cpu文件个数="+mCpuFilesCopier.countFilesInDirectory(sourceDir), Toast.LENGTH_SHORT).show()
                                    uploadStatus.text = "开始执行mCpuFilesCopier.copyCpuFiles()"
                                }
                                val resultJson = mCpuFilesCopier.copyCpuFiles()
                                runOnUiThread {
                                    copyCPUResult.text = resultJson
                                    uploadStatus.text = "执行mCpuFilesCopier结果 = $resultJson"
                                    Thread.sleep(3000)
                                }
                            }else{
                                runOnUiThread {
                                    uploadStatus.text = "/sys/devices/system/cpu 路径不存在，跳过CPU复制"
                                    Thread.sleep(2000)
                                }
                            }


                            //温度文件拷贝
                            runOnUiThread {
                                uploadStatus.text = "开始执行mTempFilesCopier.copyFiles()"
                            }
                            mTempFilesCopier.copyFiles()


                        }else{
                            runOnUiThread {
                                uploadStatus.text = "不需要上传cpu文件，跳过."
                                Thread.sleep(3000)
                            }
                        }

                        //显卡JSON文件
                        DisplayCard.saveDisplayCardInfo(File(externalDir111.absolutePath))


                        runOnUiThread {
                            uploadStatus.text = "开始执行mPower_SupplyFilesCopier.copyPower_SupplyFiles()"
                        }
                        mPower_SupplyFilesCopier.copyPower_SupplyFiles()

                    }
                    delay(5000)
                    UploadData.printAllFiles(sensorFileDir.absolutePath)


                    //开始检查Sensor文件的长度
                    val sensorFilePath = java.io.File("$externalDir111")
                    runOnUiThread {
                        val checkSensorJSON = CheckSensorLength.check(sensorFilePath)
                        Log.d("sb", "checkSensorJSON = $checkSensorJSON")

                        val checkSensorFailedCount = checkSensorJSON.get("failedCount")
                        val sumSensorFailedCount = checkSensorJSON.get("totalCheckedFiles")

                        if (checkSensorFailedCount == 0){
                            checkSensorText.text = "检查$sumSensorFailedCount 个Sensor文件，所有的Sensor文件长度都符合要求"
                            checkSensorText.setTextColor(android.graphics.Color.GREEN);
                        }else{
                            checkSensorText.text = checkSensorJSON.toString()
                            checkSensorText.setTextColor(android.graphics.Color.RED);

                        }
                    }


                    // 继续上传操作
            Log.d("sb", "uploadTxTtoServerState  = $uploadTxTtoServerState")
            Log.d("sb", "uploadTxTtoServerState externalDir = $externalDir111")

                    Log.d("sb", "UploadData.upload start....")
                    runOnUiThread {
                        uploadStatus.text = "开始准备上传文件到服务器..."
                    }


                    //把externalDir111下面的整个目录拷贝到download/nest下面去,然后上传
                    val sourceDir = File(externalDir111.absolutePath)

                    try {
                        // 确保目标目录存在
                        if (!lastTargetDir.exists()) {
                            if (!lastTargetDir.mkdirs()) {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "无法创建目标目录: ${lastTargetDir.absolutePath}", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }
                        }
                        
                        // 复制所有文件
                        sourceDir.listFiles()?.forEach { sourceFile ->
                            val targetFile = File(lastTargetDir, sourceFile.name)
                            if (sourceFile.isDirectory) {
                                // 如果是目录，递归复制
                                copyDirectory(sourceFile, targetFile)
                            } else {
                                // 如果是文件，直接复制
                                copyFile(sourceFile, targetFile)
                            }
                        }
                        
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "文件已复制到: ${lastTargetDir.absolutePath}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "复制文件时出错: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    UploadData.upload(this@MainActivity, lastTargetDir.absolutePath, allDataFileNameSuffix,
                        onSuccess = {
            // 在主线程更新UI
                            runOnUiThread {
                                uploadStatus.text = "已经上传数据到服务器，文件名称是$allDataFileNameSuffix.zip————————>"
                            }
                        },
                        onFailure = { errorMsg ->
                            // 在主线程更新UI
                            runOnUiThread {
                                if (!errorMsg.contains("Expected a string but was BEGIN_OBJECT")) {
                                    uploadStatus.text = "上传失败: $errorMsg————————>"
                                }else{
                                    uploadStatus.text = "已经上传数据到服务器，文件名称是$allDataFileNameSuffix.zip————————>"
                                }
                            }
                        }
                    )


                }

            }
        }

        findViewById<Button>(R.id.stop_sensor_btn).setOnClickListener {
            testor.stop()
        }



    }


    fun saveSensorList(context: Context, sensorFileDir: String){

        var sm: SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        var sensors = sm!!.getSensorList(Sensor.TYPE_ALL)
        var x = ArrayList<SensorInfo>()
        for (sensor in sensors){
            Log.d("sb", "sensor=${sensor.name}, handle=${testor.getSensorHandle(sensor.name, sensor.type)}")
            x.add(
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
                    sensor.version
                )
            )
        }

        // write json to externaldir/sensors.txt
        var json = Gson().toJson(x)
        Log.d("sb", "json=${json}")
        java.io.File("$sensorFileDir/sensors.txt").writeText(json)






    }


    fun hasReadPermission(path: String): Boolean {
        val file = File(path)
        return file.canRead() && file.canExecute()  // 确保具有读取和执行权限
    }

//    fun readDirectoryEntries(path: String) : ByteArray?{
//        val file = File(path)
//
//        // 使用 ParcelFileDescriptor 打开文件
//        val parcelFd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//        val fd = parcelFd.fd  // 获取文件描述符的整数值
//
//        val bufferSize = 4096
//        val result = NativeLib.getDents64(fd, bufferSize)  // 调用 Native 方法
//
//        if (result != null) {
//            println("Directory entries read successfully.")
//        } else {
//            println("Failed to read directory entries.")
//        }
//
//        parcelFd.close()  // 关闭文件描述符
//
//
//        var v_result = result?.toString(StandardCharsets.UTF_8)
//        println("Directory entries read successfully v_result = " + v_result)
//
//        return result
//    }


    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            Log.d("MainActivity", "新增显示器，ID: $displayId")
            updateDisplaysInfo()
            Log.d("MainActivity", "updateDisplaysInfo() =  " + updateDisplaysInfo())
        }

        override fun onDisplayRemoved(displayId: Int) {
            Log.d("MainActivity", "移除显示器，ID: $displayId")
            Log.d("MainActivity", "updateDisplaysInfo() =  " + updateDisplaysInfo())
        }

        override fun onDisplayChanged(displayId: Int) {
            Log.d("MainActivity", "显示器变化，ID: $displayId")
            Log.d("MainActivity", "updateDisplaysInfo() =  " + updateDisplaysInfo())
        }
    }

    private fun updateDisplaysInfo() : JSONObject{

        val jsonObject = JSONObject()


        // 获取显示模式
        val displayMode = ScreenUtils(this).getDisplayMode()

        val info = buildString {
            appendLine("═══════════════ 显示状态 ═══════════════")
            appendLine("屏幕镜像状态: ${if (displayMode.isScreenMirroring) "正在进行屏幕镜像" else "未进行屏幕镜像"}")
            appendLine()

            // 显示主屏信息
            val (main, secondary) = ScreenUtils(this@MainActivity).getScreensInfo()
            appendLine("═══════════════ 主屏信息 ═══════════════")
            appendLine(main.toString())

            jsonObject.put("主屏", main.toString())
            jsonObject.put("主屏正在scrcpy或其他工具进行镜像显示", displayMode.isScreenMirroring)

            // 显示副屏信息
            appendLine("\n═══════════════ 副屏信息 ═══════════════")
            if (secondary.isEmpty()) {
                appendLine("当前未连接副屏")
                if (displayMode.isScreenMirroring) {
                    appendLine("(检测到屏幕正在通过scrcpy或其他工具进行镜像显示)")
                }
            } else {
                secondary.forEachIndexed { index, screen ->
                    if (index > 0) appendLine("\n——————— 副屏 ${index + 1} ———————")
                    appendLine(screen.toString())
                    jsonObject.put("副屏 - $index", screen.toString())
                }
            }
        }


        // 打印到日志
        Log.d("DisplayInfo", info)

        return jsonObject
    }

    // 添加复制目录的辅助函数
    private fun copyDirectory(sourceDir: File, targetDir: File) {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        
        sourceDir.listFiles()?.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            if (sourceFile.isDirectory) {
                copyDirectory(sourceFile, targetFile)
            } else {
//                copyFile(sourceFile, targetFile)
                sourceFile.copyTo(targetFile, overwrite = true)
            }
        }
    }

    // 添加复制单个文件的辅助函数
    fun copyFile(sourceFile: File, targetFile: File) {
        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytes = input.read(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FileCopy", "复制文件失败: ${e.message}")
            throw e
        }
    }
}