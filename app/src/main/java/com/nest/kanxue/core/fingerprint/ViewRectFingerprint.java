package com.nest.kanxue.core.fingerprint;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.security.MessageDigest;

//web js 在浏览器中计算client rect的hash特征原理是什么，使用java在手机中也要获取这个硬件特征唯一值
//基于Android View系统的实现

public class ViewRectFingerprint {

    public static String generateRectFingerprint(Context context) {
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
        View[] testViews = createTestViews(context, metrics);
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
        rectData.append("Screen Density: ").append(metrics.density).append("\n");
        rectData.append("Screen Density DPI: ").append(metrics.densityDpi).append("\n");
        rectData.append("Screen Width: ").append(screenWidth).append("\n");
        rectData.append("Screen Height: ").append(screenHeight).append("\n");
        rectData.append("Scaled Density: ").append(metrics.scaledDensity).append("\n");
        rectData.append("XDpi: ").append(metrics.xdpi).append("\n");
        rectData.append("YDpi: ").append(metrics.ydpi).append("\n");

        // 收集视图信息
        for (View view : testViews) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            
            // 添加视图的完整信息
            rectData.append("View ").append(view.hashCode()).append(":\n");
            rectData.append("Global Rect: ").append(rect.flattenToString()).append("\n");
            rectData.append("Local Position: [")
                    .append(view.getLeft()).append(",")
                    .append(view.getTop()).append(",")
                    .append(view.getRight()).append(",")
                    .append(view.getBottom()).append("]\n");
            rectData.append("Translation: [")
                    .append(view.getTranslationX()).append(",")
                    .append(view.getTranslationY()).append("]\n");
            rectData.append("Scale: [")
                    .append(view.getScaleX()).append(",")
                    .append(view.getScaleY()).append("]\n");
            rectData.append("Rotation: ").append(view.getRotation()).append("\n");
            rectData.append("Pivot: [")
                    .append(view.getPivotX()).append(",")
                    .append(view.getPivotY()).append("]\n");
            rectData.append("Elevation: ").append(view.getElevation()).append("\n");
            rectData.append("Alpha: ").append(view.getAlpha()).append("\n");
        }

        // 6. 计算哈希
        return calculateHash(rectData.toString());
    }

    private static View[] createTestViews(Context context, DisplayMetrics metrics) {
        View[] views = new View[5];
        float density = metrics.density;
        
        for (int i = 0; i < views.length; i++) {
            View view = new View(context);
            
            // 使用设备密度相关的尺寸
            int baseWidth = (int)(50 * density);
            int baseHeight = (int)(25 * density);
            int margin = (int)(5 * density);
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    baseWidth + (int)(i * 10 * density),
                    baseHeight + (int)(i * 5 * density));
            params.setMargins(
                    (int)(i * margin * density),
                    (int)(i * margin * density),
                    0,
                    0);
            view.setLayoutParams(params);

            // 使用设备相关的变换
            view.setRotation(i * 5.0f);
            view.setScaleX(1.0f + (i * 0.05f));
            view.setScaleY(1.0f + (i * 0.05f));
            view.setTranslationX(i * density);
            view.setTranslationY(i * density);
            view.setElevation(i * density);
            view.setAlpha(1.0f - (i * 0.1f));

            views[i] = view;
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
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}