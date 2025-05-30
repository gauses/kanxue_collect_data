package com.nest.kanxue.core

import android.opengl.GLES20
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.util.Log
import java.security.MessageDigest
import android.util.Base64

class GpuFingerprinter {
    companion object {
        private const val TAG = "GpuFingerprinter"

        fun generateGpuFingerprint(): String {
            var eglDisplay: EGLDisplay? = null
            var eglContext: EGLContext? = null
            var eglConfig: EGLConfig? = null
            var eglSurface: EGLSurface? = null

            try {
                // 1. 初始化 EGL
                eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                    throw RuntimeException("Failed to get EGL display")
                }

                val version = IntArray(2)
                if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                    throw RuntimeException("Failed to initialize EGL")
                }

                // 2. 配置 EGL
                val configAttribs = intArrayOf(
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE
                )

                val configs = arrayOfNulls<EGLConfig>(1)
                val numConfigs = IntArray(1)
                if (!EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, configs.size, numConfigs, 0)) {
                    throw RuntimeException("Failed to choose EGL config")
                }
                eglConfig = configs[0]

                // 3. 创建 EGL 上下文
                val contextAttribs = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
                )
                eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
                if (eglContext == EGL14.EGL_NO_CONTEXT) {
                    throw RuntimeException("Failed to create EGL context")
                }

                // 4. 创建离屏渲染表面
                val surfaceAttribs = intArrayOf(
                    EGL14.EGL_WIDTH, 1,
                    EGL14.EGL_HEIGHT, 1,
                    EGL14.EGL_NONE
                )
                eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
                if (eglSurface == EGL14.EGL_NO_SURFACE) {
                    throw RuntimeException("Failed to create EGL surface")
                }

                // 5. 绑定上下文和表面
                if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                    throw RuntimeException("Failed to make EGL context current")
                }

                // 6. 收集 GPU 信息
                val gpuInfo = StringBuilder()
                
                // 获取显卡信息
                gpuInfo.append("Vendor: ${GLES20.glGetString(GLES20.GL_VENDOR)}\n")
                gpuInfo.append("Renderer: ${GLES20.glGetString(GLES20.GL_RENDERER)}\n")
                gpuInfo.append("Version: ${GLES20.glGetString(GLES20.GL_VERSION)}\n")
                gpuInfo.append("Extensions: ${GLES20.glGetString(GLES20.GL_EXTENSIONS)}\n")

                // 获取支持的着色器版本
                val maxVertexAttribs = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0)
                gpuInfo.append("Max Vertex Attribs: ${maxVertexAttribs[0]}\n")

                val maxVertexUniformVectors = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS, maxVertexUniformVectors, 0)
                gpuInfo.append("Max Vertex Uniform Vectors: ${maxVertexUniformVectors[0]}\n")

                val maxVaryingVectors = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, maxVaryingVectors, 0)
                gpuInfo.append("Max Varying Vectors: ${maxVaryingVectors[0]}\n")

                val maxCombinedTextureImageUnits = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, maxCombinedTextureImageUnits, 0)
                gpuInfo.append("Max Combined Texture Image Units: ${maxCombinedTextureImageUnits[0]}\n")

                val maxVertexTextureImageUnits = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, maxVertexTextureImageUnits, 0)
                gpuInfo.append("Max Vertex Texture Image Units: ${maxVertexTextureImageUnits[0]}\n")

                val maxTextureImageUnits = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, maxTextureImageUnits, 0)
                gpuInfo.append("Max Texture Image Units: ${maxTextureImageUnits[0]}\n")

                val maxFragmentUniformVectors = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS, maxFragmentUniformVectors, 0)
                gpuInfo.append("Max Fragment Uniform Vectors: ${maxFragmentUniformVectors[0]}\n")

                // 7. 计算哈希值
                val gpuInfoString = gpuInfo.toString()
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(gpuInfoString.toByteArray())
                return Base64.encodeToString(hashBytes, Base64.NO_WRAP)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating GPU fingerprint", e)
                throw RuntimeException("Failed to generate GPU fingerprint", e)
            } finally {
                // 8. 清理资源
                eglDisplay?.let { display ->
                    EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                    eglSurface?.let { EGL14.eglDestroySurface(display, it) }
                    eglContext?.let { EGL14.eglDestroyContext(display, it) }
                    EGL14.eglTerminate(display)
                }
            }
        }
    }
} 