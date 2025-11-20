package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.what2do_today.ui.components.ResultMap
import com.example.what2do_today.viewmodel.ResultMapViewModel
import com.example.what2do_today.viewmodel.What2DoViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    sharedVm: What2DoViewModel,
    onBack: () -> Unit
) {
    val plan by sharedVm.selectedItinerary.collectAsState()
    val mapVm: ResultMapViewModel = viewModel()
    val mapState by mapVm.state.collectAsState()

    LaunchedEffect(plan?.id) {
        plan?.let { itn ->
            val points = itn.steps.mapNotNull { s ->
                val lat = s.place.lat; val lng = s.place.lng
                if (lat != null && lng != null) LatLng(lat, lng) else null
            }
            mapVm.setMarkers(points, zoom = if (points.size >= 2) 13f else 15f)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("코스 상세") }) }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            // 1) 지도
            ResultMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                markers = mapState.markers,
                cameraTarget = mapState.cameraTarget,
                cameraZoom = mapState.cameraZoom
            )

            // 2) 상세 정보 (기존 UI)
            Column(Modifier.padding(16.dp)) {
                if (plan == null) {
                    Text("선택된 코스가 없습니다.")
                } else {
                    Text("Plan ${plan!!.id}", style = MaterialTheme.typography.titleLarge)
                    Text("총거리 ${plan!!.totalDistanceKm ?: "-"} km · 총소요 ${plan!!.totalDurationMin ?: "-"}분")
                    Divider()
                    plan!!.steps.forEachIndexed { i, step ->
                        Text("${i + 1}. ${step.place.name} (${step.place.category})")
                        step.place.address?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}