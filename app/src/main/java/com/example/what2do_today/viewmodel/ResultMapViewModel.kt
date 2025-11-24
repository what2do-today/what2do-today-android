package com.example.what2do_today.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ResultMapState(
    val markers: List<LatLng> = emptyList(),
    val cameraTarget: LatLng? = null,
    val cameraZoom: Float = 14f
)

class ResultMapViewModel : ViewModel() {
    private val _state = MutableStateFlow(ResultMapState())
    val state: StateFlow<ResultMapState> = _state

    fun setMarkers(points: List<LatLng>, zoom: Float = 14f) {
        _state.value = ResultMapState(
            markers = points,
            cameraTarget = points.firstOrNull(), // ⭐ 첫 번째 장소를 중심으로
            cameraZoom = zoom
        )
    }
}
