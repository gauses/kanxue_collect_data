package com.nest.kanxue.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.nest.kanxue.core.ShellGetKernel.getKernelUsingFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

object GetCameraInfo {

    private const val TAG = "GetCameraInfo"

    fun getCameraResolutions(context: Context): JSONObject {
        val resultJson = JSONObject()

        // 检查相机权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            resultJson.put("error", "没有相机权限")
            return resultJson
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val camerasArray = JSONArray()
            
            cameraManager.cameraIdList.forEach { cameraId ->
                val cameraJson = JSONObject()
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                
                // 获取摄像头朝向
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)?.let {
                    when (it) {
                        CameraCharacteristics.LENS_FACING_FRONT -> "前置"
                        CameraCharacteristics.LENS_FACING_BACK -> "后置"
                        else -> "外部"
                    }
                } ?: "未知"
                
                cameraJson.put("cameraId", cameraId)
                cameraJson.put("facing", facing)

                // 获取支持的输出配置
                val configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                configs?.let {
                    // 拍照分辨率（JPEG）
                    val jpegSizes = configs.getOutputSizes(ImageFormat.JPEG)
                    val photoResolutions = JSONArray()
                    jpegSizes?.forEach { size ->
                        photoResolutions.put("${size.width}x${size.height}")
                    }
                    cameraJson.put("photoResolutions", photoResolutions)

                    // 预览分辨率（SurfaceTexture）
                    val previewSizes = configs.getOutputSizes(SurfaceTexture::class.java)
                    val previewResolutions = JSONArray()
                    previewSizes?.forEach { size ->
                        previewResolutions.put("${size.width}x${size.height}")
                    }
                    cameraJson.put("previewResolutions", previewResolutions)
                }
                
                camerasArray.put(cameraJson)
            }
            
            resultJson.put("cameras", camerasArray)
            Log.d("CameraResolution", "Camera info: $resultJson")
            
        } catch (e: CameraAccessException) {
            Log.e("CameraResolution", "获取相机信息失败: ${e.message}")
            resultJson.put("error", "获取相机信息失败: ${e.message}")
        }
        
        return resultJson
    }


    fun saveSystemPropsToFile(context: Context, targetDir: File) {
        try {
            val props = getCameraResolutions(context)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val cameraInfoFile = File(targetDir, "CameraInfo.txt")

            FileWriter(cameraInfoFile).use { writer ->
                writer.write(props.toString())
            }

            Log.d(TAG, "CameraInfo信息已保存到: ${cameraInfoFile.absolutePath}")
            Log.d(TAG, "文件大小: ${cameraInfoFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存CameraInfo信息失败", e)
        }
    }
}