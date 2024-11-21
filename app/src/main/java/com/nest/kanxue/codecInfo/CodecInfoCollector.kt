import android.media.MediaCodecList
import android.media.MediaCodecInfo
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class CodecInfoCollector {
    private val TAG = "CodecInfoCollector"

    /**
     * 获取所有编解码器信息，以 JSONObject 形式返回
     * @return 包含所有编解码器详细信息的 JSONObject
     */
    fun collectCodecInfo(): JSONObject {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos

        return JSONObject().apply {
            put("totalCodecs", codecInfos.size)
            put("collectionTime", System.currentTimeMillis())
            put("codecs", JSONArray().apply {
                codecInfos.forEach { codec ->
                    try {
                        put(getCodecJson(codec))
                    } catch (e: Exception) {
                        put(JSONObject().apply {
                            put("error", "获取编解码器信息失败: ${e.message}")
                        })
                    }
                }
            })
        }
    }

    /**
     * 获取单个编解码器的 JSON 信息
     */
    private fun getCodecJson(codec: MediaCodecInfo): JSONObject {
        return JSONObject().apply {
            put("name", codec.name)
            put("isEncoder", codec.isEncoder) //isEncoder = true：表示是编码器（Encoder） ------ isEncoder = false：表示是解码器（Decoder）
            put("isSoftwareOnly", codec.isSoftwareOnly)
            put("isVendor", codec.isVendor)
            put("isHardwareAccelerated", codec.isHardwareAccelerated)

            // 支持的媒体类型
            put("supportedTypes", JSONArray().apply {
                codec.supportedTypes.forEach { type ->
                    put(JSONObject().apply {
                        put("mimeType", type)
                        try {
                            val capabilities = codec.getCapabilitiesForType(type)

                            // 颜色格式
                            put("colorFormats", JSONArray().apply {
                                capabilities?.colorFormats?.forEach { format ->
                                    put(format)
                                }
                            })

                            // 视频能力
                            capabilities?.videoCapabilities?.let { videoCapabilities ->
                                put("videoCapabilities", JSONObject().apply {
                                    try {
                                        put("supportedFrameRates", videoCapabilities.supportedFrameRates.toString())
                                        put("supportedWidths", videoCapabilities.supportedWidths.toString())
                                        put("supportedHeights", videoCapabilities.supportedHeights.toString())
                                        put("bitrateRange", videoCapabilities.bitrateRange.toString())

                                        // 添加一些常用分辨率是否支持的信息
                                        put("supports1080p", videoCapabilities.isSizeSupported(1920, 1080))
                                        put("supports4K", videoCapabilities.isSizeSupported(3840, 2160))
                                        put("supports720p", videoCapabilities.isSizeSupported(1280, 720))
                                    } catch (e: Exception) {
                                        put("error", "获取视频能力详情失败: ${e.message}")
                                    }
                                })
                            }

                            // 音频能力
                            capabilities?.audioCapabilities?.let { audioCapabilities ->
                                put("audioCapabilities", JSONObject().apply {
                                    try {
                                        put("supportedSampleRates", JSONArray().apply {
                                            audioCapabilities.supportedSampleRates?.forEach { rate ->
                                                put(rate)
                                            }
                                        })
                                        put("maxInputChannelCount", audioCapabilities.maxInputChannelCount)
                                        put("bitrateRange", audioCapabilities.bitrateRange.toString())
                                    } catch (e: Exception) {
                                        put("error", "获取音频能力详情失败: ${e.message}")
                                    }
                                })
                            }

                            // 编码器特有的属性
                            if (codec.isEncoder) {
                                capabilities?.encoderCapabilities?.let { encoderCapabilities ->
                                    put("encoderCapabilities", JSONObject().apply {
                                        try {
                                            put("complexityRange", encoderCapabilities.complexityRange.toString())
                                            put("qualityRange", encoderCapabilities.qualityRange.toString())
                                        } catch (e: Exception) {
                                            put("error", "获取编码器能力详情失败: ${e.message}")
                                        }
                                    })
                                }
                            }

                        } catch (e: Exception) {
                            put("error", "获取编解码器能力时出错: ${e.message}")
                        }
                    })
                }
            })
        }
    }

    /**
     * 获取指定类型的编解码器列表的 JSON 表示
     */
    fun findCodecsByTypeAsJson(mimeType: String, isEncoder: Boolean = false): JSONObject {
        return JSONObject().apply {
            put("mimeType", mimeType)
            put("isEncoder", isEncoder)
            put("codecs", JSONArray().apply {
                try {
                    val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
                    codecList.codecInfos
                        .filter { codec ->
                            codec.isEncoder == isEncoder &&
                                    codec.supportedTypes.contains(mimeType)
                        }
                        .forEach { codec ->
                            put(getCodecJson(codec))
                        }
                } catch (e: Exception) {
                    put(JSONObject().apply {
                        put("error", "查找编解码器失败: ${e.message}")
                    })
                }
            })
        }
    }

    /**
     * 将 JSON 信息打印到日志
     */
    fun logCodecInfo() {
        try {
            val jsonInfo = collectCodecInfo()
            Log.d(TAG, jsonInfo.toString(2)) // 使用缩进格式化输出
        } catch (e: Exception) {
            Log.e(TAG, "打印编解码器信息时出错", e)
        }
    }
}