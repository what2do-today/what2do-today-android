package com.example.what2do_today.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun ResultMap(
    modifier: Modifier = Modifier,
    markers: List<LatLng>,
    cameraTarget: LatLng?,
    cameraZoom: Float
) {
    val cameraState = rememberCameraPositionState {
        position = if (cameraTarget != null)
            CameraPosition.fromLatLngZoom(cameraTarget, cameraZoom)
        else
            CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 12f) // 서울 기본
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = MapProperties(isMyLocationEnabled = false)
    ) {
        markers.forEachIndexed { idx, ll ->
            Marker(
                state = MarkerState(position = ll),
                title = "${idx + 1}번째 장소"
            )
        }
        // 폴리라인이 필요해지면 여기서 Polyline(points = ...) 추가
    }
}
