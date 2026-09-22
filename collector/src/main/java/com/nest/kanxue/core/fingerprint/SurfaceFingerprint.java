package com.nest.kanxue.core.fingerprint;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.security.MessageDigest;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//web js 在浏览器中计算client rect的hash特征原理是什么，使用java在手机中也要获取这个硬件特征唯一值
//基于SurfaceView和硬件加速的实现
public class SurfaceFingerprint extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG = "SurfaceFingerprint";
    private FingerprintCallback callback;
    private DisplayMetrics metrics;
    private boolean isFirstFrame = true;
    private boolean hasCalculated = false;

    public interface FingerprintCallback {
        void onFingerprintCalculated(String fingerprint);
    }

    public SurfaceFingerprint(Context context) {
        super(context);
        Log.d(TAG, "Constructor called with context only");
        init(context);
    }

    public SurfaceFingerprint(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Constructor called with context and attrs");
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "Initializing SurfaceFingerprint");
        try {
            // 获取设备显示信息
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            Log.d(TAG, "Display metrics initialized");

            // 配置 OpenGL ES
            setEGLContextClientVersion(2);
            setRenderer(this);
            // 修改为按需渲染模式
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            // 强制请求一次渲染
            requestRender();
            Log.d(TAG, "OpenGL ES configured and render requested");
        } catch (Exception e) {
            Log.e(TAG, "Error in init", e);
        }
    }

    public void setCallback(FingerprintCallback callback) {
        Log.d(TAG, "setCallback called");
        this.callback = callback;
        if (hasCalculated && this.callback != null) {
            Log.d(TAG, "Callback set after calculation, delivering cached fingerprint");
            this.callback.onFingerprintCalculated(lastFingerprint);
        }
    }

    private String lastFingerprint = null;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated called");
        try {
            // 设置背景色
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            Log.d(TAG, "OpenGL surface created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onSurfaceCreated", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG, "onSurfaceChanged called with width: " + width + ", height: " + height);
        try {
            GLES20.glViewport(0, 0, width, height);
            Log.d(TAG, "Viewport set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onSurfaceChanged", e);
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG, "onDrawFrame called, isFirstFrame: " + isFirstFrame + ", hasCalculated: " + hasCalculated);
        if (!isFirstFrame || hasCalculated) {
            Log.d(TAG, "Skipping frame calculation");
            return;
        }
        isFirstFrame = false;

        try {
            // 清除背景
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            Log.d(TAG, "Background cleared");

            StringBuilder gpuInfo = new StringBuilder();

            // 1. 收集 OpenGL 信息
            String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
            String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
            String version = GLES20.glGetString(GLES20.GL_VERSION);
            String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
            
            if (vendor == null || renderer == null || version == null) {
                Log.e(TAG, "OpenGL info is null - vendor: " + vendor + ", renderer: " + renderer + ", version: " + version);
                return;
            }
            
            Log.d(TAG, "OpenGL Info - Vendor: " + vendor);
            Log.d(TAG, "OpenGL Info - Renderer: " + renderer);
            Log.d(TAG, "OpenGL Info - Version: " + version);

            gpuInfo.append("Vendor: ").append(vendor).append("\n");
            gpuInfo.append("Renderer: ").append(renderer).append("\n");
            gpuInfo.append("Version: ").append(version).append("\n");
            gpuInfo.append("Extensions: ").append(extensions).append("\n");

            // 2. 收集设备显示信息
            gpuInfo.append("Screen Density: ").append(metrics.density).append("\n");
            gpuInfo.append("Screen Density DPI: ").append(metrics.densityDpi).append("\n");
            gpuInfo.append("Screen Width: ").append(metrics.widthPixels).append("\n");
            gpuInfo.append("Screen Height: ").append(metrics.heightPixels).append("\n");
            gpuInfo.append("Scaled Density: ").append(metrics.scaledDensity).append("\n");
            gpuInfo.append("XDpi: ").append(metrics.xdpi).append("\n");
            gpuInfo.append("YDpi: ").append(metrics.ydpi).append("\n");

            Log.d(TAG, "Display metrics collected");

            // 3. 收集 OpenGL 限制信息
            int[] maxTextureSize = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
            gpuInfo.append("Max Texture Size: ").append(maxTextureSize[0]).append("\n");

            int[] maxViewportDims = new int[2];
            GLES20.glGetIntegerv(GLES20.GL_MAX_VIEWPORT_DIMS, maxViewportDims, 0);
            gpuInfo.append("Max Viewport Dimensions: [")
                    .append(maxViewportDims[0]).append(", ")
                    .append(maxViewportDims[1]).append("]\n");

            // 4. 收集着色器限制
            int[] maxVertexAttribs = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0);
            gpuInfo.append("Max Vertex Attribs: ").append(maxVertexAttribs[0]).append("\n");

            int[] maxVertexUniformVectors = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS, maxVertexUniformVectors, 0);
            gpuInfo.append("Max Vertex Uniform Vectors: ").append(maxVertexUniformVectors[0]).append("\n");

            int[] maxVaryingVectors = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, maxVaryingVectors, 0);
            gpuInfo.append("Max Varying Vectors: ").append(maxVaryingVectors[0]).append("\n");

            int[] maxCombinedTextureImageUnits = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, maxCombinedTextureImageUnits, 0);
            gpuInfo.append("Max Combined Texture Image Units: ").append(maxCombinedTextureImageUnits[0]).append("\n");

            Log.d(TAG, "OpenGL limits collected");

            // 5. 计算哈希值
            String gpuInfoString = gpuInfo.toString();
            Log.d(TAG, "Calculating hash for GPU info");
            lastFingerprint = calculateHash(gpuInfoString);
            hasCalculated = true;
            
            // 6. 触发回调
            if (callback != null) {
                Log.d(TAG, "Calling callback with fingerprint");
                callback.onFingerprintCalculated(lastFingerprint);
                Log.d(TAG, "Callback completed");
            } else {
                Log.d(TAG, "Callback is null, fingerprint calculated but not delivered");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDrawFrame", e);
            e.printStackTrace();
        }
    }

    private static String calculateHash(String data) {
        try {
            Log.d(TAG, "Starting hash calculation");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String result = hexString.toString();
            Log.d(TAG, "Hash calculation completed");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Hash calculation failed", e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}