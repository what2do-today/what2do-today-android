package com.example.what2do_today.data

import com.example.what2do_today.network.*

class What2DoRepository {

    //로그인

    // 자연어 → 카테고리 리스트
    suspend fun fetchRecommend(
        latitude: Double?,
        longitude: Double?,
        query: String
    ): RecommendResponse {
        return NetworkModule.api.getRecommend(
            latitude = latitude,
            longitude = longitude,
            sentences = query
        )
    }

    // 🌟 CategoryScreen에서 태그만 필요할 때 편의 함수
    suspend fun fetchCategories(
        latitude: Double?,
        longitude: Double?,
        query: String
    ): List<String> {
        val res = fetchRecommend(latitude, longitude, query)
        return res.activityTags
    }

    // 선택한 카테고리 리스트 → 코스(플랜) 리스트
    suspend fun fetchPlans(categories: List<String>): List<Plan> {
        val res = NetworkModule.api.getPlans(categories)
        return res.plans
    }


}
