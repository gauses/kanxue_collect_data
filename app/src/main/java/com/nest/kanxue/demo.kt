//package com.nest.kanxue
//import android.os.Bundle
//import android.system.Os
//import android.system.StructStat
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.nest.kanxue.statprocpath.FileConstants
//import com.nest.kanxue_data.R
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//class DemoActivity : AppCompatActivity() {
//    private lateinit var pathInput: EditText
//    private lateinit var confirmButton: Button
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var statsAdapter: FileStatsAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.demo)
//
//        pathInput = findViewById(R.id.pathInput)
//        confirmButton = findViewById(R.id.confirmButton)
//        recyclerView = findViewById(R.id.recyclerView)
//
//        // 设置默认路径为 /proc
//        val fileName = "/sdcard/Android/data/com.google.android.gms"
//        val fileName1 = "/proc/self/mounts"
//        pathInput.setText(fileName1)
//
//        // 设置 RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        statsAdapter = FileStatsAdapter()
//        recyclerView.adapter = statsAdapter
//
//        confirmButton.setOnClickListener {
//            val path = pathInput.text.toString()
////            if (path.isNotEmpty() && path.startsWith("/proc")) {
//            if (path.isNotEmpty() ) {
//                val file = File(path)
//                if (file.exists()) {
//                    val statsInfo = getFileStats(file)
//                    statsAdapter.submitList(statsInfo)
//                }
//            }
//        }
//    }
//
//    private fun getFileStats(file: File): List<FileStats> {
//        val statsList = mutableListOf<FileStats>()
//
//        try {
//            when {
//                file.isDirectory -> {
//                    // 处理目录
//                    file.listFiles()?.forEach { childFile ->
//                        try {
//                            val stats = Os.stat(childFile.absolutePath)
//                            statsList.add(createFileStats(childFile, stats))
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//                file.isFile -> {
//                    // 处理单个文件
//                    val stats = Os.stat(file.absolutePath)
//                    statsList.add(createFileStats(file, stats))
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return statsList.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
//    }
//
//    private fun createFileStats(file: File, stats: StructStat): FileStats {
//        val isSymlink = isSymlink(file)
//        val symlinkTarget = if (isSymlink) readSymlinkTarget(file) else null
//
//        return FileStats(
//            name = file.name,
//            path = file.absolutePath,
//            isDirectory = file.isDirectory,
//            isSymlink = isSymlink,
//            symlinkTarget = symlinkTarget,
//            // 基本信息
//            size = stats.st_size,
//            mode = stats.st_mode,
//            uid = stats.st_uid,
//            gid = stats.st_gid,
//            // 时间信息
//            accessTime = Date(stats.st_atime * 1000),
//            modifyTime = Date(stats.st_mtime * 1000),
//            createTime = Date(stats.st_ctime * 1000),
//            // 设备信息
//            device = stats.st_dev,
//            rDevice = stats.st_rdev,
//            // inode信息
//            inode = stats.st_ino,
//            blocks = stats.st_blocks,
//            blockSize = stats.st_blksize,
//            // 链接数
//            nlink = stats.st_nlink
//        )
//    }
//
//    private fun isSymlink(file: File): Boolean {
//        return try {
//            !file.canonicalPath.equals(file.absolutePath)
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    private fun readSymlinkTarget(file: File): String? {
//        return try {
//            file.canonicalPath
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//
//}
//
////// FileStats.kt
////data class FileStats(
////    val name: String,
////    val path: String,
////    val isDirectory: Boolean,
////    val isSymlink: Boolean,
////    val symlinkTarget: String?,
////    // 基本信息
////    val size: Long,
////    val mode: Int,
////    val uid: Int,
////    val gid: Int,
////    // 时间信息
////    val accessTime: Date,
////    val modifyTime: Date,
////    val createTime: Date,
////    // 设备信息
////    val device: Long,
////    val rDevice: Long,
////    // inode信息
////    val inode: Long,
////    val blocks: Long,
////    val blockSize: Long,
////    // 链接数
////    val nlink: Long
////)
//
//
//
//
