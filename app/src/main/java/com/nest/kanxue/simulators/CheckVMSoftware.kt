package com.nest.kanxue.simulators
import android.app.ActivityManager
import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

//4.检查是否安装了虚拟机软件:
//1.VMOS虚拟机
//方法1：运行服务查找：tomq_MANAGER_SERVICE
//方法2：ROOT_DIR查找VMOS_SYS_NUM
//方法3：/proc/self/root/data/data/查找包名：“com.vmos.app”、“com.vmos.pro”、“com.vmos.ggp”
//方法4：props查找：vmprop.androidid、vmprop.dev_ashmem、vmprop.ip、ro.vmos.simplest.rom
//2.X8沙箱
//方法1:props查找：ro.x8.version、ro.x8.uuid
//方法2:查找文件目录：/x8/config/root.pkg.blacklist、/x8/config/full_vm是否存在
//3.51虚拟机
///proc/self/root/data/data/查找包名：“com.f1player”、“com.f1player.play”
//4.虚拟精灵和虚拟大师
///proc/self/root/data/data/查找包名：com.pspace.vandroid、com.yiqiang.xmaster
object CheckVMSoftware {


    // 检测 VMOS
    fun isVMOSInstalled(context: Context): Boolean {
        return checkVMOSService(context) ||
                checkVMOSSysNum() ||
                checkVMOSPackages() ||
                checkVMOSProps()
    }

    private fun checkVMOSService(context: Context): Boolean {
        return try {
            // 方法1: 通过 ActivityManager 获取运行的服务
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
            runningServices.any { it.service.className.contains("tomq_MANAGER_SERVICE") }
        } catch (e: Exception) {
            try {
                // 方法2: 通过 ps 命令查看进程
                val process = Runtime.getRuntime().exec("ps")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line?.contains("tomq_MANAGER_SERVICE") == true) {
                        return true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkVMOSSysNum(): Boolean {
        return File("ROOT_DIR/VMOS_SYS_NUM").exists()
    }

    private fun checkVMOSPackages(): Boolean {
        val vmosPackages = listOf(
            "com.vmos.app",
            "com.vmos.pro",
            "com.vmos.ggp"
        )
        return checkPackagesInProcData(vmosPackages)
    }

    private fun checkVMOSProps(): Boolean {
        val vmosProps = listOf(
            "vmprop.androidid",
            "vmprop.dev_ashmem",
            "vmprop.ip",
            "ro.vmos.simplest.rom"
        )
        return checkSystemProperties(vmosProps)
    }

    // 检测 X8 沙箱
    fun isX8SandboxInstalled(): Boolean {
        return checkX8Props() || checkX8Files()
    }

    private fun checkX8Props(): Boolean {
        val x8Props = listOf(
            "ro.x8.version",
            "ro.x8.uuid"
        )
        return checkSystemProperties(x8Props)
    }

    private fun checkX8Files(): Boolean {
        val x8Files = listOf(
            "/x8/config/root.pkg.blacklist",
            "/x8/config/full_vm"
        )
        return x8Files.any { File(it).exists() }
    }

    // 检测 51 虚拟机
    fun is51VMInstalled(): Boolean {
        val packages51VM = listOf(
            "com.f1player",
            "com.f1player.play"
        )
        return checkPackagesInProcData(packages51VM)
    }

    // 检测虚拟精灵和虚拟大师
    fun isOtherVMInstalled(): Boolean {
        val otherVMPackages = listOf(
            "com.pspace.vandroid",
            "com.yiqiang.xmaster"
        )
        return checkPackagesInProcData(otherVMPackages)
    }

    // 通用工具方法
    private fun checkPackagesInProcData(packages: List<String>): Boolean {
        try {
            val procPath = "/proc/self/root/data/data/"
            val directory = File(procPath)
            if (directory.exists() && directory.isDirectory) {
                return packages.any { packageName ->
                    File(procPath + packageName).exists()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun checkSystemProperties(props: List<String>): Boolean {
        try {
            // 读取系统属性
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(process.inputStream.reader())
            val properties = reader.readText()

            return props.any { prop ->
                properties.contains(prop)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}