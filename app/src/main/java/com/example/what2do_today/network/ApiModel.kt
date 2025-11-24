package com.example.what2do_today.network

import com.google.gson.annotations.SerializedName


// 자연어 → 카테고리 리스트 응답
data class CategoryListResponse(
    @SerializedName("tags")
    val categories: List<String>
)


// 카테고리 -> 코스 추천
data class Place(
    val id: String,
    val name: String,
    val category: String,
    val address: String? = null,
    val lat: Double? = null, //위도
    val lng: Double? = null, //경도
    val rating: Double? = null // 별점
)


data class Plan(
    val id: String,
    val plan: List<Place>,
    val score: Double? = null,      // 추천 점수
    val totalDistanceKm: Double? = null, // 총 이동거리
    val totalDurationMin: Int? = null, // 총 이동시간
    val totalCostEstimate: Int? = null // 총 예산
)

data class Plans(
    val plans: List<Plan>
)
