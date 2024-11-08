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
import com.google.gson.Gson
import com.nest.kanxue.Dents64.parseDents64.parseDents64
import com.nest.kanxue.HOOK.Xposed
import com.nest.kanxue_data.R
import com.nest.kanxue_data.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.File.separator
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val button = findViewById<Button>(R.id.stat_file_btn)
        // 设置点击事件
        button.setOnClickListener {
            // 在按钮点击时执行的代码

            //1， statFile
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
            uploadJsonArray.put(statJson)


            //2，readDirectoryEntries来调用,获取某一目录下的文件和文件夹
            val dents64Array = JSONArray();
            stat_file_path.forEach { fileName ->
                if (File(fileName).exists() && File(fileName).canRead()) {
                    var byteArray = readDirectoryEntries(fileName)
                    dents64Array.put(JSONObject().put(fileName,
                        java.lang.String(byteArray, StandardCharsets.UTF_8)
                    ))
                }
            }

            val getDents64JSON = JSONObject();
            getDents64JSON.put("name", "getDents64") ;
            getDents64JSON.put("data", Base64.encodeToString(Gson().toJson(Gson().fromJson(dents64Array.toString(), List::class.java)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            uploadJsonArray.put(getDents64JSON)



            //3.xpsoed
            Xposed.check_xposed_file_path()




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

    fun readDirectoryEntries(path: String) : ByteArray?{
        val file = File(path)

        // 使用 ParcelFileDescriptor 打开文件
        val parcelFd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val fd = parcelFd.fd  // 获取文件描述符的整数值

        val bufferSize = 4096
        val result = NativeLib.getDents64(fd, bufferSize)  // 调用 Native 方法

        if (result != null) {
            println("Directory entries read successfully.")
        } else {
            println("Failed to read directory entries.")
        }

        parcelFd.close()  // 关闭文件描述符


        var v_result = result?.toString(StandardCharsets.UTF_8)
        println("Directory entries read successfully v_result = " + v_result)

        return result
    }
}