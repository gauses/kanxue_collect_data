package com.nest.kanxue.mcc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import android.os.Build
import android.telephony.CellInfoLte
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONObject

class TelephonyPropertyCollector(private val context: Context) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val TAG = "TelephonyProperties"

    private val propertiesMap = mutableMapOf<String, Any?>()
    private val propertiesJSON = JSONObject()


    /**
     * 获取所有运营商属性并返回JSON字符串
     */
    fun getTelephonyPropertiesJson(): JSONObject {
        getAllTelephonyProperties()
        return propertiesJSON
    }


    @SuppressLint("MissingPermission", "WrongConstant")
    fun getAllTelephonyProperties() {
        try {
            // 基础网络信息
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val allowsEmergencyCalls = telephonyManager::class.java
                        .getDeclaredMethod("getAllowsEmergencyCalls")
                        .invoke(telephonyManager) as? Boolean
                    logProperty("allowsEmergencyCalls", allowsEmergencyCalls)
                } catch (e: Exception) {
                    logProperty("allowsEmergencyCalls", "API 不可用: ${e.message}")
                }
            }
            logProperty("getCallState", telephonyManager.callState)
            logProperty("getDataActivity", telephonyManager.dataActivity)
            logProperty("getDataState", telephonyManager.dataState)
            logProperty("getDeviceId", tryGetDeviceId())
            logProperty("getDeviceSoftwareVersion", telephonyManager.deviceSoftwareVersion)
            logProperty("getImei", tryGetImei())
            logProperty("getMeid", tryGetMeid())
            logProperty("getLine1Number", tryGetLine1Number())
            logProperty("getManufacturerCode", telephonyManager.manufacturerCode)
