package com.example.baselibrary.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface BaseApi {
    @Streaming //大文件时要加不然会OOM
    @GET
    suspend fun downloadFile(@Url fileUrl: String?): Response<ResponseBody>
}