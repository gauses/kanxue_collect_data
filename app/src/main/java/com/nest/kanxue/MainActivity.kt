package com.nest.kanxue

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import com.google.gson.Gson
import com.nest.kanxue.apkinstallpath.getAPKInstallPath
import com.nest.kanxue.bootid.getBootId
import com.nest.kanxue.checkenvironment.checkXposed
import com.nest.kanxue.devicefingerprint.getDrmId
import com.nest.kanxue.devicefingerprint.getStorageInfo
import com.nest.kanxue.devicefingerprint.getSystemProp
import com.nest.kanxue.deviceidentification.getDeviceIdentifiers
import com.nest.kanxue.hardwarerelated.CustomGLSurfaceView
import com.nest.kanxue.hardwarerelated.getHardwareRelated
import com.nest.kanxue.inputmethodlist.getInputMethodList
import com.nest.kanxue.network.getNetworkInfo
import com.nest.kanxue_data.R
import com.nest.kanxue_data.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {



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
        "/system/fonts"
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


        val testbutton = findViewById<Button>(R.id.test)
        testbutton.setOnClickListener{
            Log.d("sb" , "getHardwareRelated = "+getHardwareRelated.getInfo(this))
            Log.d("sb" , "getNetwork = "+getNetworkInfo.getInfo(this))
            Log.d("sb" , "getBootId = "+getBootId.getBootIdUsingCat())
            Log.d("sb" , "getAPKPath = "+ getAPKInstallPath.getAPKPath(this))
            Log.d("sb" , "getInputMethodList = "+ getInputMethodList.getInfo(this))



        }




        val button = findViewById<Button>(R.id.stat_file_btn)
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





            //4.设备指纹
            val devicefingerprintson = JSONObject();
            val devicefingerprintJsonArray = JSONArray();
            devicefingerprintJsonArray.put(getStorageInfo.getstorage_emulated_0())
            devicefingerprintJsonArray.put(getSystemProp.getPropertyAllInfo())
            devicefingerprintJsonArray.put(JSONObject().put("DRMID", Base64.encodeToString(getDrmId.getDrmId(), Base64.DEFAULT)))

            devicefingerprintson.put("name", "devicefingerprint") ;
            val devicefingerprintList: List<*>? = Gson().fromJson(devicefingerprintJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val devicefingerprintJsonString = Gson().toJson(devicefingerprintList) // 将 List 转换为 JSON 字符串
            devicefingerprintson.put("data", Base64.encodeToString(devicefingerprintJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(devicefingerprintson)


            //5.设备标识
            val DeviceIdentifiersJson = JSONObject();
            val DeviceIdentifiersJsonArray = JSONArray();

            val statJsonArray = JSONArray();
            stat_file_path.forEach { fileName ->
                val fileStat = Stat_File_Utils.getFileStat(fileName)
                statJsonArray.put(JSONObject().put(fileName, convertToJSONObject(fileStat)))
            }

            val statJson = JSONObject();
            statJson.put("name", "statFile") ;
            val gson = Gson()
            val list: List<*>? = gson.fromJson(statJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val jsonString = gson.toJson(list) // 将 List 转换为 JSON 字符串
            statJson.put("data", Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            DeviceIdentifiersJson.put("name", "deviceIdentifiers") ;
            DeviceIdentifiersJsonArray.put(statJson) ;
            DeviceIdentifiersJsonArray.put(getDeviceIdentifiers.getInfo(this)) ;
            val deviceIdentifiersList: List<*>? = Gson().fromJson(DeviceIdentifiersJsonArray.toString(), List::class.java) // 将 JSONArray 转换为 List
            val deviceIdentifiersJsonString = Gson().toJson(deviceIdentifiersList) // 将 List 转换为 JSON 字符串
            DeviceIdentifiersJson.put("data", Base64.encodeToString(deviceIdentifiersJsonString.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(DeviceIdentifiersJson)


            //6.硬件相关
            val hardwareJson = JSONObject();
            hardwareJson.put("name", "HardwareRelated") ;
            hardwareJson.put("data", Base64.encodeToString(getHardwareRelated.getInfo(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(hardwareJson)



            //7.网络相关



            //8.boot id
            val BootIdJson = JSONObject();
            BootIdJson.put("name", "BootId") ;
            BootIdJson.put("data", Base64.encodeToString(getBootId.getBootIdUsingCat().toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(BootIdJson)




            //9.APK Install Path
            val apkInstallPathJson = JSONObject();
            apkInstallPathJson.put("name", "apkInstallPath") ;
            apkInstallPathJson.put("data", Base64.encodeToString(getAPKInstallPath.getAPKPath(this).toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT) ) ;
            uploadJsonArray.put(apkInstallPathJson)


            //10.输入法
            val InputMethodListJson = JSONObject();
            InputMethodListJson.put("name", "InputMethodList") ;
            InputMethodListJson.put("data", Base64.encodeToString(Gson().toJson(getInputMethodList.getInfo(this)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(InputMethodListJson)


            UploadData.upload(this , externalDir , uploadJsonArray)


        }


        // check has permission WRITE_EXTERNAL_STORAGE
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            onRequestPermissionsResult(
                RESULT_OK, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), intArrayOf(
                    PackageManager.PERMISSION_GRANTED));
        }else{
            // request to write external storage
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
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