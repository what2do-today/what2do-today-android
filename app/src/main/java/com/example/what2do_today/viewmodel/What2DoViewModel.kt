package com.example.what2do_today.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.data.What2DoRepository
import com.example.what2do_today.network.Itinerary
import com.example.what2do_today.network.Place
import com.example.what2do_today.network.Step
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface CategoryUiState {
    object Idle : CategoryUiState
    object Loading : CategoryUiState
    data class Success(val categories: List<String>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

sealed interface PlanUiState {
    object Idle : PlanUiState
    object Loading : PlanUiState
    data class Success(val itineraries: List<Itinerary>) : PlanUiState
    data class Error(val message: String) : PlanUiState
}

class What2DoViewModel(
    private val repo: What2DoRepository = What2DoRepository()
) : ViewModel() {

    private val _categoryState = MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val categoryState: StateFlow<CategoryUiState> = _categoryState

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories

    private val _planState = MutableStateFlow<PlanUiState>(PlanUiState.Idle)
    val planState: StateFlow<PlanUiState> = _planState

    private val _selectedItinerary = MutableStateFlow<Itinerary?>(null)
    val selectedItinerary: StateFlow<Itinerary?> = _selectedItinerary

    fun loadCategories(query: String, categoryScores: Map<String, Int>?) {
        viewModelScope.launch {
            _categoryState.value = CategoryUiState.Loading
            runCatching { repo.fetchCategories(query, categoryScores) }
                .onSuccess { _categoryState.value = CategoryUiState.Success(it) }
                //.onFailure { _categoryState.value = CategoryUiState.Error(it.message ?: "오류") }

                // 🚧 서버 실패 시에도 임시 리스트 반환 (테스트용)
                .onFailure {
                    val dummyCats = listOf("식사", "카페", "전시회", "산책", "게임방")
                    _categoryState.value = CategoryUiState.Success(dummyCats)
                }

        }
    }

    fun setSelectedCategories(list: List<String>) {
        _selectedCategories.value = list
    }


    fun loadPlans() {
        val cats = _selectedCategories.value
        viewModelScope.launch {
            _planState.value = PlanUiState.Loading
            runCatching { repo.fetchPlans(cats) }   // ← 카테고리만 전송
                .onSuccess { _planState.value = PlanUiState.Success(it) }
                //.onFailure { _planState.value = PlanUiState.Error(it.message ?: "오류") }


                // 🚧 서버 실패 시 더미 데이터로 대체 (테스트용)
                .onFailure {
                    val dummyPlans = listOf(
                        Itinerary(
                            id = "1",
                            steps = listOf(
                                Step(Place("1", "스타벅스 강남역", "카페")),
                                Step(Place("2", "메가박스 강남", "영화관")),
                                Step(Place("3", "도산공원", "산책"))
                            ),
                            legs = emptyList(),
                            totalCostEstimate = 20000,
                            totalDistanceKm = 3.2,
                            totalDurationMin = 120,
                            score = 4.5
                        ),
                        Itinerary(
                            id = "2",
                            steps = listOf(
                                Step(Place("4", "을지로 노포식당", "식사")),
                                Step(Place("5", "익선동 카페거리", "카페")),
                                Step(Place("6", "청계천 산책로", "산책"))
                            ),
                            legs = emptyList(),
                            totalCostEstimate = 15000,
                            totalDistanceKm = 2.5,
                            totalDurationMin = 90,
                            score = 4.2
                        )
                    )
                    _planState.value = PlanUiState.Success(dummyPlans)
                }
        }
    }

    fun selectItinerary(plan: Itinerary) { _selectedItinerary.value = plan }
}