//            logProperty("getNai", telephonyManager.nai) //NO
            logProperty("getNetworkCountryIso", telephonyManager.networkCountryIso)
            logProperty("getNetworkOperator", telephonyManager.networkOperator)
            logProperty("getNetworkOperatorName", telephonyManager.networkOperatorName)
            logProperty("getNetworkSpecifier", telephonyManager.networkSpecifier)
            logProperty("getNetworkType", telephonyManager.networkType)
            logProperty("getPhoneCount", telephonyManager.phoneCount)
            logProperty("getPhoneType", telephonyManager.phoneType)
            logProperty("getSimCarrierId", telephonyManager.simCarrierId)
            logProperty("getSimCountryIso", telephonyManager.simCountryIso)
            logProperty("getSimOperator", telephonyManager.simOperator)
            logProperty("getSimOperatorName", telephonyManager.simOperatorName)
            logProperty("getSimSerialNumber", tryGetSimSerialNumber())
            logProperty("getSimState", telephonyManager.simState)
            logProperty("getSubscriberId", tryGetSubscriberId())
            logProperty("getVoiceMailAlphaTag", telephonyManager.voiceMailAlphaTag)
            logProperty("getVoiceMailNumber", tryGetVoiceMailNumber())
            logProperty("hasCarrierPrivileges", telephonyManager.hasCarrierPrivileges())
            logProperty("isDataEnabled", telephonyManager.isDataEnabled)
            logProperty("isSmsCapable", telephonyManager.isSmsCapable)
            logProperty("isVoiceCapable", telephonyManager.isVoiceCapable)
            logProperty("isWorldPhone", telephonyManager.isWorldPhone)

            // Android 10 (Q) 及以上版本的属性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 获取小区带宽信息
                val bandwidths = telephonyManager.allCellInfo?.mapNotNull { cellInfo ->
                    when {
                        // LTE 小区信号
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && cellInfo is CellInfoLte -> {
                            try {
                                val signalStrength = cellInfo.cellSignalStrength
                                signalStrength::class.java.getMethod("getBandwidth").invoke(signalStrength) as? Int
                            } catch (e: Exception) {
                                null
                            }
                        }
                        // 5G NR 小区信号
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo::class.java.simpleName == "CellInfoNr" -> {
                            try {
                                val signalStrength = cellInfo::class.java.getMethod("getCellSignalStrength").invoke(cellInfo)
                                signalStrength?.let {
                                    it::class.java.getMethod("getBandwidth").invoke(it) as? Int
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        else -> null
                    }
                }
                logProperty("getCellBandwidths", mapOf(
                    "带宽值" to bandwidths,
                    "单位" to "kHz",
                    "说明" to "对于LTE，这个值表示载波带宽；对于5G NR，这个值表示组件载波带宽"
                ))
                logProperty("getDataNetworkType", telephonyManager.dataNetworkType)
                logProperty("isMultiSimSupported", telephonyManager.isMultiSimSupported)
                logProperty("getVoiceNetworkType", telephonyManager.voiceNetworkType)
                logProperty("getServiceState", telephonyManager.serviceState)
            }

            // Android 11 (R) 及以上版本的属性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                logProperty("getCarrierIdFromSimMccMnc", telephonyManager.carrierIdFromSimMccMnc)
                logProperty("isDataRoamingEnabled", telephonyManager.isDataRoamingEnabled)
//                logProperty("isManualNetworkSelectionAllowed", telephonyManager.isManualNetworkSelectionAllowed) //NO
//                logProperty("isMobileDataPolicyEnabled", telephonyManager.isMobileDataPolicyEnabled)
                logProperty("isNetworkRoaming", telephonyManager.isNetworkRoaming)
//                logProperty("isVolteMobileDataPolicyEnabled", telephonyManager.isVolteAvailable)
//                logProperty("isWifiCallingAvailable", telephonyManager.isWifiCallingAvailable)
            }

            // Android 12 (S) 及以上版本的属性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                logProperty("getActiveModemCount", telephonyManager.activeModemCount)
                try {
                    logProperty("getBootstrapAuthenticationToken", telephonyManager::class.java
                        .getMethod("getBootstrapAuthenticationToken")
                        .invoke(telephonyManager))
                } catch (e: Exception) {
                    logProperty("getBootstrapAuthenticationToken", "不支持此API")
                }

                try {
                    val imsManager = context.getSystemService(Context.TELEPHONY_IMS_SERVICE)
                    if (imsManager != null) {
                        logProperty("getImsRegistrationState", "IMS服务可用")
                    } else {
                        logProperty("getImsRegistrationState", "IMS服务不可用")
                    }
                } catch (e: Exception) {
                    logProperty("getImsRegistrationState", "不支持此API")
                }

//                logProperty("getSupportedRadioAccessFamily", telephonyManager.supportedRadioAccessFamily)
            }

            // 信号强度相关信息
            try {
                val signalStrength = telephonyManager.signalStrength
                logProperty("getSignalStrength", mapOf(
                    "level" to signalStrength?.level,
                    "gsmSignalStrength" to signalStrength?.gsmSignalStrength,
                    "gsmBitErrorRate" to signalStrength?.gsmBitErrorRate,
                    "cdmaDbm" to signalStrength?.cdmaDbm,
                    "cdmaEcio" to signalStrength?.cdmaEcio,
                    "evdoDbm" to signalStrength?.evdoDbm,
                    "evdoEcio" to signalStrength?.evdoEcio,
                    "evdoSnr" to signalStrength?.evdoSnr
                ))
            } catch (e: SecurityException) {
                logProperty("getSignalStrength", "需要权限: ${Manifest.permission.ACCESS_FINE_LOCATION}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "获取属性时发生错误", e)
        }
    }

    private fun tryGetDeviceId(): String {
        return try {
            telephonyManager.deviceId ?: "需要权限或不可用"
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun tryGetImei(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei ?: "不可用"
            } else {
                "需要 Android O 或更高版本"
            }
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun tryGetMeid(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.meid ?: "不可用"
            } else {
                "需要 Android O 或更高版本"
            }
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun tryGetLine1Number(): String {
        return try {
            telephonyManager.line1Number ?: "不可用"
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_SMS}, ${Manifest.permission.READ_PHONE_NUMBERS}"
        }
    }

    private fun tryGetSimSerialNumber(): String {
        return try {
            telephonyManager.simSerialNumber ?: "不可用"
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun tryGetSubscriberId(): String {
        return try {
            telephonyManager.subscriberId ?: "不可用"
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun tryGetVoiceMailNumber(): String {
        return try {
            telephonyManager.voiceMailNumber ?: "不可用"
        } catch (e: SecurityException) {
            "需要权限: ${Manifest.permission.READ_PHONE_STATE}"
        }
    }

    private fun logProperty(propertyName: String, value: Any?) {
        Log.d(TAG, "$propertyName: $value")
        propertiesJSON.put(propertyName, value)

    }
}
