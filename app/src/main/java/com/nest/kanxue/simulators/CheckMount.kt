package com.nest.kanxue.simulators

import java.io.File

//3.检查挂载点,是否存在以下目录:

object CheckMount {

    private fun checkMountPoints(directories: List<String>): List<String> {
        val missingDirectories = mutableListOf<String>()

        for (directory in directories) {
            val file = File(directory)
            if (!file.exists()) {
                missingDirectories.add(directory)
            }
        }

        return missingDirectories
    }

    fun check(): Boolean {
        var flag = false
        val mountPoints = listOf(
            "/mnt/shared/Sharefolder",
            "/tiantian.conf",
            "/data/share1",
            "/hardware_device.conf",
            "/mnt/shared/products",
            "/mumu_hardware.conf",
            "/Andy.conf",
            "/mnt/windows/BstSharedFolder",
            "/bst.conf",
            "/mnt/shared/Applications",
            "/ld.conf"
        )

        //missingDirectories:列表包含未找到的目录，最终会打印不存在的路径。
        val missingDirectories = checkMountPoints(mountPoints)

        if (missingDirectories.isEmpty()) {
            println("All specified mount points exist.")
            flag = true
        } else {
            println("The following mount points are missing:")
            missingDirectories.forEach { println(it) }
            flag = false
        }

        return flag
    }
}