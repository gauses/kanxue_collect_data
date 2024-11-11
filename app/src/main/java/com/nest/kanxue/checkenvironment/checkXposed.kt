package com.nest.kanxue.checkenvironment

import android.util.Log
import com.nest.kanxue.Utils
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object checkXposed {

    var xposed_file_path = arrayOf(
        "/sbin/.magisk/modules/riru_lsposed",
        "/data/adb/lspd",
        "/sbin/.magisk/modules/zygisk_lsposed",
        "/sbin/.magisk/modules/riru_edxposed",
        "/data/misc/riru/modules/edxp",
        "/data/adb/riru/modules/edxp.prop",
        "/sbin/.magisk/modules/taichi",
        "/data/misc/taichi",
        "/sbin/.magisk/modules/dreamland",
        "/data/misc/riru/modules/dreamland",
        "/data/adb/riru/modules/dreamland",
        "/system/bin/app_process.orig",
        "/system/xposed.prop",
        "/system/framework/XposedBridge.jar",
        "/system/lib/",
        "libxposed_art.so",
        "/system/lib/",
        "libxposed_art.so",
        ".no_orig",
        "/system/lib64/",
        "libxposed_art.so",
        "/system/lib64/",
        "libxposed_art.so",
        ".no_orig",
        "/system/bin/app_process_zposed",
        "/system/framework/ZposedBridge.jar",
        "/system/lib/",
        "libzposed_art.so"
    )

    //1.检查路径
    fun check_xposed_file_path(): List<String> {
        var list = ArrayList<String>()
        xposed_file_path.forEach { fileName ->
            val value = Utils.checkFileExists(fileName)
            if (value) {
                Log.d("sb", "xposed_file_path fileName = " + fileName + "exists")
                list.add(fileName)
            }
        }
        return list
    }

    //2.方法2：Hook loadClass加载类，检查程序加载的Class中是否包含名为de.robv.android.xposed.XposedBridge的类名路径。
    fun check_Hook_loadClass(classLoader: ClassLoader){
        try {
            // 获取 loadClass 方法的引用
            val loadClassMethod = ClassLoader::class.java.getDeclaredMethod("loadClass", String::class.java)
            loadClassMethod.isAccessible = true

            // 创建一个代理来拦截 loadClass 方法调用
            val handler = InvocationHandler { proxy, method, args ->
                // 检查是否是 loadClass 方法被调用
                if (method.name == "loadClass" && args.isNotEmpty() && args[0] is String) {
                    val className = args[0] as String
                    if (className == "de.robv.android.xposed.XposedBridge") {
                        println("检测到加载 XposedBridge 类！")
                        // 在这里可以进行其他处理，例如报警或拦截
                    }
                }

                // 调用原始的 loadClass 方法
                method.invoke(classLoader, *args)
            }

            // 创建代理 ClassLoader，拦截 loadClass
            val proxyClassLoader = Proxy.newProxyInstance(
                classLoader.javaClass.classLoader,
                arrayOf(ClassLoader::class.java),
                handler
            ) as ClassLoader

            // 通过反射将代理的 ClassLoader 设置到当前线程的 context 中
            Thread.currentThread().contextClassLoader = proxyClassLoader

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    //3.方法3:遍历/data/data/目录，寻找有无以下包名：
    //由于 Android 系统权限的限制，通常只有在 root 权限下才有权限访问 /data/data/ 目录中的所有文件夹。否则，应用只能访问自己的数据目录。
    fun check_data_package(): List<String>{
        val suspiciousPackages = listOf(
            "de.robv.android.xposed.installer",  // Xposed框架
            "org.meowcat.edxposed.manager",      // EdXposed框架
            "com.tsng.hidemyapplist",            // 隐藏应用列表
            "com.tsng.hidemyroot",               // 隐藏Root
            "org.lsposed.manager",               // LSPosed框架
            "me.weishu.exp",                     // VirtualXposed 太极
            "top.canyie.dreamland.manager",      // VirtualXposed 夢境
            "io.va.exposed",                     // VirtualXposed 夢境
            "io.va.exposed64",                   // VirtualXposed 夢境
            "io.virtualapp",                     // VirtualApp
            "io.virtualapp.sandvxposed64"        // VirtualApp
        )
        val foundPackages = mutableListOf<String>()

        // /data/data/目录路径
        val dataDir = File("/data/data/")
        if (dataDir.exists() && dataDir.isDirectory) {
            // 遍历 /data/data/ 目录中的子目录
            dataDir.listFiles()?.forEach { dir ->
                if (dir.isDirectory) {
                    val packageName = dir.name
                    if (packageName in suspiciousPackages) {
                        foundPackages.add(packageName)
                    }
                }
            }
        }
        return foundPackages
    }






}