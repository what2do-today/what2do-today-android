package com.example.what2do_today.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.what2do_today.network.MapPoint
import com.example.what2do_today.network.toGmsLatLngList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.maps.android.compose.*

@Composable
fun ResultMapGoogle(
    modifier: Modifier = Modifier,
    markers: List<MapPoint>,
    routePoints: List<MapPoint>,
    cameraTarget: MapPoint,
    cameraZoom: Float = 15f
) {
    // 카메라 상태
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            GmsLatLng(cameraTarget.lat, cameraTarget.lng),
            cameraZoom
        )
    }

    // 카메라 애니메이션
    LaunchedEffect(cameraTarget, cameraZoom) {
        cameraState.animate(
            CameraUpdateFactory.newLatLngZoom(
                GmsLatLng(cameraTarget.lat, cameraTarget.lng),
                cameraZoom
            )
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = MapProperties(isMyLocationEnabled = false)
    ) {
        // 마커
        markers.forEachIndexed { idx, p ->
            Marker(
                state = MarkerState(
                    position = GmsLatLng(p.lat, p.lng)
                ),
                title = "${idx + 1}번째 장소"
            )
        }

        // Tmap에서 받은 경로를 polyline으로 그리기
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints.toGmsLatLngList(),
                color = Color.Blue,
                width = 8f
            )
        }
    }
}
