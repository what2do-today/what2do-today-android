package com.example.what2do_today.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.data.What2DoRepository
import com.example.what2do_today.network.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface CategoryUiState {
    object Idle : CategoryUiState
    object Loading : CategoryUiState
    data class Success(val categories: List<String>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

sealed interface CourseUiState {
    object Idle : CourseUiState
    object Loading : CourseUiState
    data class Success(val courses: List<Course>) : CourseUiState
    data class Error(val message: String) : CourseUiState
}

class What2DoViewModel(
    private val repo: What2DoRepository = What2DoRepository()
) : ViewModel() {

    // ---------------- 카테고리 상태 ----------------
    private val _categoryState =
        MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val categoryState: StateFlow<CategoryUiState> = _categoryState

    // 선택한 카테고리
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    fun setSelectedCategories(new: Set<String>) {
        _selectedCategories.value = new
    }

    // ---------------- 코스 리스트 상태 ----------------
    private val _courseState =
        MutableStateFlow<CourseUiState>(CourseUiState.Idle)
    val courseState: StateFlow<CourseUiState> = _courseState

    // 선택된 코스 (결과 화면에서 사용)
    private val _selectedCourse = MutableStateFlow<Course?>(null)  // 타입: Course
    val selectedCourse: StateFlow<Course?> = _selectedCourse

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    // ---------------- 위치 상태 ----------------

    // 디바이스에서 받은 현재 위치 (LocationHelper 등)
    private val _currentLocation =
        MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> =
        _currentLocation

    fun setCurrentLocation(lat: Double, lng: Double) {
        _currentLocation.value = lat to lng
    }

    // 서버가 돌려주는 검색 기준 위치 (searchLatitude, searchLongitude)
    private val _serverSearchLocation =
        MutableStateFlow<Pair<Double, Double>?>(null)
    val serverSearchLocation: StateFlow<Pair<Double, Double>?> =
        _serverSearchLocation

    fun setServerSearchLocation(lat: Double, lng: Double) {
        _serverSearchLocation.value = lat to lng
    }

    // ---------------- 서버 통신 ----------------

    // 자연어 + 위치 → recommend 호출해서
    // - 서버 검색 위치 저장
    // - 카테고리(activityTags) 저장
    fun loadCategories(
        latitude: Double?,
        longitude: Double?,
        query: String
    ) {
        viewModelScope.launch {
            _categoryState.value = CategoryUiState.Loading

            runCatching {
                repo.fetchRecommend(latitude, longitude, query)
            }.onSuccess { res ->
                // 1) 서버 검색 기준 위치
                setServerSearchLocation(res.searchLatitude, res.searchLongitude)
                // 2) 카테고리 태그
                _categoryState.value =
                    CategoryUiState.Success(res.activityTags)
            }.onFailure { e ->
                _categoryState.value =
                    CategoryUiState.Error(e.message ?: "오류")
            }
        }
    }

    // 선택된 카테고리(tags)로 코스 추천 받기
    fun loadCourses() {
        val cats = _selectedCategories.value
        if (cats.isEmpty()) return

        viewModelScope.launch {
            _courseState.value = CourseUiState.Loading

            runCatching {
                repo.fetchCourses(cats.toList())
            }.onSuccess { courses ->
                _courseState.value = CourseUiState.Success(courses)
            }.onFailure { e ->
                _courseState.value =
                    CourseUiState.Error(e.message ?: "오류")
            }
        }
    }
}
