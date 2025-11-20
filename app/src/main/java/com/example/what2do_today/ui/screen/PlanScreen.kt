package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.what2do_today.network.Itinerary
import com.example.what2do_today.viewmodel.PlanUiState
import com.example.what2do_today.viewmodel.What2DoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    vm: What2DoViewModel,
    onSelectPlan: (Itinerary) -> Unit,
    onBack: () -> Unit
) {
    val state by vm.planState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("추천 코스") }) }) { inner ->
        when (val s = state) {
            is PlanUiState.Success -> {
                LazyColumn(
                    Modifier.padding(inner).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.itineraries) { it ->
                        ElevatedCard(onClick = { onSelectPlan(it) }) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Plan ${it.id}", style = MaterialTheme.typography.titleMedium)
                                Text("총거리 ${it.totalDistanceKm ?: "-"} km · 총소요 ${it.totalDurationMin ?: "-"}분 · 예산~${it.totalCostEstimate ?: "-"}원")
                                Text("스텝 ${it.steps.size}개  |  점수 ${it.score ?: "-"}")
                            }
                        }
                    }
                }
            }
            is PlanUiState.Error -> Column(Modifier.padding(inner).padding(16.dp)) {
                Text("불러오기 실패: ${s.message}", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onBack) { Text("뒤로") }
            }
            is PlanUiState.Loading -> Box(Modifier.padding(inner).fillMaxSize()) { CircularProgressIndicator() }
            PlanUiState.Idle -> Box(Modifier.padding(inner).fillMaxSize()) { Text("카테고리를 먼저 선택해 주세요") }
        }
    }
}
