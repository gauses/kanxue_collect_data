package http

import com.nest.kanxue.http.ApiService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ApiService {
    @Multipart
    @POST("upload")  // 替换为你的实际上传端点
    fun uploadFile(@Part file: MultipartBody.Part): Call<UploadResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://your-server-url/"  // 替换为你的服务器地址

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun create(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
} 