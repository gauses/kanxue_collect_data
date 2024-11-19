package com.nest.kanxue.cert

import android.content.Context
import android.security.KeyChain
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class CertificateReader() {

    companion object {
        // 系统证书存储位置
        private const val SYSTEM_CA_PATH = "/system/etc/security/cacerts"
        private const val USER_CA_PATH = "/data/misc/user/0/cacerts-added"

        // KeyStore类型
        private const val KEYSTORE_TYPE = "AndroidCAStore"
    }

    fun getInfo(context: Context): JSONObject{
        val certsJSONObject = JSONObject()
        certsJSONObject.put("system-cert", getSystemCertsInfo(context) )
        certsJSONObject.put("user-cert", getUserCertsInfo(context) )
        return certsJSONObject

    }


    private fun getSystemCertsInfo(context: Context): JSONArray{
        val systemCerts = readSystemCertificates()

        val systemJSONOArray = JSONArray()
        systemCerts.forEach { cert ->
            val systemJSONObject = JSONObject()
            systemJSONObject.put("cert.alias", cert.alias)
            systemJSONObject.put("cert.subject", cert.subject)
            systemJSONObject.put("cert.issuer", cert.issuer)
            systemJSONObject.put("cert.validFrom", cert.validFrom)
            systemJSONObject.put("cert.validTo", cert.validTo)
            systemJSONObject.put("cert.serialNumber", cert.serialNumber)
            systemJSONObject.put("cert.version", cert.version)
            systemJSONObject.put("cert.path", cert.path)
            systemJSONOArray.put(systemJSONObject)
        }
        return systemJSONOArray
    }

    private fun getUserCertsInfo(context: Context): JSONArray{
        val userCerts = readUserCertificates()

        val userJSONOArray = JSONArray()
        userCerts.forEach { cert ->
            val userJSONObject = JSONObject()
            userJSONObject.put("cert.alias", cert.alias)
            userJSONObject.put("cert.subject", cert.subject)
            userJSONObject.put("cert.issuer", cert.issuer)
            userJSONObject.put("cert.validFrom", cert.validFrom)
            userJSONObject.put("cert.validTo", cert.validTo)
            userJSONObject.put("cert.serialNumber", cert.serialNumber)
            userJSONObject.put("cert.version", cert.version)
            userJSONObject.put("cert.path", cert.path)
            userJSONOArray.put(userJSONObject)
        }
        return userJSONOArray
    }



    /**
     * 读取系统证书信息
     */
    fun readSystemCertificates(): List<CertificateInfo> {
        val certificates = mutableListOf<CertificateInfo>()

        try {
            // 方法1：通过 KeyStore 读取
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            keyStore.load(null, null)

            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                val cert = keyStore.getCertificate(alias) as? X509Certificate

                cert?.let {
                    certificates.add(
                        CertificateInfo(
                            alias = alias,
                            subject = it.subjectDN.name,
                            issuer = it.issuerDN.name,
                            validFrom = it.notBefore,
                            validTo = it.notAfter,
                            serialNumber = it.serialNumber.toString(16),
                            version = it.version
                        )
                    )
                }
            }

//            // 方法2：直接读取证书文件
//            val systemCaDir = File(SYSTEM_CA_PATH)
//            if (systemCaDir.exists() && systemCaDir.isDirectory) {
//                systemCaDir.listFiles()?.forEach { file ->
//                    if (file.isFile && file.extension == "0") {
//                        try {
//                            FileInputStream(file).use { fis ->
//                                val cert = java.security.cert.CertificateFactory
//                                    .getInstance("X.509")
//                                    .generateCertificate(fis) as X509Certificate
//
//                                certificates.add(
//                                    CertificateInfo(
//                                        alias = file.nameWithoutExtension,
//                                        subject = cert.subjectDN.name,
//                                        issuer = cert.issuerDN.name,
//                                        validFrom = cert.notBefore,
//                                        validTo = cert.notAfter,
//                                        serialNumber = cert.serialNumber.toString(16),
//                                        version = cert.version,
//                                        path = file.absolutePath
//                                    )
//                                )
//                            }
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return certificates
    }


//    /**
//     * 读取用户安装的证书
//     */
//    fun readUserCertificates1(): List<CertificateInfo> {
//        val certificates = mutableListOf<CertificateInfo>()
//
//        try {
//            // 使用 AndroidCAStore 类型的 KeyStore
//            val keyStore = KeyStore.getInstance("AndroidCAStore")
//            keyStore.load(null, null)
//
//
//            val path: String? = null
//
//            // 获取所有别名
//            val aliases = keyStore.aliases()
//            while (aliases.hasMoreElements()) {
//                val alias = aliases.nextElement()
//                try {
//                    // 只获取用户安装的证书（通常以 "user:" 开头）
//                    if (alias.startsWith("user:")) {
//                        val cert = keyStore.getCertificate(alias) as? X509Certificate
//                        cert?.let {
//                            certificates.add(
//                                CertificateInfo(
//                                    alias = alias,
//                                    subject = it.subjectDN.name,
//                                    issuer = it.issuerDN.name,
//                                    serialNumber = it.serialNumber.toString(),
//                                    validFrom = it.notBefore,
//                                    validTo = it.notAfter,
//                                    version = it.version,
//                                    path = ""
//                                )
//                            )
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("", "Error reading certificate for alias: $alias", e)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("", "Error reading user certificates", e)
//        }
//
//        return certificates
//    }

    /**
     * 读取用户安装的证书
     */
    fun readUserCertificates(): List<CertificateInfo> {
        val certificates = mutableListOf<CertificateInfo>()

        try {
            val userCaDir = File(USER_CA_PATH)
            if (userCaDir.exists() && userCaDir.isDirectory) {
                userCaDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        try {
                            FileInputStream(file).use { fis ->
                                val cert = java.security.cert.CertificateFactory
                                    .getInstance("X.509")
                                    .generateCertificate(fis) as X509Certificate

                                certificates.add(
                                    CertificateInfo(
                                        alias = file.nameWithoutExtension,
                                        subject = cert.subjectDN.name,
                                        issuer = cert.issuerDN.name,
                                        validFrom = cert.notBefore,
                                        validTo = cert.notAfter,
                                        serialNumber = cert.serialNumber.toString(16),
                                        version = cert.version,
                                        path = file.absolutePath
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return certificates
    }
}

/**
 * 证书信息数据类
 */
data class CertificateInfo(
    val alias: String,
    val subject: String,
    val issuer: String,
    val validFrom: java.util.Date,
    val validTo: java.util.Date,
    val serialNumber: String,
    val version: Int,
    val path: String? = null
)