package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.what2do_today.BuildConfig
import com.example.what2do_today.ui.components.ResultMap
import com.example.what2do_today.viewmodel.RouteViewModel
import com.example.what2do_today.viewmodel.What2DoViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    sharedVm: What2DoViewModel,
    onBack: () -> Unit
) {
    // ✅ 선택된 코스 (Course?)
    val course by sharedVm.selectedCourse.collectAsState()

    // ✅ 도보 루트 ViewModel
    val routeVm: RouteViewModel = viewModel()
    val routePoints by routeVm.routePoints.collectAsState()

    // ✅ 서버가 돌려준 검색 기준 위치 (searchLatitude, searchLongitude)
    val serverLocation by sharedVm.serverSearchLocation.collectAsState()

    // 서버 위치 → LatLng
    val startLatLng: LatLng? = serverLocation?.let { (lat, lng) ->
        LatLng(lat, lng)
    }

    // ✅ 코스 안의 장소들을 LatLng 리스트로 변환
    val placePoints = remember(course) {
        course?.places?.map { place ->
            LatLng(place.latitude, place.longitude)
        } ?: emptyList()
    }

    // ✅ 지도에 찍을 마커: (시작 위치 있으면) + 코스 장소들
    val markers = remember(startLatLng, placePoints) {
        buildList {
            if (startLatLng != null) add(startLatLng)
            addAll(placePoints)
        }
    }

    // ✅ 카메라 기본 타겟
    val cameraTarget: LatLng = startLatLng
        ?: placePoints.firstOrNull()
        ?: LatLng(37.5665, 126.9780) // fallback: 서울 시청

    val cameraZoom = if (markers.size >= 2) 13f else 15f

    // ✅ 출발 위치 + 코스가 준비되면 도보 루트 로딩
    LaunchedEffect(startLatLng, placePoints) {
        if (startLatLng != null && placePoints.isNotEmpty()) {
            routeVm.loadWalkingRoute(
                startLocation = startLatLng,
                points = placePoints,
                apiKey = BuildConfig.MAPS_API_KEY
            )
        } else {
            routeVm.clearRoute()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("코스 상세") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // 필요하면 여기서 상태 초기화도 가능
                            // routeVm.clearRoute()
                            onBack()
                        }
                    ) {
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
                markers = markers,
                routePoints = routePoints,
                cameraTarget = cameraTarget,
                cameraZoom = cameraZoom
            )

            // 2) 코스 상세 정보
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (course == null) {
                    Text("선택된 코스가 없습니다.")
                } else {
                    val c = course!!

                    Text(
                        text = c.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    val distanceKm = c.totalDistanceMeters / 1000.0
                    Text(
                        text = "총거리 %.1f km · 장소 ${c.places.size}개"
                            .format(distanceKm)
                    )

                    if (c.description.isNotBlank()) {
                        Text(
                            text = c.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))

                    c.places.forEachIndexed { index, place ->
                        Text(
                            text = "${index + 1}. ${place.name} (${place.tag})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // TODO: SNS 공유 버튼
            // TODO: 캘린더 저장 버튼 (현재 날짜 + course 정보 사용)
        }
    }
}
