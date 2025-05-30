package com.nest.kanxue.core.fingerprint;

//web js 在浏览器中计算声卡的hash特征原理是什么，使用java在手机中也要获取这个硬件特征唯一值

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import java.security.MessageDigest;
import java.util.Arrays;

public class AudioFingerprint {
    private static final String TAG = "AudioFingerprint";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    public static String generateAudioFingerprint() {
        AudioRecord audioRecord = null;
        AudioTrack audioTrack = null;
        
        try {
            // 1. 获取音频设备信息
            StringBuilder deviceInfo = new StringBuilder();
            
            // 获取支持的采样率
            int[] sampleRates = {8000, 11025, 16000, 22050, 44100, 48000};
            for (int rate : sampleRates) {
                int bufferSize = AudioRecord.getMinBufferSize(rate, CHANNEL_CONFIG, AUDIO_FORMAT);
                if (bufferSize > 0) {
                    deviceInfo.append("Supported Sample Rate: ").append(rate).append("\n");
                }
            }
            
            // 获取音频源信息
            deviceInfo.append("Audio Source: ").append(MediaRecorder.AudioSource.MIC).append("\n");
            
            // 2. 初始化录音
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            );
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new RuntimeException("AudioRecord initialization failed");
            }
            
            // 3. 初始化播放
            audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM
            );
            
            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                throw new RuntimeException("AudioTrack initialization failed");
            }
            
            // 4. 获取音频设备延迟
            deviceInfo.append("Audio Record Buffer Size: ").append(BUFFER_SIZE).append("\n");
            deviceInfo.append("Audio Track Buffer Size: ").append(audioTrack.getBufferSizeInFrames()).append("\n");
            deviceInfo.append("Audio Track Playback Rate: ").append(audioTrack.getPlaybackRate()).append("\n");
            
            // 5. 录制一段静音并分析
            short[] buffer = new short[BUFFER_SIZE];
            audioRecord.startRecording();
            audioTrack.play();
            
            // 录制一小段音频
            int readSize = audioRecord.read(buffer, 0, BUFFER_SIZE);
            if (readSize > 0) {
                // 分析音频数据
                deviceInfo.append("Audio Data Stats:\n");
                deviceInfo.append("Max Amplitude: ").append(getMaxAmplitude(buffer)).append("\n");
                deviceInfo.append("Average Amplitude: ").append(getAverageAmplitude(buffer)).append("\n");
                deviceInfo.append("Zero Crossings: ").append(getZeroCrossings(buffer)).append("\n");
            }
            
            // 6. 计算哈希值
            String deviceInfoString = deviceInfo.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(deviceInfoString.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating audio fingerprint", e);
            throw new RuntimeException("Failed to generate audio fingerprint", e);
        } finally {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
            }
        }
    }
    
    private static int getMaxAmplitude(short[] buffer) {
        int max = 0;
        for (short sample : buffer) {
            max = Math.max(max, Math.abs(sample));
        }
        return max;
    }
    
    private static double getAverageAmplitude(short[] buffer) {
        long sum = 0;
        for (short sample : buffer) {
            sum += Math.abs(sample);
        }
        return sum / (double) buffer.length;
    }
    
    private static int getZeroCrossings(short[] buffer) {
        int crossings = 0;
        for (int i = 1; i < buffer.length; i++) {
            if ((buffer[i] >= 0 && buffer[i-1] < 0) || (buffer[i] < 0 && buffer[i-1] >= 0)) {
                crossings++;
            }
        }
        return crossings;
    }
}