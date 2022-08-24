package com.example.baselibrary.network

import com.example.baselibrary.BaseConstants
import com.example.baselibrary.api.BaseApi
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.internal.ConstructorConstructor
import com.google.gson.internal.bind.CollectionTypeAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

abstract class BaseProvider {
    lateinit var baseApi: BaseApi

    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(BaseConstants.DEFAULT_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(BaseConstants.DEFAULT_TIME_OUT, TimeUnit.SECONDS)

        val addInterceptor = addInterceptor(builder)
        val client = addInterceptor.build()
        val gsonBuilder = GsonBuilder()

        val retrofit = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        createApi(retrofit)
    }

    abstract fun getBaseUrl(): String

    abstract fun createApi(retrofit: Retrofit)

    open fun addInterceptor(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
    }
}