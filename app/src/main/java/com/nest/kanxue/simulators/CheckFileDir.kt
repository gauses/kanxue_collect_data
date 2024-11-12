package com.nest.kanxue.simulators

import org.json.JSONObject
import java.io.File

//扫描常见的模拟器特征，主要检测的方法是：
//1、是否存在以下文件目录：

object CheckFileDir {

    fun checkFileOrDirectory(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.canRead()
    }

    fun checkEmulatorFiles(): List<String> {

        val EMULATOR_PATHS = listOf(
            // LDPlayer 相关
            "/system/bin/ldinit",
            "/system/bin/ldmountsf",
            "/system/lib/libldutils.so",

            // Microvirt (MEmu) 相关
            "/system/bin/microvirt-prop",
            "/system/bin/microvirtd",

            // Droid4X 相关
            "/system/lib/libdroid4x.so",
            "/system/bin/droid4x-prop",

            // Windroy 相关
            "/system/bin/windroyed",

            // 网易MuMu 相关
            "/system/etc/mumu-configs/device-prop-configs/mumu.config",
            "/data/data/com.mumu.launcher",
            "/data/data/com.mumu.store",
            "/data/data/com.netease.mumu.cloner",

            // Nox 相关
            "/system/bin/nox-prop",
            "/system/lib/libnoxspeedup.so",
            "/data/property/persist.nox.simulator_version",
            "/data/misc/profiles/ref/com.bignox.google.installer",
            "/data/misc/profiles/ref/com.bignox.app.store.hd",

            // TianTian (天天) 相关
            "/system/bin/ttVM-prop",

            // 海马玩 相关
            "/system/bin/duosconfig",

            // 逍遥 相关
            "/system/etc/xxzs_prop.sh",

            // Bluestacks 相关
            "/boot/bstsetup.env",
            "/boot/bstmods",
            "/system/xbin/bstk",
            "/data/bluestacks.prop",
            "/data/data/com.bluestacks.appmart",
            "/data/data/com.bluestacks.home",
            "/system/bin/bstshutdown",
            "/sys/module/bstinput",
            "/sys/class/misc/bstXqpb",

            // Phoenix OS 相关
            "/system/phoenixos",
            "/xbin/phoenix_compat",

            // 遁地 相关
            "/init.dundi.rc",
            "/system/etc/init.dundi.sh",
            "/data/data/com.ddmnq.dundidevhelper",

            // Andy 相关
            "/init.andy.cloud.rc",

            // 小皮 相关
            "/system/bin/xiaopiVM-prop",

            // 雷电 相关
            "/system/bin/XCPlayer-prop",
            "/system/lib/liblybox_prop.so",

            // 腾讯手游助手相关
            "/system/bin/tencent_virtual_input",
            "/vendor/bin/init.tencent.sh",

            // YouWave 相关
            "/data/youwave_id",

            // VirtualBox 相关
            "/dev/vboxguest",
            "/dev/vboxuser",
            "/sys/bus/pci/drivers/vboxguest",
            "/sys/class/bdi/vboxsf-c",
            "/sys/class/misc/vboxguest",
            "/sys/class/misc/vboxuser",
            "/sys/devices/virtual/bdi/vboxsf-c",
            "/sys/devices/virtual/misc/vboxguest",
            "/sys/devices/virtual/misc/vboxuser",
            "/sys/module/vboxguest",
            "/sys/module/vboxsf",
            "/sys/module/vboxvideo",
            "/system/bin/androVM-vbox-sf",
            "/system/bin/androVM_setprop",
            "/system/bin/get_androVM_host",
            "/system/bin/mount.vboxsf",
            "/system/etc/init.androVM.sh",
            "/system/etc/init.buildroid.sh",
            "/system/lib/vboxguest.ko",
            "/system/lib/vboxsf.ko",
            "/system/lib/vboxvideo.ko",
            "/system/xbin/mount.vboxsf",

            // QEMU/Goldfish 相关
            "/dev/goldfish_pipe",
            "/sys/devices/virtual/misc/goldfish_pipe",
            "/sys/module/goldfish_audio",
            "/sys/module/goldfish_battery",

            // KVM 相关
            "/sys/module/kvm_intel/",
            "/sys/module/kvm_amd/",

            // x86 相关
            "/init.android_x86_64.rc",
            "/init.android_x86.rc",
            "/init.androidVM_x86.rc",
            "/init.intel.rc",
            "/init.vbox2345_x86.rc"
        )

        val detectedPaths = mutableListOf<String>()

        for (path in EMULATOR_PATHS) {
            try {
                if (File(path).exists()) {
                    detectedPaths.add(path)
                }
            } catch (e: Exception) {
                // 忽略访问异常
                continue
            }
        }

        return detectedPaths


    }

//    /**
//     * 获取可能的模拟器类型
//     * @return 返回检测到的模拟器类型列表
//     */
//    fun getEmulatorType(): List<String> {
//        val detectedTypes = mutableSetOf<String>()
//        val detectedPaths = detectEmulatorFiles()
//
//        for (path in detectedPaths) {
//            when {
//                path.contains("ld") -> detectedTypes.add("雷电模拟器")
//                path.contains("microvirt") -> detectedTypes.add("逍遥模拟器")
//                path.contains("droid4x") -> detectedTypes.add("海马玩模拟器")
//                path.contains("nox") -> detectedTypes.add("夜神模拟器")
//                path.contains("mumu") -> detectedTypes.add("网易MuMu模拟器")
//                path.contains("bst") -> detectedTypes.add("BlueStacks")
//                path.contains("phoenix") -> detectedTypes.add("PhoenixOS")
//                path.contains("dundi") -> detectedTypes.add("遁地模拟器")
//                path.contains("tencent") -> detectedTypes.add("腾讯手游助手")
//                path.contains("vbox") -> detectedTypes.add("VirtualBox")
//                path.contains("goldfish") -> detectedTypes.add("Android SDK 模拟器")
//                path.contains("kvm") -> detectedTypes.add("KVM 虚拟机")
//            }
//        }
//
//        return detectedTypes.toList()
//    }


}