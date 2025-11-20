package com.example.what2do_today.network

// 1) 카테고리 추천 전달
data class SuggestRequest(
    val query: String,
    val categoryScores: Map<String, Int>? = null
)

// 1) 카테고리 리스트 반환
data class CategoryListResponse(
    val categories: List<String>
)

// 2) 카테고리 리스트 전달
data class PlanRequest(
    val categories: List<String>
)

// 장소/코스 모델 (Result에서 상세 표시)
data class Place(
    val id: String,
    val name: String,
    val category: String,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val rating: Double? = null,
    val priceLevel: Int? = null
)

data class Leg(
    val fromPlaceId: String,
    val toPlaceId: String,
    val distanceKm: Double,
    val durationMin: Int,
    val mode: String? = null      // "WALK" | "TRANSIT" | "DRIVE" 등
)

data class Step(
    val place: Place,
    val plannedDurationMin: Int? = null,
    val costEstimate: Int? = null
)

data class Itinerary(
    val id: String,
    val steps: List<Step>,
    val legs: List<Leg>,
    val totalCostEstimate: Int? = null,
    val totalDistanceKm: Double? = null,
    val totalDurationMin: Int? = null,
    val score: Double? = null
)

data class PlanResponse(
    val itineraries: List<Itinerary>
)
