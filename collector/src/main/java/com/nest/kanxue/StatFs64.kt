package com.nest.kanxue

class StatFs64(
    val f_type: Long,
    val f_bsize: Long,
    val f_blocks: Long,
    val f_bfree: Long,
    val f_bavail: Long,
    val f_files: Long,
    val f_ffree: Long,
    val f_fsid: Long,
    val f_namelen: Long,
    val f_frsize: Long,
    val f_flags: Long,
    val f_spare: LongArray
)