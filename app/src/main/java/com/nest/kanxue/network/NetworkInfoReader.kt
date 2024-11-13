package com.nest.kanxue.network


import java.net.NetworkInterface
import java.net.InetAddress
import android.content.Context
import android.net.wifi.WifiManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Build

class NetworkInfoReader(private val context: Context) {

    data class NetworkInterfaceInfo(
        val name: String,                // 接口名称
        val displayName: String,         // 显示名称
        val ipAddresses: List<String>,   // IP地址列表
        val macAddress: String,          // MAC地址
        val subnetMask: String,         // 子网掩码
        val dnsServers: List<String>,   // DNS服务器列表
        val isUp: Boolean,              // 接口是否启用
        val isLoopback: Boolean,        // 是否是回环接口
        val mtu: Int                    // 最大传输单元
    )

    /**
     * 获取所有网络接口信息
     */
    fun getAllNetworkInterfaces(): List<NetworkInterfaceInfo> {
        val interfaces = mutableListOf<NetworkInterfaceInfo>()

        try {
            // 获取所有网络接口
            NetworkInterface.getNetworkInterfaces().toList().forEach { networkInterface ->
                // 获取接口的IP地址
                val addresses = networkInterface.inetAddresses.toList()
                val ipAddresses = addresses.map { it.hostAddress }.filter { it != null }

                // 获取MAC地址
                val mac = networkInterface.hardwareAddress?.joinToString(":") {
                    "%02x".format(it)
                } ?: "unknown"

                // 获取子网掩码
                val subnetMask = getSubnetMask(networkInterface)

                // 获取DNS服务器
                val dnsServers = getDnsServers()

                interfaces.add(NetworkInterfaceInfo(
                    name = networkInterface.name,
                    displayName = networkInterface.displayName,
                    ipAddresses = ipAddresses,
                    macAddress = mac,
                    subnetMask = subnetMask,
                    dnsServers = dnsServers,
                    isUp = networkInterface.isUp,
                    isLoopback = networkInterface.isLoopback,
                    mtu = networkInterface.mtu
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return interfaces
    }

    /**
     * 获取当前活动的WiFi接口信息
     */
    @Suppress("DEPRECATION")
    fun getActiveWifiInfo(): NetworkInterfaceInfo? {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo

            // 获取WiFi接口
            val wifiInterface = NetworkInterface.getByName("wlan0")
                ?: return null

            // 获取IP地址
            val ipAddress = wifiInfo.ipAddress
            val formattedIp = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                (ipAddress shr 8) and 0xff,
                (ipAddress shr 16) and 0xff,
                (ipAddress shr 24) and 0xff
            )

            // 获取MAC地址
            val mac = wifiInterface.hardwareAddress?.joinToString(":") {
                "%02x".format(it)
            } ?: wifiInfo.macAddress

            return NetworkInterfaceInfo(
                name = wifiInterface.name,
                displayName = wifiInterface.displayName,
                ipAddresses = listOf(formattedIp),
                macAddress = mac,
                subnetMask = getSubnetMask(wifiInterface),
                dnsServers = getDnsServers(),
                isUp = wifiInterface.isUp,
                isLoopback = wifiInterface.isLoopback,
                mtu = wifiInterface.mtu
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 获取DNS服务器列表
     */
    private fun getDnsServers(): List<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val linkProperties = connectivityManager.getLinkProperties(network)
                linkProperties?.dnsServers?.map { it.hostAddress } ?: emptyList()
            } else {
                // 对于旧版本Android，尝试从系统属性读取
                val process = Runtime.getRuntime().exec("getprop")
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.filter { it.contains("dns") }
                        .map { it.split(": ")[1].trim('[', ']') }
                        .filter { it.isNotEmpty() }
                        .toList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 获取子网掩码
     */
    private fun getSubnetMask(networkInterface: NetworkInterface): String {
        return try {
            val addresses = networkInterface.interfaceAddresses
            addresses.firstOrNull()?.networkPrefixLength?.let { prefix ->
                // 将前缀长度转换为子网掩码
                val mask = (-1L shl (32 - prefix)).toInt()
                String.format(
                    "%d.%d.%d.%d",
                    mask shr 24 and 0xff,
                    mask shr 16 and 0xff,
                    mask shr 8 and 0xff,
                    mask and 0xff
                )
            } ?: "255.255.255.0" // 默认子网掩码
        } catch (e: Exception) {
            e.printStackTrace()
            "255.255.255.0"
        }
    }

    /**
     * 检查是否有网络连接
     */
    fun isNetworkConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork != null
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}