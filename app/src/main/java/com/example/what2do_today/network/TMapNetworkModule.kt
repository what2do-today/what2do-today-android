package com.example.what2do_today.network

import TMapApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TMapNetwork {

    private const val BASE_URL = "https://apis.openapi.sk.com/"

    // 로그 설정 (개발 할 때만 BODY, 배포 시에는 NONE 추천)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        // 👇 [추가] 네트워크가 느릴 때 앱이 무한정 대기하지 않도록 시간 제한 설정
        .connectTimeout(30, TimeUnit.SECONDS) // 서버 연결 대기 시간
        .readTimeout(30, TimeUnit.SECONDS)    // 데이터 읽기 대기 시간
        .writeTimeout(30, TimeUnit.SECONDS)   // 데이터 쓰기 대기 시간
        .build()

    val api: TMapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // 👇 앞서 만든 인터페이스 이름(TMapApiService)과 맞춰주세요
            .create(TMapApiService::class.java)
    }
}