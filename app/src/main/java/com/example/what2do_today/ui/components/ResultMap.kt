package com.example.what2do_today.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
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
    val defaultCenter = LatLng(37.5665, 126.9780) // 기본 중심 (서울 시청)

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            cameraTarget ?: defaultCenter,
            cameraZoom
        )
    }

    // 카메라 타깃/줌 바뀔 때 애니메이션 이동
    val target by rememberUpdatedState(cameraTarget)
    val zoom by rememberUpdatedState(cameraZoom)

    LaunchedEffect(target, zoom) {
        target?.let {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(it, zoom)
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
        properties = MapProperties(isMyLocationEnabled = false)
    ) {
        // 마커 찍기 (순서 표시)
        markers.forEachIndexed { idx, ll ->
            Marker(
                state = MarkerState(position = ll),
                title = "${idx + 1}번째 장소"
            )
        }

        // 마커가 2개 이상이면, 리스트 순서대로 선(폴리라인) 그리기
        if (markers.size >= 2) {
            Polyline(
                points = markers,       // ⭐ 리스트 순서대로 연결
                color = Color.Blue,
                width = 8f
            )
        }
    }
}
