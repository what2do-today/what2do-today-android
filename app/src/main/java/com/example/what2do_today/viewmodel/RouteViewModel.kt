package com.example.what2do_today.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.network.GoogleMapsNetwork
import com.example.what2do_today.ui.components.decodePolyline
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints

    fun loadWalkingRoute(
        startLocation: LatLng,
        points: List<LatLng>,
        apiKey: String
    ) {
        if (points.isEmpty()) {
            Log.d("RouteVM", "loadWalkingRoute: points is empty, clear route")
            _routePoints.value = emptyList()
            return
        }

        val origin = "${startLocation.latitude},${startLocation.longitude}"
        //Google Maps SDK
        val destinationLatLng = points.last()
        //REST API
        val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"

        val waypoints = if (points.size > 1) {
            points
                .dropLast(1)
                .joinToString("|") { p -> "${p.latitude},${p.longitude}" }
        } else null

        Log.d("RouteVM", "REQ origin=$origin dest=$destination waypoints=$waypoints")

        viewModelScope.launch {
            try {
                val res = GoogleMapsNetwork.directionsApi.getWalkingRoute(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints,
                    apiKey = apiKey
                )

                Log.d("RouteVM", "RES routes.size=${res.routes.size}")

                val encoded = res.routes.firstOrNull()
                    ?.overview_polyline
                    ?.points

                Log.d("RouteVM", "encoded polyline=${encoded?.take(40)}")

                val decoded = if (!encoded.isNullOrBlank()) {
                    decodePolyline(encoded)
                } else {
                    emptyList()
                }

                Log.d("RouteVM", "decoded size=${decoded.size}")


                if (decoded.isNotEmpty()) {
                    // ✅ 정상 케이스: Directions가 polyline 줬을 때
                    _routePoints.value = decoded
                } else {
                    // ⚠ ZERO_RESULTS 등 → fallback: 출발 + 장소들을 직선으로 이어서 보여주기
                    Log.w("RouteVM", "No route from Directions, use fallback straight lines")

                    val fallback = buildList {
                        add(startLocation)   // 출발점
                        addAll(points)       // 코스 장소들
                    }

                    _routePoints.value = fallback
                }
            } catch (e: Exception) {
                Log.e("RouteVM", "directions error", e)
                _routePoints.value = emptyList()
            }
        }
    }

    fun clearRoute() {
        Log.d("RouteVM", "clearRoute")
        _routePoints.value = emptyList()
    }
}
