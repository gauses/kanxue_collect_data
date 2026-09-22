package com.nest.kanxue.cert

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
    val path: String? = null,


    val tBSCertificate: ByteArray? = null,
    val signature: ByteArray? = null,
    val sigAlgName: String? = null,
    val sigAlgOID: String? = null,
    val sigAlgParams: ByteArray? = null,
    val issuerUniqueID: BooleanArray? = null ,
    val subjectUniqueID: BooleanArray? = null ,
    val keyUsage: BooleanArray? = null ,
    val extendedKeyUsage: List<String>? = null ,
    val basicConstraints: Int? = null,
    val subjectAlternativeNames: Collection<List<*>>? = null,
    val issuerAlternativeNames: Collection<List<*>>? = null,








    )