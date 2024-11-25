package com.nest.kanxue.statprocpath

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nest.kanxue.FileStat
import com.nest.kanxue_data.R

class StatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val nameText: TextView = view.findViewById(R.id.nameText)
    private val basicInfoText: TextView = view.findViewById(R.id.basicInfoText)
    private val timeInfoText: TextView = view.findViewById(R.id.timeInfoText)
    private val secTimeInfoText: TextView = view.findViewById(R.id.secTimeInfoText)
    private val deviceInfoText: TextView = view.findViewById(R.id.deviceInfoText)
    private val inodeInfoText: TextView = view.findViewById(R.id.inodeInfoText)


    fun bind(stats: FileStat) {

        Log.d("sb" , "confirmButton bind = $stats")

        if (stats.accessTime == null && stats.modifyTime == null && stats.changeTime == null) {
            nameText.text =  """    
                文件路径: ${stats.fileName}
                出错：${stats.error}
            """.trimIndent()
            timeInfoText.text = ""
            secTimeInfoText.text = ""
            inodeInfoText.text = ""
            deviceInfoText.text = ""

        }else{
            nameText.text = """
                文件路径: ${stats.fileName}
            """.trimIndent()

            timeInfoText.text = """
                Access Time: ${stats.accessTime}
                Modify Time: ${stats.modifyTime}
                Change Time: ${stats.changeTime}
            """.trimIndent()

            secTimeInfoText.text = """
                secTime: ${stats.secTime}
            """.trimIndent()


            inodeInfoText.text = """
                Inode号: ${stats.inode}
                Size: ${stats.Size}
                Blocks: ${stats.Blocks}
                IO Blocks: ${stats.IOBlocks} 字节
            """.trimIndent()


            deviceInfoText.text = """
                Device: ${stats.Device}
                Links: ${stats.Links}
                DeviceType: ${stats.DeviceType}
                Uid: ${stats.Uid} 
                Gid: ${stats.Gid} 
            """.trimIndent()


        }



    }
}
