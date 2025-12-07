package com.example.what2do_today.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.what2do_today.BuildConfig
import com.example.what2do_today.network.MapPoint
import com.example.what2do_today.ui.components.addColoredMarkers
import com.example.what2do_today.ui.components.drawRoute
import com.example.what2do_today.viewmodel.TMapRouteViewModel
import com.example.what2do_today.viewmodel.What2DoViewModel
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TMapResultScreen(
    sharedVm: What2DoViewModel,
    routeVm: TMapRouteViewModel = viewModel(),
    onBack: () -> Unit
) {
    // 1) 상태 구독
    val course by sharedVm.selectedCourse.collectAsState()
    val serverLocation by sharedVm.serverSearchLocation.collectAsState()
    val routePoints by routeVm.routePoints.collectAsState()

    // 2) 코스가 없으면 바로 빈 상태 UI
    if (course == null) {
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
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "선택된 코스가 없습니다."
                )
            }
        }
        return
    }

    val c = course!!
    val places = c.places

    // 3) 장소 → MapPoint 변환 (한 번만 계산)
    val placePoints by remember(places) {
        mutableStateOf(
            places.map { p ->
                MapPoint(lat = p.latitude, lng = p.longitude)
            }
        )
    }

    // 4) 출발 위치 계산 (서버 위치 우선, 없으면 첫 장소)
    val originForRoute by remember(serverLocation, placePoints) {
        mutableStateOf(
            when {
                serverLocation != null -> {
                    val (lat, lng) = serverLocation!!
                    MapPoint(lat, lng)
                }
                placePoints.isNotEmpty() -> placePoints.first()
                else -> null
            }
        )
    }

    // 5) 지도 마커용 포인트: 출발지(있으면) + 코스 장소들
    val markers by remember(originForRoute, placePoints) {
        mutableStateOf(
            buildList {
                if (originForRoute != null) add(originForRoute)
                addAll(placePoints)
            }
        )
    }

    // 6) 카메라 타겟
    val cameraTarget by remember(originForRoute, placePoints) {
        mutableStateOf(
            originForRoute
                ?: placePoints.firstOrNull()
                ?: MapPoint(37.5665, 126.9780) // 서울 시청 근처
        )
    }

    // 7) 출발 + 코스 준비되면 TMAP 보행자 경로 요청
    LaunchedEffect(originForRoute, placePoints) {
        if (originForRoute != null && placePoints.size >= 2) {
            val start = originForRoute!!
            val end = placePoints.last()
            val via = placePoints.drop(1).dropLast(1)

            routeVm.loadWalkingRoute(
                start = start,
                waypoints = via,
                end = end,
                apiKey = BuildConfig.TMAP_API_KEY
            )
        } else {
            routeVm.clearRoute()
        }
    }

    // ---------------- UI ----------------

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
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {

            // 지도
            ResultMapKakao(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                markers = markers as List<MapPoint>,
                routePoints = routePoints,
                cameraTarget = cameraTarget
            )

            // 상세 정보
            ResultCourseDetailSection(course = c)
        }
    }
}

/**
 * 코스 상세 정보 UI만 따로 분리
 */
@Composable
private fun ResultCourseDetailSection(course: com.example.what2do_today.network.Course) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.titleLarge
        )

        val distanceKm = course.totalDistanceMeters / 1000.0
        Text(
            text = "총거리 %.1f km · 장소 ${course.places.size}개"
                .format(distanceKm)
        )

        if (course.description.isNotBlank()) {
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))

        course.places.forEachIndexed { index, place ->
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

        // TODO: SNS 공유 버튼
        // TODO: 캘린더 저장 버튼
    }
}

/**
 * 카카오맵 + TMap 경로를 그리는 전용 Composable
 */
@Composable
fun ResultMapKakao(
    modifier: Modifier = Modifier,
    markers: List<MapPoint>,
    routePoints: List<MapPoint>,
    cameraTarget: MapPoint
) {
    val context = LocalContext.current

    // KakaoMap 인스턴스를 기억
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { androidContext ->
            MapView(androidContext).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("ResultMapKakao", "KakaoMap destroyed")
                        }

                        override fun onMapError(e: Exception?) {
                            Log.e("ResultMapKakao", "Map error: ${e?.message}", e)
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            Log.d("ResultMapKakao", "KakaoMap ready")
                            kakaoMap = map
                        }
                    }
                )
            }
        }
    )

    // KakaoMap + 상태가 준비될 때마다 마커/경로 갱신
    LaunchedEffect(kakaoMap, markers, routePoints, cameraTarget) {
        val map = kakaoMap ?: return@LaunchedEffect

        // 1) 카메라 이동
        val cameraLatLng = LatLng.from(cameraTarget.lat, cameraTarget.lng)
        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(cameraLatLng, 15)
        )

        // 2) 마커 표시
        if (markers.isNotEmpty()) {
            val kakaoMarkers = markers.map { p ->
                LatLng.from(p.lat, p.lng)
            }
            map.labelManager?.layer?.removeAll()
            addColoredMarkers(context, map, kakaoMarkers)
        }

        // 3) 경로 Polyline 표시
        if (routePoints.size >= 2) {
            val kakaoRoutePoints = routePoints.map { p ->
                LatLng.from(p.lat, p.lng)
            }
            map.routeLineManager?.layer?.removeAll()
            drawRoute(context, map, kakaoRoutePoints)
        }
    }
}
