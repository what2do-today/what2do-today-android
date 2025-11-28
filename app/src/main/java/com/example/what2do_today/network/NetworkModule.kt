package com.example.what2do_today.network

import ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "http://3.38.166.217:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)    // 연결 시도 타임아웃
        .readTimeout(30, TimeUnit.SECONDS)       // 응답 대기 타임아웃 (너한테 제일 중요)
        .writeTimeout(10, TimeUnit.SECONDS)      // 요청 전송 타임아웃
        .callTimeout(30, TimeUnit.SECONDS)       // 요청 전체 제한 시간
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
