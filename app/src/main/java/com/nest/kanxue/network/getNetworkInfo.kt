package com.nest.kanxue.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Build
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface

object getNetworkInfo {


    fun getInfo(context: Context): JSONObject{

        val networkJSON = JSONObject()


        val allInterfacesJSONArray = JSONArray()
        val wifiJSONArray = JSONArray()


        val networkInfoReader = NetworkInfoReader(context)
        // 获取所有网络接口信息
        val allInterfaces = networkInfoReader.getAllNetworkInterfaces()


        allInterfaces.forEach { info ->
            val interfacesJSON = JSONObject()
            Log.d("sb" , "getNetworkInfo getInfo= "+info.name)
            interfacesJSON.put("Name:", info.name )
            interfacesJSON.put("Display Name:", info.displayName )
            interfacesJSON.put("IP Addresses:", info.ipAddresses.joinToString() )
            interfacesJSON.put("MAC Address:", info.macAddress )
            interfacesJSON.put("Subnet Mask:", info.subnetMask )
            interfacesJSON.put("DNS Servers:", info.dnsServers.joinToString() )
            interfacesJSON.put("Is Up:", info.isUp )
            interfacesJSON.put("MTU:", info.mtu )
            allInterfacesJSONArray.put(interfacesJSON)
        }
        networkJSON.put("allInterfaces", allInterfacesJSONArray)


        // 获取WiFi接口信息
        val wifiInfo = networkInfoReader.getActiveWifiInfo()
        wifiInfo?.let {
            Log.d("sb" , "getNetworkInfo wifiInfo= "+it)

            val wifiJSON = JSONObject()
            wifiJSON.put("Name:", it.name )
            wifiJSON.put("Display Name:", it.displayName )
            wifiJSON.put("IP Addresses:", it.ipAddresses.firstOrNull())
            wifiJSON.put("MAC Address:", it.macAddress )
            wifiJSON.put("Subnet Mask:", it.subnetMask )
            wifiJSON.put("DNS Servers:", it.dnsServers.joinToString() )
            wifiJSON.put("Is Up:", it.isUp )
            wifiJSON.put("MTU:", it.mtu )
            wifiJSONArray.put(wifiJSON)
        }
        networkJSON.put("wifiInterfaces", wifiJSONArray.length())


        Log.d("sb" , "getNetworkInfo getInfo= $networkJSON")

        return networkJSON

    }
}