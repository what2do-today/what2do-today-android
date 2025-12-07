package com.example.what2do_today.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.network.TMapNetwork
import com.example.what2do_today.network.TMapRouteRequest
import com.example.what2do_today.network.Geometry  // 이미 있다면
import com.example.what2do_today.network.MapPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class TMapRouteViewModel : ViewModel() {

    private val _routePoints = MutableStateFlow<List<MapPoint>>(emptyList())
    val routePoints: StateFlow<List<MapPoint>> = _routePoints

    fun clearRoute() {
        _routePoints.value = emptyList()
    }

    /**
     * start: 출발점
     * waypoints: 경유지들 (0개 이상)
     * end: 도착점
     */
    fun loadWalkingRoute(
        start: MapPoint,
        waypoints: List<MapPoint>,
        end: MapPoint,
        apiKey: String
    ) {
        viewModelScope.launch {
            try {
                // TMAP은 X=lng, Y=lat
                val passListString = if (waypoints.isNotEmpty()) {
                    waypoints.joinToString("_") { "${it.lng},${it.lat}" }
                } else null

                val request = TMapRouteRequest(
                    startX = start.lng,
                    startY = start.lat,
                    endX = end.lng,
                    endY = end.lat,
                    passList = passListString,
                    startName = "출발지",
                    endName = "도착지"
                )

                Log.d("TMapRouteVM", "REQ: $request")

                val response = TMapNetwork.api.getWalkingRoute(
                    appKey = apiKey,
                    request = request
                )

                if (!response.isSuccessful) {
                    Log.e("TMapRouteVM", "API 실패: ${response.code()} ${response.errorBody()?.string()}")
                    _routePoints.value = emptyList()
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    _routePoints.value = emptyList()
                    return@launch
                }

                // Geometry → MapPoint 로 평탄화
                val allPoints = mutableListOf<MapPoint>()
                body.features.forEach { feature ->
                    allPoints.addAll(feature.geometry.getAllPointsAsMapPoint())
                }

                Log.d("TMapRouteVM", "경로 포인트 수: ${allPoints.size}")

                _routePoints.value = allPoints

            } catch (e: Exception) {
                Log.e("TMapRouteVM", "통신 에러: ${e.message}", e)
                _routePoints.value = emptyList()
            }
        }
    }
}
