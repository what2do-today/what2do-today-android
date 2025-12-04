package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.what2do_today.network.Course
import com.example.what2do_today.viewmodel.CourseUiState
import com.example.what2do_today.viewmodel.What2DoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    vm: What2DoViewModel,
    onSelectCourse: (Course) -> Unit,
    onBack: () -> Unit
) {
    val state by vm.courseState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("추천 코스") }) }
    ) { inner ->
        when (val s = state) {
            is CourseUiState.Success -> {
                val courses = s.courses

                LazyColumn(
                    modifier = Modifier
                        .padding(inner)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses) { course ->
                        ElevatedCard(
                            onClick = { onSelectCourse(course) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 코스 이름
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // 총 거리 (m → km)
                                val distanceKm = course.totalDistanceMeters / 1000.0
                                Text(
                                    text = "총거리 %.1f km".format(distanceKm)
                                )

                                // 장소 개수
                                Text("장소 ${course.places.size}개")

                                // 설명
                                if (course.description.isNotBlank()) {
                                    Text(
                                        text = course.description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is CourseUiState.Error -> Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "불러오기 실패: ${s.message}",
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = onBack) { Text("뒤로") }
            }

            is CourseUiState.Loading -> Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            CourseUiState.Idle -> Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("카테고리를 먼저 선택해 주세요")
            }
        }
    }
}
