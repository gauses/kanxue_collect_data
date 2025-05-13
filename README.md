# kanxue_collect_data
使用看雪论坛帖子收集数据：https://bbs.kanxue.com/thread-280869.htm

##设备标识：
//uuid:使用AndroidID生成
//ad_aaid:GoogleID
//ReaperAssignedDeviceId :暂无
//IMEI:从 Android 10（API 29）开始，获取 IMEI 的权限受到限制。只有特定的系统应用或通过设备管理员配置的企业应用才能直接访问 IMEI
//mdm_uuid:通常，mdm_uuid 只能通过 MDM 平台提供的 API 或管理控制台查看和获取，开发者无法直接在 Android 系统代码中调用获取。
//ps_imei:ps_imei 可能是某些设备厂商或第三方应用用于标识设备的参数，其中 ps 可能代表 "Persistent Storage" 或者是厂商的自定义前缀。例如，ps_imei 可能是在设备上存储的 IMEI 号码的一个别名或者备份参数。
//op_security_uuid:op_security_uuid 可能是某些 Android 设备厂商（例如 OPPO）定义的一个独特的设备标识符。通常，厂商会在设备中加入一些专有的标识符，用于特定用途，比如设备安全、设备认证、追踪、售后服务等。
//ai_stored_imei:很可能是一个由设备厂商或特定应用定义的字段，用于存储设备的 IMEI 信息。以下是 ai_stored_imei 的可能用途和来源
//device_serial:从 Android 9 开始，普通应用无法直接访问设备序列号，只有系统应用和设备管理应用可以访问。


##硬件信息：(以下信息没有读取到)
内部存储（EMMC或UFS闪存）的序列号：/sys/block/mmcblk0/device/serial  （核心）
显示设备序列号：/sys/devices/soc0/serial_number  (核心)
内部存储SD卡的CID：/sys/block/mmcblk0/device/cid（核心）
input设备相关，读取/proc/bus/input/devices，获取注册的input设备信息，比如Name和Sysfs。



#网络信息：读取到的数据有一些问题


#文件哈希：和上面设备标识中的(获取文件的最近访问时间、最近修改时间、最近改变时间，Innode编号)重复


这一个暂时没有加
#环境检测：
一、HOOK环境:
在libNetHTProtect.so中主要检测了crc校验，还有frida和Xposed的一些特征： (这个网易易盾手游SDK，暂时没有加)
，
2.检查/dev/wgzs目录下的内容是否存在，若存在则获取值：  (因为非root环境，读取不了/dev，手动cat也看不到信息，暂时没有加)


7.检测seLinux安全上下文，cat /proc/%d/attr/prev检测app进程的selinux安全上下文是否为“u:r:zygote:s0(没有看懂，暂时没有加)


ebpf检测(没有看懂，暂时没有加)


//没有权限
//出于安全原因/proc/stat，Google自 Android O 起已阻止用户应用程序访问
https://issuetracker.google.com/issues/37140047?pli=1
3.读取/proc/stat下的所有内容


4.读取/sys/firmware/devicetree/base/compatible


5.stat /dev/fuse


2.https://www.cnblogs.com/sishuiliuyun/p/3245599.html



收集帮忙加多两个文件内容：
/proc/self/mounts
/proc/meminfo

加多两个api调用结果返回：
sysinfo
uname -a




0417：
1./proc/meminfo
2.stats - f /data
3.修改duoplus抓取cpu文件时候，修改成了真机抓取cpu不上传的bug



0418：
1.am get-config
2.pm list features




0508：
1.service list
2.rsync -a  源目录  目的目录  ------cpu 和 度传感器
3.cp -rlp source_dir/ target_dir/ ------cpu 和 度传感器



