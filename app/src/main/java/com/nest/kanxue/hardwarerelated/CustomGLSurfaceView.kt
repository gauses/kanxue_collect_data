package com.nest.kanxue.hardwarerelated
import android.content.Context
import android.opengl.GLSurfaceView


class CustomGLSurfaceView(context: Context) : GLSurfaceView(context) {


    init {
        // 设置OpenGL版本
        setEGLContextClientVersion(2) // Pick an OpenGL ES 2.0 context - OK
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        setRenderer(GPUInfoUtil())

    }
}