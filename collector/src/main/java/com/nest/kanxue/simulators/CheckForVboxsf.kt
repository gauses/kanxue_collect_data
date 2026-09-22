package com.nest.kanxue.simulators

import java.io.File

//检查挂载点,检查/proc/mounts中是否存在vboxsf字段.

object CheckForVboxsf {

    fun checkForVboxsf(): Boolean {
        val mountsFile = File("/proc/mounts")
        if (!mountsFile.exists()) {
            println("Error: /proc/mounts file does not exist.")
            return false
        }

        var  flag = false
        mountsFile.forEachLine { line ->
            if (line.contains("vboxsf")) {
                flag =  true
            }
        }
        return flag
    }

}