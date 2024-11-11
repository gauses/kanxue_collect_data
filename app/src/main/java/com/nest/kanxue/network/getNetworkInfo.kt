package com.nest.kanxue.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Build
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface

object getNetworkInfo {

    // 计算子网掩码
    fun getSubnetMask(prefixLength: Int): String {
        val mask = (0xffffffff shl (32 - prefixLength)).toInt()
        return InetAddress.getByAddress(
            byteArrayOf(
                (mask shr 24 and 0xff).toByte(),
                (mask shr 16 and 0xff).toByte(),
                (mask shr 8 and 0xff).toByte(),
                (mask and 0xff).toByte()
            )
        ).hostAddress
    }


    fun getNetworkInterfaceInfo():Map<String, String?>  {
        var interfacesMap = HashMap<String, String?>()
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (!networkInterface.isLoopback && networkInterface.isUp) {
                println("Interface Name: ${networkInterface.displayName}")
                interfacesMap.put("Interface Name" , networkInterface.displayName)

                // 获取 MAC 地址
                val macAddress = networkInterface.hardwareAddress?.joinToString(":") { "%02X".format(it) }
                println("MAC Address: $macAddress")
                interfacesMap.put("MAC Address" , macAddress)

                // 获取 IP 地址和子网掩码
                networkInterface.interfaceAddresses.forEach { address ->
                    val ip = address.address.hostAddress
                    val subnetPrefixLength = address.networkPrefixLength
                    val subnetMask = getSubnetMask(subnetPrefixLength.toInt())
                    println("IP Address: $ip")
                    println("Subnet Mask: $subnetMask")
                    interfacesMap.put("IP Address" , ip)
                    interfacesMap.put("Subnet Mask" , subnetMask)

                }
            }
        }
        return interfacesMap
    }


    fun getDnsServers(context: Context): List<String> {
        val dnsServers = mutableListOf<String>()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork

        if (network != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(network)
            linkProperties?.dnsServers?.forEach { dns ->
                dnsServers.add(dns.hostAddress ?: "")
            }
        }
        return dnsServers
    }


    fun getInfo(context: Context): JSONObject{
        val networkJSON = JSONObject()
        val ipMapInfo = getNetworkInterfaceInfo()
        networkJSON.put("Interface Name" , ipMapInfo.get("Interface Name"))
        networkJSON.put("MAC Address" , ipMapInfo.get("MAC Address"))
        networkJSON.put("IP Address" , ipMapInfo.get("IP Address"))
        networkJSON.put("Subnet Mask" , ipMapInfo.get("Subnet Mask"))

        networkJSON.put("DNS Servers" , getDnsServers(context))

        return networkJSON

    }
}