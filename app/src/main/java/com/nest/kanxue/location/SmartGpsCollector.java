package com.nest.kanxue.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class SmartGpsCollector {

    private static final String TAG = "SmartGpsCollector";

    private final Context context;
    private final LocationManager locationManager;
    private boolean isCollecting = false;
    private boolean gnssSupported = false;

    public SmartGpsCollector(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // ==================== 启动采集 ====================
    @SuppressLint("MissingPermission")
    public void start() {
        if (isCollecting) return;

        // 检查权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "❌ 没有ACCESS_FINE_LOCATION权限，无法启动GPS采集");
            return;
        }

        // 检查GPS是否开启
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(TAG, "❌ GPS未开启，请先开启GPS");
            return;
        }

        isCollecting = true;
        Log.i(TAG, "=== 启动智能 GPS 采集 ===");

        // 1️⃣ 启动常规位置更新
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,  // 每秒更新一次
                0,
                locationListener
        );

        // 2️⃣ 检查是否支持 GNSS 原始数据
        try {
            gnssSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                    locationManager.registerGnssMeasurementsCallback(gnssCallback);
            if (gnssSupported) {
                Log.i(TAG, "✅ 检测到 GNSS 原始数据支持，已启用 Raw 模式");
            } else {
                Log.w(TAG, "⚠️ GNSS 原始数据不支持，降级为 NMEA 模式");
                locationManager.addNmeaListener(nmeaListener);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "❌ 权限不足，无法启动 GNSS 回调");
        } catch (Exception e) {
            Log.e(TAG, "❌ 注册 GNSS 回调失败：" + e.getMessage());
            gnssSupported = false;
            locationManager.addNmeaListener(nmeaListener);
        }
    }

    // ==================== 停止采集 ====================
    public void stop() {
        if (!isCollecting) return;
        isCollecting = false;
        Log.i(TAG, "=== 停止 GPS 数据采集 ===");

        try {
            locationManager.removeUpdates(locationListener);
            if (gnssSupported) {
                locationManager.unregisterGnssMeasurementsCallback(gnssCallback);
            } else {
                locationManager.removeNmeaListener(nmeaListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "停止采集时出错: " + e.getMessage());
        }
    }

    // ==================== 普通定位监听 ====================
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // 基本位置信息
            Log.i(TAG, String.format(
                    "Location: lat=%.6f, lon=%.6f, alt=%.2f, acc=%.1f, speed=%.2f, bearing=%.2f",
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getAccuracy(),
                    location.getSpeed(),
                    location.getBearing()
            ));

            // 获取卫星信息
            if (location.getExtras() != null) {
                Bundle extras = location.getExtras();
                
                // 获取可见卫星数量
                int satellitesInView = extras.getInt("satellites", -1);
                if (satellitesInView != -1) {
                    Log.i(TAG, "可见卫星数量: " + satellitesInView);
                }
                
                // 获取用于定位的卫星数量
                int satellitesUsed = extras.getInt("satellites_used", -1);
                if (satellitesUsed != -1) {
                    Log.i(TAG, "用于定位的卫星数量: " + satellitesUsed);
                }
                
                // 获取各个卫星系统的数量
                int gpsCount = extras.getInt("gps_satellites", -1);
                int glonassCount = extras.getInt("glonass_satellites", -1);
                int beidouCount = extras.getInt("beidou_satellites", -1);
                int galileoCount = extras.getInt("galileo_satellites", -1);
                
                StringBuilder satInfo = new StringBuilder("卫星系统分布: ");
                if (gpsCount != -1) satInfo.append("GPS:").append(gpsCount).append(" ");
                if (glonassCount != -1) satInfo.append("GLONASS:").append(glonassCount).append(" ");
                if (beidouCount != -1) satInfo.append("北斗:").append(beidouCount).append(" ");
                if (galileoCount != -1) satInfo.append("Galileo:").append(galileoCount);
                
                Log.i(TAG, satInfo.toString());
            }
        }

        @Override 
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusStr;
            switch (status) {
                case LocationProvider.AVAILABLE:
                    statusStr = "可用";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    statusStr = "服务区外";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusStr = "暂时不可用";
                    break;
                default:
                    statusStr = "未知状态: " + status;
            }
            Log.i(TAG, "GPS状态变化: " + statusStr);
            
            // 打印额外信息
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.i(TAG, "GPS额外信息 - " + key + ": " + extras.get(key));
                }
            }
        }

        @Override 
        public void onProviderEnabled(@NonNull String provider) {
            Log.i(TAG, "GPS提供者已启用: " + provider);
        }

        @Override 
        public void onProviderDisabled(@NonNull String provider) {
            Log.i(TAG, "GPS提供者已禁用: " + provider);
        }
    };

    // ==================== GNSS 原始测量监听 ====================
    private final GnssMeasurementsEvent.Callback gnssCallback = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
            Log.i(TAG, "收到GNSS测量数据，卫星数量: " + eventArgs.getMeasurements().size());
            
            for (GnssMeasurement m : eventArgs.getMeasurements()) {
                int svid = m.getSvid();
                int constellation = m.getConstellationType();
                double cn0 = m.getCn0DbHz();
                double freqHz = m.hasCarrierFrequencyHz() ? m.getCarrierFrequencyHz() : -1;
                double prRate = m.getPseudorangeRateMetersPerSecond();

                // 只记录信号强度大于20的卫星
                if (cn0 > 20) {
                    Log.i(TAG, String.format(
                            "GNSS Raw: SVID=%d, CONST=%d, C/N0=%.1f, Freq=%.0fHz, PRRate=%.3f",
                            svid, constellation, cn0, freqHz, prRate
                    ));
                }
            }
        }

        @Override
        public void onStatusChanged(int status) {
            String statusStr;
            switch (status) {
                case GnssMeasurementsEvent.Callback.STATUS_NOT_SUPPORTED:
                    statusStr = "不支持";
                    break;
                case GnssMeasurementsEvent.Callback.STATUS_READY:
                    statusStr = "就绪";
                    break;
                case GnssMeasurementsEvent.Callback.STATUS_LOCATION_DISABLED:
                    statusStr = "位置服务已禁用";
                    break;
                default:
                    statusStr = "未知状态: " + status;
            }
            Log.i(TAG, "GNSS状态变化: " + statusStr);
        }
    };

    // ==================== NMEA 原始语句监听 ====================
    private final OnNmeaMessageListener nmeaListener = new OnNmeaMessageListener() {
        @Override
        public void onNmeaMessage(String message, long timestamp) {
            Log.v(TAG, "NMEA: " + message.trim());
        }
    };
}
