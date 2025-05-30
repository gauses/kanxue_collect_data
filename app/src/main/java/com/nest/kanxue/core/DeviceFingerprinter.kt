package com.nest.kanxue.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style
import android.graphics.Paint.Align

// web js fingerprint里面有一个功能是js计算显卡，webgl，声卡，clientrect指纹的几个函数，
// 使用kt在aosp12中重现类似的方法，并把几个hash只保存为一个json格式结构，给出简洁详细的源码

class DeviceFingerprinter(private val context: Context) {

    companion object {
        // WebGL fingerprinting
        fun getWebGLFingerprint(context: Context): JSONObject {
            val webglData = JSONObject()
            var display: EGLDisplay? = null
            var eglContext: EGLContext? = null
            var surface: EGLSurface? = null

            try {
                display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                if (display == EGL14.EGL_NO_DISPLAY) {
                    throw RuntimeException("Unable to get EGL14 display")
                }

                val version = IntArray(2)
                if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                    throw RuntimeException("Unable to initialize EGL14")
                }

                val configAttribs = intArrayOf(
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 16,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE
                )

                val configs = arrayOfNulls<EGLConfig>(1)
                val numConfigs = IntArray(1)
                if (!EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, configs.size, numConfigs, 0)) {
                    throw RuntimeException("Unable to find a suitable EGL14 config")
                }

                val contextAttribs = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
                )

                eglContext = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
                if (eglContext == EGL14.EGL_NO_CONTEXT) {
                    throw RuntimeException("Unable to create EGL14 context")
                }

                val surfaceAttribs = intArrayOf(
                    EGL14.EGL_WIDTH, 1,
                    EGL14.EGL_HEIGHT, 1,
                    EGL14.EGL_NONE
                )

                surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)
                if (surface == EGL14.EGL_NO_SURFACE) {
                    throw RuntimeException("Unable to create EGL14 surface")
                }

                if (!EGL14.eglMakeCurrent(display, surface, surface, eglContext)) {
                    throw RuntimeException("Unable to make EGL14 context current")
                }

                // 获取 OpenGL ES 信息
                val glRenderer = GLES20.glGetString(GLES20.GL_RENDERER)
                val glVendor = GLES20.glGetString(GLES20.GL_VENDOR)
                val glVersion = GLES20.glGetString(GLES20.GL_VERSION)
                val glslVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)
                val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)
                val maxTextureSize = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)

                webglData.put("renderer", glRenderer?.hashCode() ?: 0)
                webglData.put("vendor", glVendor?.hashCode() ?: 0)
                webglData.put("version", glVersion?.hashCode() ?: 0)
                webglData.put("shadingLanguageVersion", glslVersion?.hashCode() ?: 0)
                webglData.put("extensions", extensions?.hashCode() ?: 0)
                webglData.put("maxTextureSize", maxTextureSize[0])

                // Canvas fingerprint
                val canvasFingerprint = getCanvasFingerprint()
                webglData.put("canvas", canvasFingerprint)

            } catch (e: Exception) {
                Log.e("WebGL", "Error in WebGL setup: ${e.message}")
                webglData.put("error", "WebGL setup failed: ${e.message}")
            } finally {
                try {
                    EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                    EGL14.eglDestroySurface(display, surface)
                    EGL14.eglDestroyContext(display, eglContext)
                    EGL14.eglTerminate(display)
                } catch (e: Exception) {
                    Log.e("WebGL", "Error cleaning up EGL14: ${e.message}")
                }
            }

            return webglData
        }

        // Canvas fingerprinting
        private fun getCanvasFingerprint(): Int {
            val width = 200
            val height = 200
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(ANTI_ALIAS_FLAG)

            // 设置背景
            canvas.drawColor(Color.WHITE)

            // 绘制文本
            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Align.CENTER
            canvas.drawText("Canvas Fingerprint", width / 2f, height / 2f, paint)

            // 绘制一些图形
            paint.style = Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(Rect(10, 10, width - 10, height - 10), paint)
            canvas.drawCircle(width / 2f, height / 2f, 50f, paint)

            // 计算哈希值
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val hash = pixels.contentHashCode()

            // 清理资源
            bitmap.recycle()

            return hash
        }

        // Audio fingerprinting
        fun getAudioFingerprint(context: Context): JSONObject {
            val audioData = JSONObject()
            
            // 检查录音权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                audioData.put("error", "No RECORD_AUDIO permission")
                return audioData
            }

            var mediaRecorder: MediaRecorder? = null
            var outputFile: File? = null

            try {
                // 创建临时文件
                outputFile = File(context.cacheDir, "audio_fingerprint.tmp")
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(outputFile.absolutePath)
                }

                try {
                    mediaRecorder.prepare()
                    mediaRecorder.start()
                    
                    // 录制一小段音频
                    Thread.sleep(1000)
                    
                    mediaRecorder.stop()
                    mediaRecorder.release()
                    mediaRecorder = null

                    // 读取录制的文件并计算哈希
                    val fileBytes = outputFile.readBytes()
                    val md = MessageDigest.getInstance("SHA-256")
                    val hash = md.digest(fileBytes)
                    
                    // 将哈希值转换为整数
                    val hashInt = ByteBuffer.wrap(hash).int
                    
                    audioData.put("hash", hashInt)
                    audioData.put("fileSize", fileBytes.size)
                    audioData.put("format", "AAC")
                    audioData.put("sampleRate", 44100)
                    audioData.put("bitRate", 128000)

                } catch (e: Exception) {
                    Log.e("Audio", "MediaRecorder failed: ${e.message}")
                    audioData.put("error", "MediaRecorder failed: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("Audio", "Audio setup failed: ${e.message}")
                audioData.put("error", "Audio setup failed: ${e.message}")
            } finally {
                try {
                    mediaRecorder?.release()
                } catch (e: Exception) {
                    Log.e("Audio", "Error releasing MediaRecorder: ${e.message}")
                }
                
                try {
                    outputFile?.delete()
                } catch (e: Exception) {
                    Log.e("Audio", "Error deleting temp file: ${e.message}")
                }
            }

            return audioData
        }

        // Client rect fingerprinting
        fun getClientRectFingerprint(context: Context, view: View): JSONObject {
            val rectData = JSONObject()
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            try {
                val location = IntArray(2)
                view.getLocationOnScreen(location)

                rectData.put("width", view.width)
                rectData.put("height", view.height)
                rectData.put("x", location[0])
                rectData.put("y", location[1])
                rectData.put("density", displayMetrics.density)
                rectData.put("densityDpi", displayMetrics.densityDpi)
            } catch (e: Exception) {
                rectData.put("error", "Client rect failed: ${e.message}")
            }

            return rectData
        }

        // Combine all fingerprints into one JSON
        fun getCombinedFingerprint(context: Context, view: View): JSONObject {
            val fingerprint = JSONObject()

            fingerprint.put("webgl", getWebGLFingerprint(context))
            fingerprint.put("audio", getAudioFingerprint(context))
            fingerprint.put("clientRect", getClientRectFingerprint(context, view))
            fingerprint.put("timestamp", System.currentTimeMillis())

            return fingerprint
        }
    }
}