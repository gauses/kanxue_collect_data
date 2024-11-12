package com.nest.kanxue.simulators

import android.content.Context
import org.json.JSONObject

object CheckSimulators {


    fun getInfo(context: Context): JSONObject{
        val simulatorsJSON = JSONObject()

        val simulatorFileList = CheckFileDir.checkEmulatorFiles()
        if (simulatorFileList.isEmpty()) {
            simulatorsJSON.put("检查文件是否包含Simulators文件目录" , "不包含")
        }else{
            simulatorsJSON.put("检查文件是否包含Simulators文件目录" , "包含")
            simulatorsJSON.put("包含Simulators文件目录如下", simulatorFileList )
        }


        val emulatorPropsList = CheckSystemProp.checkEmulatorPropsWithGetprop()
        if (emulatorPropsList.isEmpty()) {
            simulatorsJSON.put("system.prop下是否存在相关字段特征" , "不包含")
        }else{
            simulatorsJSON.put("system.prop下是否存在相关字段特征" , "包含")
            simulatorsJSON.put("包含system.prop相关字段特征", emulatorPropsList )
        }


        val mountPointsflag = CheckMount.check()
        if (mountPointsflag) {
            simulatorsJSON.put("挂载点是否存在相关指定目录" , "存在")
        }else{
            simulatorsJSON.put("挂载点是否存在相关指定目录" , "不存在")
        }

        simulatorsJSON.put("检查/proc/mounts中是否存在vboxsf字段" , CheckForVboxsf.checkForVboxsf())

        simulatorsJSON.put("检查是否安装VMOS虚拟机" , CheckVMSoftware.isVMOSInstalled(context))
        simulatorsJSON.put("检查是否安装X8沙箱" , CheckVMSoftware.isX8SandboxInstalled())

        simulatorsJSON.put("检查是否安装51虚拟机" , CheckVMSoftware.is51VMInstalled())
        simulatorsJSON.put("检查是否安装虚拟精灵和虚拟大师" , CheckVMSoftware.isOtherVMInstalled())


        return simulatorsJSON



    }
}