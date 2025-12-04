package com.example.what2do_today.viewmodel

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
        if (points.isEmpty()) return

        val origin = "${startLocation.latitude},${startLocation.longitude}"
        val destinationLatLng = points.last()
        val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"

        val waypoints = if (points.size > 1) {
            points.dropLast(1).joinToString("|") { p ->
                "${p.latitude},${p.longitude}"
            }
        } else null

        viewModelScope.launch {
            try {
                val res = GoogleMapsNetwork.directionsApi.getWalkingRoute(
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints,
                    apiKey = apiKey
                )

                val encoded = res.routes.firstOrNull()
                    ?.overview_polyline
                    ?.points

                _routePoints.value = if (encoded != null) {
                    decodePolyline(encoded)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _routePoints.value = emptyList()
            }
        }
    }

    fun clearRoute() {
        _routePoints.value = emptyList()
    }
}
