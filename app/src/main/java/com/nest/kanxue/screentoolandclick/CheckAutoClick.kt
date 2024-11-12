package com.nest.kanxue.screentoolandclick

import android.content.Context
import com.nest.kanxue.model_system_determination.CheckLineageOS
import org.json.JSONObject

object CheckAutoClick {


    fun getInfo(context: Context) : JSONObject{
        val autoClickJSON = JSONObject()
        autoClickJSON.put("运行的服务中是否存在关键字" , CheckKeywordsOnService.checkForScreenshotAndTouchServices(context))
        autoClickJSON.put("/data/local/tmp/有无相关文件或文件目录" , CheckFileOnTmp.checkFilesInDataLocalTmp())
        autoClickJSON.put("检查是否安装模拟点击的应用" , CheckInstallPackage.checkInstalledAutoClickerApps(context))
        autoClickJSON.put("检查是否安装截图应用" , CheckInstallPackage.checkInstalledScreenshotApps(context))

        return autoClickJSON


    }
}