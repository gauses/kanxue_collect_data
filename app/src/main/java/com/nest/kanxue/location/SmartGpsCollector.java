package com.nest.kanxue.location;

package com.example.gpscollector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.*;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

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
            Log.d(TAG, String.format(
                    "Location: lat=%.6f, lon=%.6f, alt=%.2f, acc=%.1f, speed=%.2f, bearing=%.2f",
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getAccuracy(),
                    location.getSpeed(),
                    location.getBearing()
            ));
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(@NonNull String provider) {}
        @Override public void onProviderDisabled(@NonNull String provider) {}
    };

    // ==================== GNSS 原始测量监听 ====================
    private final GnssMeasurementsEvent.Callback gnssCallback = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
            for (GnssMeasurement m : eventArgs.getMeasurements()) {
                int svid = m.getSvid();
                int constellation = m.getConstellationType();
                double cn0 = m.getCn0DbHz();
                double freqHz = m.hasCarrierFrequencyHz() ? m.getCarrierFrequencyHz() : -1;
                double prRate = m.getPseudorangeRateMetersPerSecond();

                Log.d(TAG, String.format(
                        "GNSS Raw: SVID=%d, CONST=%d, C/N0=%.1f, Freq=%.0fHz, PRRate=%.3f",
                        svid, constellation, cn0, freqHz, prRate
                ));
            }
        }

        @Override
        public void onStatusChanged(int status) {
            Log.d(TAG, "GNSS 状态变化: " + status);
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
