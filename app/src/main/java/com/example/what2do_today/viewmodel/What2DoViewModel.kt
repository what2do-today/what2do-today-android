package com.example.what2do_today.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.what2do_today.data.What2DoRepository
import com.example.what2do_today.network.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------- UI State 정의 ----------------

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

// ---------------- ViewModel ----------------

class What2DoViewModel(
    private val repo: What2DoRepository = What2DoRepository()
) : ViewModel() {

    // ---------------- 카테고리 상태 ----------------
    private val _categoryState =
        MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val categoryState: StateFlow<CategoryUiState> = _categoryState

    // ---------------- 코스 리스트 상태 ----------------
    private val _courseState =
        MutableStateFlow<CourseUiState>(CourseUiState.Idle)
    val courseState: StateFlow<CourseUiState> = _courseState

    // ---------------- 위치 상태 ----------------

    // 디바이스에서 받은 현재 위치 (LocationHelper 등)
    private val _currentLocation =
        MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation

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

    // ---------------- 세션 ----------------
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId

    // ---------------- 카테고리 선택 상태 ----------------
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    fun setSelectedCategories(new: Set<String>) {
        _selectedCategories.value = new
    }

    // ---------------- 선택된 코스 ----------------
    private val _selectedCourse = MutableStateFlow<Course?>(null)  // 타입: Course
    val selectedCourse: StateFlow<Course?> = _selectedCourse

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    // ---------------- 서버 통신 ----------------
    /**
     * query + (옵션) currentLat/Lng 을 받아서
     *
     * 1) /first (문장만) 호출
     *   - location 있으면 → 그 태그 바로 사용
     *   - location 없으면:
     *        - currentLat/Lng 있으면 /second(sessionId + 위치) 호출
     *        - 그것도 없으면 → 1단계 태그만 사용
     */


    private val _isLocationUnknownFromFirst = MutableStateFlow(false)
    val isLocationUnknownFromFirst: StateFlow<Boolean> = _isLocationUnknownFromFirst


    fun loadCategories(
        query: String
    ) {
        viewModelScope.launch {
            _categoryState.value = CategoryUiState.Loading

            runCatching {
                // 1️⃣ 1단계 호출 (쿼리만 보냄)
                val first = repo.fetchFirst(query)

                // 세션 아이디 저장
                _sessionId.value = first.sessionId

                _isLocationUnknownFromFirst.value = false

                val loc = first.extractedLocation
                if (
                    loc == "알 수 없음"
                ) {
                    _isLocationUnknownFromFirst.value = true
                    _serverSearchLocation.value = null
                }


                // 최종 태그 리스트는 first에서 온 것만 사용
                first.activityTags
            }.onSuccess { tags ->
                _categoryState.value = CategoryUiState.Success(tags)
            }.onFailure { e ->
                _categoryState.value =
                    CategoryUiState.Error(e.message ?: "카테고리 요청 중 오류")
            }
        }
    }

    // ---------------- 코스 추천 ----------------
    // 선택된 카테고리(tags)로 코스 추천 받기


    fun loadCourses() {

        val cats = _selectedCategories.value
        val session = _sessionId.value


        if (cats.isEmpty() || session == null) return

        viewModelScope.launch {
            _courseState.value = CourseUiState.Loading

            runCatching {

                val selectedTags = cats.toList()


                val isUnknown = _isLocationUnknownFromFirst.value
                val serverLoc = _serverSearchLocation.value
                val currentLoc = _currentLocation.value

                val finalLoc: Pair<Double, Double>? =
                    if (isUnknown) currentLoc else serverLoc

                val latForSecond = finalLoc?.first
                val lngForSecond = finalLoc?.second


                // 2) second 호출
                val second = repo.fetchSecond(
                    sessionId = session,
                    latitude = latForSecond,     // null이면 Retrofit이 안 보냄
                    longitude = lngForSecond,
                    selectedTags = selectedTags
                )
                //3)서버가 보내준 주소 갱신
                setServerSearchLocation(second.searchLatitude, second.searchLongitude)
                //4)코스 반환
                second.courses



            }.onSuccess { courses ->
                _courseState.value = CourseUiState.Success(courses)
            }.onFailure { e ->
                _courseState.value =
                    CourseUiState.Error(e.message ?: "오류")
            }
        }
    }
}