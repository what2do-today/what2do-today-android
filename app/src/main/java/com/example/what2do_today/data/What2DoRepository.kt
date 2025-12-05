package com.example.what2do_today.data

import com.example.what2do_today.network.*

class What2DoRepository {

    //로그인
    //자연어만
    suspend fun fetchFirst(query: String): FirstResponse {
        return NetworkModule.api.getFirst(sentences = query)
    }

    suspend fun fetchSecond(
        sessionId: String,
        selectedTags: List<String>,
        latitude: Double?,
        longitude: Double?
    ): SecondResponse {
        return NetworkModule.api.getSecond(
            sessionId = sessionId,
            latitude = latitude,
            longitude = longitude,
            selectedTags = selectedTags
        )
    }

}
