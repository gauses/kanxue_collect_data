package com.nest.kanxue

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import http.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object UploadData {



    fun upload(context: Context, externalDir:String, uploadJsonArray:JSONArray){


        if (externalDir.isEmpty()){
            Toast.makeText(context, "externalDir is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val allDataFileName = Build.MODEL + "_" + Utils.getCurrentDateTime() + "_" + "allData.txt"
        val uploadTxTtoServerState = "开始保存数据到本地，文件名称是$allDataFileName————————>"
        java.io.File("$externalDir/$allDataFileName").writeText(uploadJsonArray.toString())
        Log.d("sb", "uploadTxTtoServerState  = $uploadTxTtoServerState")
        Log.d("sb", "uploadTxTtoServerState externalDir = $externalDir")


        val apiService = RetrofitClient.create()
        val file = File("$externalDir/$allDataFileName")
        val requestFile = file.asRequestBody("text/plain; charset=utf-8".toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val call = apiService.uploadFile(filePart)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("sb", "File uploaded  = $response")

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("sb", "File uploaded  error= ${t.message}")

            }
        })



    }
}