package http

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("upload-txt")  // 替换为服务器的上传路径
    fun uploadFile(
        @Part file: MultipartBody.Part
    ): Call<String>
}
