package com.nest.kanxue.root

import android.content.Context
import com.nest.kanxue.root.CheckMagiskFile.checkMagiskFiles
import com.nest.kanxue.root.CheckSuFile.checkSuFiles
import com.nest.kanxue.screentoolandclick.BuildTagsChecker
import com.nest.kanxue.screentoolandclick.SELinuxContextChecker
import org.json.JSONObject

object CheckRoot {


    fun getInfo(context: Context): JSONObject{

        val rootJSON = JSONObject()

        val rootPkgList = CheckInstallPackageRoot.checkPackages(context)
        if (rootPkgList.isEmpty()) {
            rootJSON.put("是否发现root包名" , "否")
        } else {
            rootJSON.put("是否发现root包名" , "是")
            rootJSON.put("发现root包名" , rootPkgList)
            rootPkgList.forEach { println("- $it") }
        }

        //su
        if (checkSuFiles().isNotEmpty()){
            rootJSON.put("检查su文件是否存在" , "不存在")
        }else{
            rootJSON.put("检查su文件是否存在" , "存在")
        }

        //magisk
        if (checkMagiskFiles().isNotEmpty()){
            rootJSON.put("检查magisk文件是否存在" , "不存在")
        }else{
            rootJSON.put("检查magisk文件是否存在" , "存在")
        }


        //检查是否在PATH中发现su
        if (CheckAndroidPath.hasSuInPath()){
            rootJSON.put("检查是否在PATH中发现su" , "存在")
        }else{
            rootJSON.put("检查是否在PATH中发现su" , "不存在")
        }


        //检测 /proc/self/maps 是否存在名为“/memfd:/jit-cache”的段（加载zygisk模块时（也就是liblspd.so)的时候会讲其名称设置为jit-cache，这样的话so的内存段在maps中就是/memfd:/jit-cache）
        rootJSON.put("检查JIT Cache:" , CheckSelfMaps.hasJitCacheSegment())
        //通过检测map表是否存在匿名的并且具有可执行属性的内存判断是否存在lsposed
        rootJSON.put("检查匿名可执行段:" , CheckSelfMaps.hasAnonymousExecutableSegment())
        //检测栈空间[stack]的权限是否为“rw-p”
        rootJSON.put("检查栈权限异常:" , CheckSelfMaps.hasAbnormalStackPermissions())




        //检测ro.build.tags的值，读取 /system/build.prop并检测ro.build.tags的值是否为“test-keys”
        val allChecks = BuildTagsChecker.checkBuildTags()
        println("\n所有检查结果:")
        allChecks.forEach { (method, result) ->
            println("$method: ${
                when(result) {
                    true -> "发现test-keys"
                    false -> "未发现test-keys"
                    null -> "无法检查"
                }
            }")
        }
        rootJSON.put("是否检测到test-keys" , BuildTagsChecker.isTestKeysDetected())


//        //检测seLinux
//        // 检查当前进程
//        val currentProcess = SELinuxContextChecker.checkCurrentProcessContext()
//        println("当前进程 (PID: ${currentProcess.pid}):")
//        println("上下文: ${currentProcess.context}")
//        println("是否为Zygote上下文: ${currentProcess.isZygoteContext}")
//        currentProcess.error?.let { println("错误: $it") }
//
//        // 检查父进程
//        val parentProcess = SELinuxContextChecker.checkParentProcessContext()
//        println("\n父进程:")
//        if (parentProcess != null) {
//            println("PID: ${parentProcess.pid}")
//            println("上下文: ${parentProcess.context}")
//            println("是否为Zygote上下文: ${parentProcess.isZygoteContext}")
//            parentProcess.error?.let { println("错误: $it") }
//        } else {
//            println("无法获取父进程信息")
//        }
//
//        // 获取SELinux状态
//        println("\nSELinux状态: ${SELinuxContextChecker.getSELinuxStatus() ?: "无法获取"}")
//        rootJSON.put("SELinux状态" , (SELinuxContextChecker.getSELinuxStatus()?: "无法获取"))
//
//
//        // 快速检查是否检测到zygote上下文
//        println("\n是否检测到Zygote上下文: ${SELinuxContextChecker.isZygoteContextDetected()}")
//
//        // 获取进程树上下文
//        println("\n进程树上下文:")
//        SELinuxContextChecker.getProcessTreeContexts().forEach { info ->
//            println("PID ${info.pid}: ${info.context}")
//        }
//
//        // 打印完整报告
//        println("\n完整报告:")
//        println(SELinuxContextChecker.getFullReport())

        rootJSON.put("检查/data/local/tmp/是否有shizuku" , CheckShizukuFile.checkFilesExistence())




        return rootJSON
    }
}