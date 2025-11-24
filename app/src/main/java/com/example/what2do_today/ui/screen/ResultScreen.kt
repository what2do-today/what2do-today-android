package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    val plan by sharedVm.selectedPlan.collectAsState()
    val mapVm: ResultMapViewModel = viewModel()
    val mapState by mapVm.state.collectAsState()

    // 코스가 바뀔 때마다 마커/카메라 갱신
    LaunchedEffect(plan?.id) {
        plan?.let { p ->
            val points = p.plan.mapNotNull { place ->
                val lat = place.lat; val lng = place.lng
                if (lat != null && lng != null) LatLng(lat, lng) else null
            }
            mapVm.setMarkers(points, zoom = if (points.size >= 2) 13f else 15f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("코스 상세") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        }
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

            // 2) 상세 정보
            Column(Modifier.padding(16.dp)) {
                if (plan == null) {
                    Text("선택된 코스가 없습니다.")
                } else {
                    Text("Plan ${plan!!.id}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "총거리 ${plan!!.totalDistanceKm ?: "-"} km · " +
                                "총소요 ${plan!!.totalDurationMin ?: "-"}분"
                    )
                    Divider()
                    plan!!.plan.forEachIndexed { i, place ->
                        Text("${i + 1}. ${place.name} (${place.category})")
                        place.address?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
