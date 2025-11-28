package com.example.what2do_today.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.data.What2DoRepository
import com.example.what2do_today.network.Plan
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
    data class Success(val plans: List<Plan>) : PlanUiState   // 🔁
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

    private val _selectedPlan = MutableStateFlow<Plan?>(null)   // 🔁 이름/타입 변경
    val selectedPlan: StateFlow<Plan?> = _selectedPlan

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation

    fun setCurrentLocation(lat: Double, lng: Double) {
        _currentLocation.value = lat to lng
    }

    // 자연어 + 위치 같이 서버로 보내는 함수
    fun loadCategories(latitude: Double?,
                       longitude: Double?,
                       query: String) {


        viewModelScope.launch {
            _categoryState.value = CategoryUiState.Loading
            runCatching {
                repo.fetchCategories(latitude, longitude, query)
            }.onSuccess { tags ->
                _categoryState.value = CategoryUiState.Success(tags)
            }.onFailure { e ->
                _categoryState.value = CategoryUiState.Error(e.message ?: "오류")
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
            runCatching { repo.fetchPlans(cats) }
                .onSuccess { _planState.value = PlanUiState.Success(it) }
                .onFailure { _planState.value = PlanUiState.Error(it.message ?: "오류") }
        }
    }

    fun selectPlan(plan: Plan) { _selectedPlan.value = plan }


}
