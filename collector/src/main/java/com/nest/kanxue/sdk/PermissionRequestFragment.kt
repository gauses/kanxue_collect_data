package com.nest.kanxue.sdk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * 用透明 Fragment 代宿主申请运行时权限，结果回调后自动移除自身。
 */
internal class PermissionRequestFragment : Fragment() {

    private var onFinished: ((denied: List<String>) -> Unit)? = null
    private var pending: Array<String> = emptyArray()
    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pending = arguments?.getStringArray(ARG_PERMS) ?: emptyArray()
    }

    override fun onResume() {
        super.onResume()
        if (launched) return
        launched = true
        if (pending.isEmpty()) {
            finish(emptyList())
            return
        }
        @Suppress("DEPRECATION")
        requestPermissions(pending, REQ_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQ_CODE) return
        val denied = permissions.filterIndexed { index, _ ->
            grantResults.getOrNull(index) != PackageManager.PERMISSION_GRANTED
        }
        finish(denied)
    }

    private fun finish(denied: List<String>) {
        val cb = onFinished
        onFinished = null
        cb?.invoke(denied)
        val fm = parentFragmentManager
        fm.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    companion object {
        private const val TAG = "DeviceCollector.Perm"
        private const val ARG_PERMS = "perms"
        private const val REQ_CODE = 0xDC01

        /** SDK 采集需要的运行时危险权限（随系统版本过滤）。 */
        fun requiredPermissions(): Array<String> {
            val list = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.RECORD_AUDIO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // BODY_SENSORS 在部分机型仍有效；高版本可按需扩展
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                list.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                list.add(Manifest.permission.READ_PHONE_NUMBERS)
            }
            return list.toTypedArray()
        }

        fun missingPermissions(activity: Activity): List<String> {
            return requiredPermissions().filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * @param onFinished 无论用户是否全部授权都会回调；denied 为仍未授予的权限。
         *                   采集侧对单项无权限会降级跳过，因此通常应继续采集。
         */
        fun request(activity: Activity, onFinished: (denied: List<String>) -> Unit) {
            val fragmentActivity = activity as? FragmentActivity
            if (fragmentActivity == null) {
                onFinished(missingPermissions(activity))
                return
            }
            val missing = missingPermissions(activity)
            if (missing.isEmpty()) {
                onFinished(emptyList())
                return
            }
            val existing = fragmentActivity.supportFragmentManager.findFragmentByTag(TAG)
            if (existing is PermissionRequestFragment) {
                existing.onFinished = onFinished
                return
            }
            val fragment = PermissionRequestFragment().apply {
                arguments = Bundle().apply { putStringArray(ARG_PERMS, missing.toTypedArray()) }
                this.onFinished = onFinished
            }
            fragmentActivity.supportFragmentManager
                .beginTransaction()
                .add(fragment, TAG)
                .commitAllowingStateLoss()
        }
    }
}
