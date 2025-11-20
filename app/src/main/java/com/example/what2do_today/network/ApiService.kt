package com.example.what2do_today.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // 카테고리 추천: (점수는 보낼 때만, 받을 때는 리스트만)
    @POST("suggest")
    suspend fun getCategories(@Body body: SuggestRequest): CategoryListResponse

    // 선택 카테고리들만 전송 → 플랜(코스) 목록 수신
    @POST("plan/itineraries")
    suspend fun getPlans(@Body body: PlanRequest): PlanResponse
}
