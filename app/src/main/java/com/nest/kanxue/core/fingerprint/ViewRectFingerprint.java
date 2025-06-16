package com.nest.kanxue.core.fingerprint;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

//web js 在浏览器中计算client rect的hash特征原理是什么，使用java在手机中也要获取这个硬件特征唯一值
//基于Android View系统的实现

public class ViewRectFingerprint {
    private static final String TAG = "ViewRectFingerprint";

    public static String generateRectFingerprint(Context context) {
        try {
            // 1. 获取设备显示信息
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            
            // 2. 创建测试容器
            FrameLayout container = new FrameLayout(context);
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // 3. 添加测试视图
            List<View> testViews = createTestViews(context, metrics);
            for (View view : testViews) {
                container.addView(view);
            }

            // 4. 测量并布局
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;
            container.measure(
                    View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.EXACTLY));
            container.layout(0, 0, screenWidth, screenHeight);

            // 5. 收集测量结果
            StringBuilder rectData = new StringBuilder();
            
            // 添加设备信息
            rectData.append("Screen: ").append(screenWidth).append("x").append(screenHeight).append("\n");
            rectData.append("Density: ").append(metrics.density).append("\n");

            // 收集视图信息
            for (int i = 0; i < testViews.size(); i++) {
                View view = testViews.get(i);
                
                // 获取视图在屏幕上的位置
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                
                // 计算相对于视口的坐标
                float viewportWidth = screenWidth;
                float viewportHeight = screenHeight;
                
                // 计算相对于视口的坐标，并四舍五入到4位小数
                float left = Math.round((float)location[0] / viewportWidth * 10000) / 10000f;
                float top = Math.round((float)location[1] / viewportHeight * 10000) / 10000f;
                float right = Math.round((float)(location[0] + view.getWidth()) / viewportWidth * 10000) / 10000f;
                float bottom = Math.round((float)(location[1] + view.getHeight()) / viewportHeight * 10000) / 10000f;
                
                // 格式化坐标，确保4位小数
                rectData.append(String.format("Rect[%d]: [%.4f, %.4f, %.4f, %.4f]\n", 
                    i, left, top, right, bottom));
            }

            // 6. 计算哈希
            String result = rectData.toString();
            Log.d(TAG, "Rect data: " + result);
            return calculateHash(result);
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating fingerprint", e);
            throw new RuntimeException("Failed to generate fingerprint", e);
        }
    }

    private static List<View> createTestViews(Context context, DisplayMetrics metrics) {
        List<View> views = new ArrayList<>();
        float screenWidth = metrics.widthPixels;
        float screenHeight = metrics.heightPixels;
        
        // 创建固定数量的视图，使用固定的尺寸比例
        float[] sizes = {0.1f, 0.15f, 0.2f, 0.25f, 0.3f}; // 相对于屏幕的尺寸比例
        
        for (float size : sizes) {
            View view = new View(context);
            view.setBackgroundColor(0xFFCCCCCC); // 设置背景色
            
            // 计算视图尺寸（使用固定的宽高比）
            int width = Math.round(screenWidth * size);
            int height = Math.round(width * 0.5f); // 保持固定的宽高比 2:1
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            
            // 设置位置（使用固定的偏移比例）
            float offsetX = size * 0.1f; // 10% 的水平偏移
            float offsetY = size * 0.1f; // 10% 的垂直偏移
            
            // 使用 Math.round 确保像素对齐
            params.leftMargin = Math.round(screenWidth * offsetX);
            params.topMargin = Math.round(screenHeight * offsetY);
            
            // 设置重力，确保视图在正确的位置
            params.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
            
            view.setLayoutParams(params);
            views.add(view);
        }
        
        return views;
    }

    private static String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating hash", e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}