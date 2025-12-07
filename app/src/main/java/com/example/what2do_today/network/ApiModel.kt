package com.example.what2do_today.network

import com.google.gson.annotations.SerializedName

// 로그인


data class FirstResponse(
    val sessionId: String,
    val activityTags: List<String>,
    val extractedLocation: String?
)

// ------------------------------------------------------
// 1. /api/v1/recommend 응답 전체
// ------------------------------------------------------
data class SecondResponse(
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
//길찾기
data class DirectionsResponse(
    val routes: List<Route>,
    val status: String?,          // OK, ZERO_RESULTS, OVER_QUERY_LIMIT ...
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String  // 인코딩된 polyline 문자열
)