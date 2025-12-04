package com.example.what2do_today.ui.screen

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.what2do_today.location.LocationHelper
import com.example.what2do_today.viewmodel.CategoryUiState
import com.example.what2do_today.viewmodel.What2DoViewModel
import kotlinx.coroutines.launch

private const val DEFAULT_LAT_GANGNAM = 37.4979
private const val DEFAULT_LNG_GANGNAM = 127.0276

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun What2DoScreen(
    vm: What2DoViewModel,
    goPlan: () -> Unit,
    locationHelper: LocationHelper
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val categoryState by vm.categoryState.collectAsState()
    val scope = rememberCoroutineScope()

    val selectedCategories by vm.selectedCategories.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("메인 기능") }) }) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("자연어 쿼리") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.text.isNotBlank() && categoryState !is CategoryUiState.Loading) {
                            scope.launch {


                                val (rawLat, rawLng) = locationHelper.getCurrentLocation()
                                val lat = rawLat ?: DEFAULT_LAT_GANGNAM
                                val lng = rawLng ?: DEFAULT_LNG_GANGNAM
                                /*val (lat, lng) = locationHelper.getCurrentLocation()
                                if (lat != null && lng != null) {
                                    vm.setCurrentLocation(lat, lng)
                                }*/
                                vm.loadCategories(lat, lng, query.text)
                            }
                        }
                    }
                )
            )

            Button(
                onClick = {
                    if (query.text.isNotBlank() && categoryState !is CategoryUiState.Loading) {
                        scope.launch {

                            val (rawLat, rawLng) = locationHelper.getCurrentLocation()
                            val lat = rawLat ?: DEFAULT_LAT_GANGNAM
                            val lng = rawLng ?: DEFAULT_LNG_GANGNAM

                            /*val (lat, lng) = locationHelper.getCurrentLocation()
                            if (lat != null && lng != null) {
                                vm.setCurrentLocation(lat, lng)
                            }*/
                            vm.loadCategories(lat, lng, query.text)
                        }
                    }
                },
                enabled = categoryState !is CategoryUiState.Loading && query.text.isNotBlank()
            ) {
                Text(
                    if (categoryState is CategoryUiState.Loading)
                        "요청 중..."
                    else
                        "카테고리 받기"
                )
            }

            Spacer(Modifier.height(16.dp))

            // 3) 아래에 카테고리 선택 UI(원래 CategoryScreen 내용)
            when (val s = categoryState) {
                is CategoryUiState.Success -> {
                    val cats = s.categories

                    Text("가고 싶은 카테고리를 골라주세요")
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(cats) { name ->
                            val checked = selectedCategories.contains(name)
                            FilterChip(
                                selected = checked,
                                onClick = {
                                    val new = if (checked)
                                        selectedCategories - name
                                    else
                                        selectedCategories + name
                                    vm.setSelectedCategories(new)
                                },
                                label = { Text(name) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ✅ 코스 보기 (예전 Plan → Course)
                    Button(
                        onClick = {
                            // 예전 vm.loadPlans()에서 이름만 loadCourses()로 바꾼 함수라고 가정
                            vm.loadCourses()
                            goPlan()
                        },
                        enabled = selectedCategories.isNotEmpty()
                    ) {
                        Text("코스 보기")
                    }
                }

                is CategoryUiState.Error -> {
                    Text(
                        "카테고리 불러오기 실패: ${s.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is CategoryUiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                CategoryUiState.Idle -> {
                    Text("자연어를 입력하고 '카테고리 받기'를 눌러주세요.")
                }
            }
        }
    }
}