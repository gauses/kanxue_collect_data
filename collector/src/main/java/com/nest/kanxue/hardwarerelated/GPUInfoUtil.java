package com.nest.kanxue.hardwarerelated;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GPUInfoUtil implements GLSurfaceView.Renderer{

    public static String glVersion;

    public static String glRenderer;
    public static String glVendor;
    public static String glExtensions;




    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        glRenderer = gl.glGetString(GL10.GL_RENDERER);      //GPU 渲染器
        glVendor = gl.glGetString(GL10.GL_VENDOR);          //GPU 供应商
        glVersion = gl.glGetString(GL10.GL_VERSION);        //GPU 版本
        glExtensions = gl.glGetString(GL10.GL_EXTENSIONS);  //GPU 扩展名
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}