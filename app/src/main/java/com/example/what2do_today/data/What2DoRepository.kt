package com.example.what2do_today.data

import com.example.what2do_today.network.*

class What2DoRepository {

    // Main → 서버: query(+scores) 전송 → 카테고리 "이름 리스트" 수신
    suspend fun fetchCategories(
        query: String,
        categoryScores: Map<String, Int>? = null
    ): List<String> {
        val res = NetworkModule.api.getCategories(
            SuggestRequest(query = query, categoryScores = categoryScores)
        )
        return res.categories
    }

    // Category → 서버: 선택 카테고리만 전송 → 코스 목록 수신
    suspend fun fetchPlans(categories: List<String>): List<Itinerary> {
        val res = NetworkModule.api.getPlans(PlanRequest(categories = categories))
        return res.itineraries
    }
}
