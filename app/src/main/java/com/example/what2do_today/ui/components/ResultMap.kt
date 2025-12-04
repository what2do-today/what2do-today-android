package com.example.what2do_today.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    routePoints: List<LatLng>,
    cameraTarget: LatLng,
    cameraZoom: Float
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cameraTarget, cameraZoom)
    }

    LaunchedEffect(cameraTarget, cameraZoom) {
        cameraState.animate(
            CameraUpdateFactory.newLatLngZoom(cameraTarget, cameraZoom)
        )
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

        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = Color.Blue,
                width = 8f
            )
        }
    }
}