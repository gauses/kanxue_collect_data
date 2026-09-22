package com.nest.kanxue

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nest.kanxue.sdk.DeviceCollector
import com.nest.kanxue_data.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SDK 演示壳：
 * - 权限由 AAR 内 [DeviceCollector] 申请（传入 Activity）
 * - 拒绝权限时 SDK 降级继续，本页展示进度日志
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var collecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.permissionButton.setOnClickListener {
            requestPermissionsOnly()
        }
        binding.collectButton.setOnClickListener {
            startCollect()
        }
    }

    /** 只演示 SDK 权限申请，不开始采集。 */
    private fun requestPermissionsOnly() {
        if (collecting) {
            Toast.makeText(this, "采集进行中，请稍候", Toast.LENGTH_SHORT).show()
            return
        }
        appendLog("调用 DeviceCollector.requestPermissions …")
        binding.permissionButton.isEnabled = false
        DeviceCollector.requestPermissions(this) { denied ->
            runOnUiThread {
                binding.permissionButton.isEnabled = true
                if (denied.isEmpty()) {
                    appendLog("权限结果：全部已授予")
                    Toast.makeText(this, "权限已全部授予", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("权限结果：以下未授予（采集时对应项会降级）")
                    denied.forEach { appendLog("  - $it") }
                    Toast.makeText(this, "有 ${denied.size} 项权限未授予", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** 推荐接入方式：Activity 入参 → SDK 申请权限 → 采集上传。 */
    private fun startCollect() {
        if (collecting) return
        collecting = true
        setButtonsEnabled(false)
        appendLog("———— 开始采集 ————")
        appendLog("collectAndUpload(activity)：SDK 将先申请权限")

        DeviceCollector.collectAndUpload(
            activity = this,
            callback = object : DeviceCollector.Callback {
                override fun onProgress(stage: String) {
                    runOnUiThread {
                        appendLog(stage)
                    }
                }

                override fun onSuccess(zipFileName: String) {
                    runOnUiThread {
                        appendLog("成功：$zipFileName")
                        appendLog("———— 结束 ————")
                        collecting = false
                        setButtonsEnabled(true)
                        Toast.makeText(this@MainActivity, "采集完成：$zipFileName", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        appendLog("失败：$message")
                        appendLog("———— 结束 ————")
                        collecting = false
                        setButtonsEnabled(true)
                        Toast.makeText(this@MainActivity, "失败：$message", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.collectButton.isEnabled = enabled
        binding.permissionButton.isEnabled = enabled
    }

    private fun appendLog(line: String) {
        val stamp = timeFmt.format(Date())
        val old = binding.statusText.text?.toString().orEmpty()
        val next = if (old.isBlank()) "[$stamp] $line" else "$old\n[$stamp] $line"
        binding.statusText.text = next
        binding.statusScroll.post {
            binding.statusScroll.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
