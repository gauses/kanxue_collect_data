package com.nest.kanxue.statfs64

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Fsid(
    val values: List<Int>
)

data class Statfs64(
    val fType: Long,
    val bsize: Long,
    val blocks: Long,
    val bfree: Long,
    val bavail: Long,
    val files: Long,
    val ffree: Long,
    val fsid: Fsid,
    val namelen: Long,
    val frsize: Long,
    val flags: Long,
    val spare: List<Long>
)

class Statfs64Parser {
    fun parse(bytes: ByteArray): Statfs64 {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        return Statfs64(
            fType = buffer.getLong(),
            bsize = buffer.getLong(),
            blocks = buffer.getLong(),
            bfree = buffer.getLong(),
            bavail = buffer.getLong(),
            files = buffer.getLong(),
            ffree = buffer.getLong(),
            fsid = Fsid(listOf(buffer.getInt(), buffer.getInt())),
            namelen = buffer.getLong(),
            frsize = buffer.getLong(),
            flags = buffer.getLong(),
            spare = List(3) { buffer.getLong() }
        )
    }

    fun toString(statfs: Statfs64): String {
        return """
            Parsed struct statfs64: {
                f_type: ${statfs.fType},
                f_bsize: ${statfs.bsize},
                f_blocks: ${statfs.blocks},
                f_bfree: ${statfs.bfree},
                f_bavail: ${statfs.bavail},
                f_files: ${statfs.files},
                f_ffree: ${statfs.ffree},
                f_fsid: {
                    val: ${statfs.fsid.values}
                },
                f_namelen: ${statfs.namelen},
                f_frsize: ${statfs.frsize},
                f_flags: ${statfs.flags},
                f_spare: ${statfs.spare}
            }
        """.trimIndent()
    }
}