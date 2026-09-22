package com.nest.kanxue.statprocpath

object FileConstants {

    // 文件类型掩码 - 使用十六进制替代八进制
    const val S_IFMT   = 0xF000    // 文件类型位掩码  (0o170000)
    const val S_IFSOCK = 0xC000    // socket         (0o140000)
    const val S_IFLNK  = 0xA000    // 符号链接        (0o120000)
    const val S_IFREG  = 0x8000    // 普通文件        (0o100000)
    const val S_IFBLK  = 0x6000    // 块设备          (0o060000)
    const val S_IFDIR  = 0x4000    // 目录           (0o040000)
    const val S_IFCHR  = 0x2000    // 字符设备        (0o020000)
    const val S_IFIFO  = 0x1000    // FIFO          (0o010000)

    // 权限位
    const val S_ISUID  = 0x800     // set-user-ID bit  (0o004000)
    const val S_ISGID  = 0x400     // set-group-ID bit (0o002000)
    const val S_ISVTX  = 0x200     // sticky bit       (0o001000)

    // 用户权限
    const val S_IRUSR  = 0x100     // 用户可读         (0o000400)
    const val S_IWUSR  = 0x80      // 用户可写         (0o000200)
    const val S_IXUSR  = 0x40      // 用户可执行       (0o000100)

    // 组权限
    const val S_IRGRP  = 0x20      // 组可读          (0o000040)
    const val S_IWGRP  = 0x10      // 组可写          (0o000020)
    const val S_IXGRP  = 0x8       // 组可执行        (0o000010)

    // 其他用户权限
    const val S_IROTH  = 0x4       // 其他可读         (0o000004)
    const val S_IWOTH  = 0x2       // 其他可写         (0o000002)
    const val S_IXOTH  = 0x1       // 其他可执行       (0o000001)


    fun getPermissionsString(mode: Long): String {
        val perms = StringBuilder()

        // 文件类型
        perms.append(when (mode and FileConstants.S_IFMT.toLong()) {
            FileConstants.S_IFDIR.toLong() -> 'd'  // 目录
            FileConstants.S_IFLNK.toLong() -> 'l'  // 符号链接
            FileConstants.S_IFREG.toLong() -> '-'  // 普通文件
            FileConstants.S_IFBLK.toLong() -> 'b'  // 块设备
            FileConstants.S_IFCHR.toLong() -> 'c'  // 字符设备
            FileConstants.S_IFIFO.toLong() -> 'p'  // 命名管道
            FileConstants.S_IFSOCK.toLong() -> 's' // 套接字
            else -> '?'  // 未知类型
        })

        // 用户权限 (USR)
        perms.append(if (mode and FileConstants.S_IRUSR.toLong() != 0L) 'r' else '-')
        perms.append(if (mode and FileConstants.S_IWUSR.toLong() != 0L) 'w' else '-')
        perms.append(if (mode and FileConstants.S_IXUSR.toLong() != 0L) 'x' else '-')

        // 组权限 (GRP)
        perms.append(if (mode and FileConstants.S_IRGRP.toLong() != 0L) 'r' else '-')
        perms.append(if (mode and FileConstants.S_IWGRP.toLong() != 0L) 'w' else '-')
        perms.append(if (mode and FileConstants.S_IXGRP.toLong() != 0L) 'x' else '-')

        // 其他用户权限 (OTH)
        perms.append(if (mode and FileConstants.S_IROTH.toLong() != 0L) 'r' else '-')
        perms.append(if (mode and FileConstants.S_IWOTH.toLong() != 0L) 'w' else '-')
        perms.append(if (mode and FileConstants.S_IXOTH.toLong() != 0L) 'x' else '-')

        // 处理特殊权限位
        // SUID (Set-UID)
        if (mode and FileConstants.S_ISUID.toLong() != 0L) {
            val index = 3
            perms.setCharAt(index, if (perms[index] == 'x') 's' else 'S')
        }

        // SGID (Set-GID)
        if (mode and FileConstants.S_ISGID.toLong() != 0L) {
            val index = 6
            perms.setCharAt(index, if (perms[index] == 'x') 's' else 'S')
        }

        // Sticky bit
        if (mode and FileConstants.S_ISVTX.toLong() != 0L) {
            val index = 9
            perms.setCharAt(index, if (perms[index] == 'x') 't' else 'T')
        }

        return perms.toString()
    }
}