package com.example.what2do_today.network

import com.google.gson.annotations.SerializedName

// 로그인

// ------------------------------------------------------
// 1. /api/v1/recommend 응답 전체
// ------------------------------------------------------
data class RecommendResponse(
    val nlpTimeMs: Long,
    val apiTimeMs: Long,
    val totalTimeMs: Long,
    val location: String?,
    val placeKeywords: List<String>,
    val activity: List<String>,
    val activityTags: List<String>,   // 🌟 CategoryScreen에서 사용하는 태그
    val timeSpecific: String?,
    val timeLengthHour: Int,
    val companionType: String?,
    val companionNum: Int,
    val budgetType: String?,
    val budgetAmount: Int,
    val searchLatitude: Double,
    val searchLongitude: Double,
    val nearbyPlaces: List<NearbyPlace>,
    val courses: List<Course>
)

// 주변 장소 (지도 마커용)
data class NearbyPlace(
    val placeId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val address: String,
    val tag: String
)

// 추천 코스 한 개
data class Course(
    val name: String,                 // "Recommended Course (pc방, 패스트푸드)"
    val description: String,          // "Optimized route through 2 locations."
    val totalDistanceMeters: Int,
    val places: List<CoursePlace>     // 순서대로 방문할 장소들
)

// 코스 안의 장소
data class CoursePlace(
    val placeId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val address: String,
    val tag: String
)

// ------------------------------------------------------
// 2. /api/v1/plans 응답 (기존 코스 API 그대로 쓰고 싶을 때)

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
