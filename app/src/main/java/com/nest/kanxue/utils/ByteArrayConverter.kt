package com.nest.kanxue.utils

class ByteArrayConverter {
    companion object {
        // 方法1：转换成十六进制字符串
        fun ByteArray.toHexString(): String {
            return joinToString("") { "%02x".format(it) }
        }

        // 方法2：使用指定编码转成字符串
        fun ByteArray.toUtf8String(): String {
            return String(this, Charsets.UTF_8)
        }

        // 方法3：转换成带分隔符的十六进制字符串
        fun ByteArray.toHexStringWithSeparator(separator: String = " "): String {
            return joinToString(separator) { "%02x".format(it) }
        }

        // 方法4：使用StringBuilder构建十六进制字符串（性能更好）
        fun ByteArray.toHexStringFast(): String {
            val hexChars = "0123456789abcdef"
            val result = StringBuilder(size * 2)
            forEach { byte ->
                val i = byte.toInt()
                result.append(hexChars[i shr 4 and 0x0f])
                result.append(hexChars[i and 0x0f])
            }
            return result.toString()
        }

        // 方法5：转换成二进制字符串
        fun ByteArray.toBinaryString(): String {
            return joinToString(" ") {
                Integer.toBinaryString(it.toInt() and 0xFF)
                    .padStart(8, '0')
            }
        }
    }
}