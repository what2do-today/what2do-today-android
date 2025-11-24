package com.example.what2do_today.data

import com.example.what2do_today.network.*

class What2DoRepository {

    // 자연어 → 카테고리 리스트
    suspend fun fetchCategories(
        query: String
    ): List<String> {
        val res = NetworkModule.api.getCategories(query)
        return res.categories
    }

    // 선택한 카테고리 리스트 → 코스(플랜) 리스트
    suspend fun fetchPlans(categories: List<String>): List<Plan> {
        val res = NetworkModule.api.getPlans(categories)
        return res.plans
    }
}
