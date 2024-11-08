package com.nest.kanxue.Dents64


import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object parseDents64 {



    fun parseDents64(byteArray: ByteArray): List<String> {
        val entries = mutableListOf<String>()
        val buffer = ByteBuffer.wrap(byteArray)
        buffer.order(ByteOrder.nativeOrder()) // 设置为系统的字节序

        while (buffer.remaining() > 10) {  // 确保至少有基本的10个字节可以读取
            // 解析 inode 编号
            val inode = buffer.long // 8 bytes for inode number
            // 解析目录项的长度
            val entryLength = buffer.short.toInt() // 2 bytes for entry length

            if (entryLength <= 10 || entryLength > buffer.remaining()) {
                // 如果 entryLength 不合理（小于最小长度或超出剩余缓冲区），退出解析
                break
            }

            // 解析文件名长度
            val nameLength = buffer.get().toInt() // 1 byte for name length
            // 跳过类型字段
            buffer.get() // 1 byte for type (skip)

            // 读取文件名
            val nameBytes = ByteArray(nameLength)
            buffer.get(nameBytes)
            val fileName = String(nameBytes, Charsets.UTF_8) // 转换为字符串

            entries.add("Inode: $inode, Name: $fileName")

            // 跳过到下一个目录项
            val remaining = entryLength - (10 + nameLength)
            if (remaining > 0 && remaining <= buffer.remaining()) {
                buffer.position(buffer.position() + remaining)
            } else {
                break // 如果剩余长度不合理，终止解析
            }
        }
        return entries
    }
}