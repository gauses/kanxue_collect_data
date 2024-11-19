package com.nest.kanxue

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nest.kanxue.apkinstallpath.getAPKInstallPath
import com.nest.kanxue.bootid.getBootId
import com.nest.kanxue.cert.CertificateReader
import com.nest.kanxue.checkenvironment.checkHookEnvironment
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import com.nest.kanxue.devicefingerprint.getDrmId
import com.nest.kanxue.devicefingerprint.getStorageInfo
import com.nest.kanxue.devicefingerprint.getSystemProp
import com.nest.kanxue.deviceidentification.getDeviceIdentifiers
import com.nest.kanxue.hardwarerelated.CustomGLSurfaceView
import com.nest.kanxue.hardwarerelated.getHardwareRelated
import com.nest.kanxue.inputmethodlist.getInputMethodList
import com.nest.kanxue.model_system_determination.CheckSIM
import com.nest.kanxue.model_system_determination.getModelSystemDeter
import com.nest.kanxue.modifymachine.CheckInstallPackageChangerApps
import com.nest.kanxue.network.NetworkInfoReader
import com.nest.kanxue.network.getNetworkInfo
import com.nest.kanxue.root.CheckRoot
import com.nest.kanxue.screentoolandclick.CheckAutoClick
import com.nest.kanxue.simulators.CheckFileDir
import com.nest.kanxue.simulators.CheckSimulators
import com.nest.kanxue.simulators.CheckSystemProp
import com.nest.kanxue.sishuiliuyun.sishuiliuyunCpuManager
import com.nest.kanxue.statprocpath.FileStatsAdapter
import com.nest.kanxue.utils.ByteArrayConverter
import com.nest.kanxue_data.R
import com.nest.kanxue_data.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {


    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var confirmButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var statsAdapter: FileStatsAdapter



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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySurfaceView = CustomGLSurfaceView(this)
        val frame = findViewById<FrameLayout>(R.id.SurfaceViewFrame)
        frame.addView(mySurfaceView)


        // check has permission WRITE_EXTERNAL_STORAGE
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            onRequestPermissionsResult(
                RESULT_OK, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), intArrayOf(
                    PackageManager.PERMISSION_GRANTED));
        }else{
            // request to write external storage
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, ) == PackageManager.PERMISSION_GRANTED){
            onRequestPermissionsResult(
                RESULT_OK, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), intArrayOf(
                    PackageManager.PERMISSION_GRANTED));
        }else{
            // request to write external storage
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }



        // check has permission READ_PHONE_STATE
        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            onRequestPermissionsResult(
                RESULT_OK, arrayOf(android.Manifest.permission.READ_PHONE_STATE), intArrayOf(
                    PackageManager.PERMISSION_GRANTED));
        }else{
            // request to write external storage
            requestPermissions(arrayOf(android.Manifest.permission.READ_PHONE_STATE), 0)
        }

        val uploadStatus = findViewById<TextView>(R.id.uploadStatusText)

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
                if (!s.toString().startsWith("/proc")) {
                    autoCompleteTextView.setText("/proc/${s.toString()}")
                    autoCompleteTextView.setSelection(autoCompleteTextView.text.length)
                }
            }
        })


        confirmButton = findViewById(R.id.confirmButton)
        recyclerView = findViewById(R.id.recyclerView)
        autoCompleteTextView.setText("/proc/")
        // 设置 RecyclerView
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
                    statsAdapter.submitList(fileList)
                }
            }
        }

        val testbutton = findViewById<Button>(R.id.test)
        testbutton.setOnClickListener{


            stat_file_path.forEach { fileName ->
                val fileStat = Stat_File_Utils.getFileStat(fileName)
                Log.d("sb" , "getFileStat getFileStat= "+fileName)
                Log.d("sb" , "getFileStat getFileStat= "+fileStat)
                Log.d("sb" , "getFileStat getFileStat================")
            }



            Log.d("sb" , "getCgroupUsingCat= "+getBootId.getCgroupUsingFile())
            uploadStatus.text = getBootId.getCgroupUsingFile()

//            lifecycleScope.launch(Dispatchers.IO) {
//                Log.d("sb" , "getNetworkInfo = "+getNetworkInfo.getInfo(this@MainActivity))
//            }

            // 在 Activity 或 Fragment 中使用
            val certificateReader = CertificateReader()

//            // 读取系统证书
//            val systemCerts = certificateReader.readSystemCertificates()
//            systemCerts.forEach { cert ->
//                println("证书别名: ${cert.alias}")
//                println("主题: ${cert.subject}")
//                println("颁发者: ${cert.issuer}")
//                println("有效期从: ${cert.validFrom}")
//                println("有效期至: ${cert.validTo}")
//                println("序列号: ${cert.serialNumber}")
//                println("版本: ${cert.version}")
//                println("文件路径: ${cert.path}")
//                println("----------------")
//            }

            // 读取用户安装的证书
            val userCerts = certificateReader.readUserCertificates()
            println("证书: ${userCerts.size}")
            userCerts.forEach { cert ->
                println("证书别名: ${cert.alias}")
                println("主题: ${cert.subject}")
                println("颁发者: ${cert.issuer}")
                println("有效期从: ${cert.validFrom}")
                println("有效期至: ${cert.validTo}")
                println("序列号: ${cert.serialNumber}")
                println("版本: ${cert.version}")
                println("文件路径: ${cert.path}")
                println("----------------")
            }


//
//            Log.d("sb" , "getAPKPath = "+ getAPKInstallPath.getAPKPath(this))
//            Log.d("sb" , "getInputMethodList = "+ getInputMethodList.getInfo(this))
//            Log.d("sb" , "checkHookEnvironment = "+ checkHookEnvironment.getInfo())
//            Log.d("sb" , "CheckBrandOS = "+ getModelSystemDeter.getInfo(this))
//            Log.d("sb" , "CheckSIM = "+ CheckSIM.getSimOperator(this))
//
//            Log.d("sb" , "CheckAutoClick = "+ CheckAutoClick.getInfo(this))
//            Log.d("sb" , "CheckSystemProp = "+ CheckSystemProp.checkEmulatorPropsWithGetprop())
//            Log.d("sb" , "CheckFileDir = "+ CheckFileDir.checkEmulatorFiles())
//            Log.d("sb" , "CheckSimulators = "+ CheckSimulators.getInfo(this))
//            Log.d("sb" , "CheckInstallPackage = "+ CheckInstallPackageChangerApps.getInfo(this))
//            Log.d("sb" , " Build.getSerial()  = "+ Build.SERIAL )
//
//
//            Log.d("sb" , "getDrmId = "+ Base64.encodeToString(getDrmId.getDrmId(), Base64.DEFAULT))
//                val DrmId = getDrmId.getDrmId()
//                with(ByteArrayConverter) {
//                    // 1. 转换成十六进制
//                    println("DrmId Hex: ${DrmId?.toHexString()}")
//                    // 输出: 48656c6c6f
//
//                    val result = StringBuilder(DrmId!!.size * 2)
//                    DrmId!!.forEach { byte ->
//                        result.append(String.format("%02x", byte))
//                    }
//                    println("result result: ${DrmId?.toHexString()}")
//
//
//
//                }


//            // 在后台线程中读取文件
//            lifecycleScope.launch(Dispatchers.IO) {
//                val reader = com.nest.kanxue.devicefingerprint.DrmIdFetcher.readCompatible()
//                Log.d("sb" , "readCompatible = $reader")
//
////                // 在主线程更新UI
////                withContext(Dispatchers.Main) {
////                    findViewById<TextView>(R.id.textView).text = content
////                }
//            }



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
            // 在按钮点击时执行的代码

            //2，readDirectoryEntries来调用,获取某一目录下的文件和文件夹
//            val dents64Array = JSONArray();
//            stat_file_path.forEach { fileName ->
//                if (File(fileName).exists() && File(fileName).canRead()) {
//                    var byteArray = readDirectoryEntries(fileName)
//                    dents64Array.put(JSONObject().put(fileName,
//                        java.lang.String(byteArray, StandardCharsets.UTF_8)
//                    ))
//                }
//            }
//
//            val getDents64JSON = JSONObject();
//            getDents64JSON.put("name", "getDents64") ;
//            getDents64JSON.put("data", Base64.encodeToString(Gson().toJson(Gson().fromJson(dents64Array.toString(), List::class.java)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
//            uploadJsonArray.put(getDents64JSON)


            uploadStatus.text = "开始采集sensor，等待5秒钟————————>"


            //4.设备指纹
            uploadStatus.text = "开始采集设备指纹————————>"
            val devicefingerprintson = JSONObject();
            val devicefingerprintJsonArray = JSONArray();
            devicefingerprintJsonArray.put(getStorageInfo.getstorage_emulated_0())
            devicefingerprintJsonArray.put(getSystemProp.getPropertyAllInfo())

            with(ByteArrayConverter) {
                // 1. 转换成十六进制
                val DrmId = getDrmId.getDrmId()
                devicefingerprintJsonArray.put(JSONObject().put("DRMID(已经是16进制)", DrmId?.toHexString()))
            }
//            devicefingerprintson.put("name", "devicefingerprint") ;
            devicefingerprintson.put("name", "设备指纹") ;
            val devicefingerprintList: List<*>? = Gson().fromJson(devicefingerprintJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val devicefingerprintJsonString = Gson().toJson(devicefingerprintList) // 将 List 转换为 JSON 字符串
            devicefingerprintson.put("data", Base64.encodeToString(devicefingerprintJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(devicefingerprintson)



            //5.设备标识
            uploadStatus.text = "开始采集设备标识————————>"
            val DeviceIdentifiersJson = JSONObject();
            val DeviceIdentifiersJsonArray = JSONArray();

            val statJsonArray = JSONArray();
            stat_file_path.forEach { fileName ->
                val fileStat = Stat_File_Utils.getFileStat(fileName)
                statJsonArray.put(JSONObject().put(fileName, fileStat))
            }

            val statJson = JSONObject();
            statJson.put("name", "statFile") ;
            val gson = Gson()
            val list: List<*>? = gson.fromJson(statJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val jsonString = gson.toJson(list) // 将 List 转换为 JSON 字符串
            statJson.put("data", Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
//            DeviceIdentifiersJson.put("name", "deviceIdentifiers") ;
            DeviceIdentifiersJson.put("name", "设备标识") ;
            DeviceIdentifiersJsonArray.put(statJson) ;
            DeviceIdentifiersJsonArray.put(getDeviceIdentifiers.getInfo(this)) ;
            val deviceIdentifiersList: List<*>? = Gson().fromJson(DeviceIdentifiersJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val deviceIdentifiersJsonString = Gson().toJson(deviceIdentifiersList) // 将 List 转换为 JSON 字符串
            DeviceIdentifiersJson.put("data", Base64.encodeToString(deviceIdentifiersJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(DeviceIdentifiersJson)


            //6.硬件相关
            uploadStatus.text = "开始采集硬件相关————————>"
            val hardwareJson = JSONObject();
//            hardwareJson.put("name", "HardwareRelated") ;
            hardwareJson.put("name", "硬件相关") ;
            hardwareJson.put("data", Base64.encodeToString(getHardwareRelated.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(hardwareJson)



            //7.网络相关
            uploadStatus.text = "开始采集网络相关————————>"
            val networkJson = JSONObject();
            networkJson.put("name", "网络相关") ;
            networkJson.put("data", Base64.encodeToString(getNetworkInfo.getInfo(this@MainActivity).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(networkJson)




            //8.boot id
            //cat命令读取/proc/self/mounts
            //cat命令读取/proc/sys/kernel/random/boot_id
            ///proc/meminfo
            val BootIdJson = JSONObject();
            BootIdJson.put("name", "/proc目录相关信息") ;
            BootIdJson.put("data", Base64.encodeToString(getBootId.getInfo().toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(BootIdJson)




            //9.APK Install Path
            uploadStatus.text = "开始采集APK Install Path————————>"
            val apkInstallPathJson = JSONObject();
            apkInstallPathJson.put("name", "apkInstallPath") ;
            apkInstallPathJson.put("data", Base64.encodeToString(getAPKInstallPath.getAPKPath(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(apkInstallPathJson)


            //10.输入法
            val InputMethodListJson = JSONObject();
            InputMethodListJson.put("name", "InputMethodList") ;
            InputMethodListJson.put("data", Base64.encodeToString(Gson().toJson(getInputMethodList.getInfo(this)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(InputMethodListJson)


            //11.机型和系统判定 + Bootloader解锁状态 + SIM卡
            val deviceOSListJson = JSONObject();
//            deviceOSListJson.put("name", "deviceOS") ;
            deviceOSListJson.put("name", "机型") ;
            deviceOSListJson.put("data", Base64.encodeToString(getModelSystemDeter.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(deviceOSListJson)


            //12.截图和模拟点击
            val AutoClickerJson = JSONObject();
            AutoClickerJson.put("name", "AutoClick") ;
            AutoClickerJson.put("data", Base64.encodeToString(CheckAutoClick.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(AutoClickerJson)


            //13.模拟器 :扫描常见的模拟器特征
            val simulatorsJson = JSONObject();
            simulatorsJson.put("name", "模拟器特征") ;
            simulatorsJson.put("data", Base64.encodeToString(CheckSimulators.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(simulatorsJson)

            //14.改机软件
            val chageAppsJson = JSONObject();
            chageAppsJson.put("name", "是否安装改机软件") ;
            chageAppsJson.put("data", Base64.encodeToString(CheckInstallPackageChangerApps.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(chageAppsJson)



            //15.root
            val rootJson = JSONObject();
            rootJson.put("name", "ROOT") ;
            rootJson.put("data", Base64.encodeToString(Gson().toJson(CheckRoot.getInfo(this)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(rootJson)


            //16.证书
            val certJson = JSONObject();
            certJson.put("name", "证书(System + User)") ;
            certJson.put("data", Base64.encodeToString(CertificateReader().getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(certJson)


            //16.https://www.cnblogs.com/sishuiliuyun/p/3245599.html
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



            //17.读取/proc/stat下的所有内容:没有权限
//            Log.d("sb", "ProcStatReader.readProcStat() = " + ReadProcStat.getInfo())

            val allDataFileName = Build.MODEL + "_" + Utils.getCurrentDateTime() + "_" + "allData.txt"
            val uploadTxTtoServerState = "开始保存数据到本地，文件名称是$allDataFileName————————>"
            uploadStatus.text = uploadTxTtoServerState
            var externalDir111 = this.filesDir ;
            java.io.File("$externalDir111/$allDataFileName").writeText(uploadJsonArray.toString())
            Log.d("sb", "uploadTxTtoServerState  = $uploadTxTtoServerState")
            Log.d("sb", "uploadTxTtoServerState externalDir = $externalDir111")
            UploadData.upload(this , externalDir111.path , allDataFileName, uploadJsonArray)
            Thread.sleep(3000)
            uploadStatus.text = "已经上传数据到服务器，文件名称是$allDataFileName————————>"


            // 在主线程更新UI


        }



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
}