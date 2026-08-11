package com.nest.kanxue.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val uploadUrl = "http://console.nestbrowser.com/api/v1/"

    fun create(): ApiService {

        val logInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BASIC) // 也可以使用 Level.HEADERS 或 Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            // 上传大文件：默认写/读超时仅 10s，弱网下必然 timeout，这里显式放大
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .callTimeout(0, TimeUnit.MILLISECONDS) // 0 = 不限制整个调用的总时长
            .build()

        return Retrofit.Builder()
            .baseUrl(uploadUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}