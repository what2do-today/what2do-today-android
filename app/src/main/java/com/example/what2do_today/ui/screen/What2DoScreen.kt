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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun What2DoScreen(
    vm: What2DoViewModel,
    goPlan: () -> Unit,
    locationHelper: LocationHelper
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val categoryState by vm.categoryState.collectAsState()
    val selectedCategories by vm.selectedCategories.collectAsState()

    // 공통으로 쓸 “카테고리 요청 트리거”
    fun requestCategories() {
        if (query.text.isNotBlank() && categoryState !is CategoryUiState.Loading) {
            vm.loadCategories(query = query.text)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("메인 기능") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔎 자연어 입력창
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
                        requestCategories()
                    }
                )
            )

            // 🔘 버튼들 (위치 권한 + 카테고리 요청)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        locationHelper.requestLocationPermission()
                    }
                ) {
                    Text("위치 권한 요청")
                }

                Button(
                    onClick = { requestCategories() },
                    enabled = categoryState !is CategoryUiState.Loading &&
                            query.text.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (categoryState is CategoryUiState.Loading)
                            "요청 중..."
                        else
                            "카테고리 받기"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 📚 카테고리 결과 처리
            when (val s = categoryState) {
                is CategoryUiState.Success -> {

                    // ⇩ 문장에서 위치를 못 뽑은 경우에만 현재 위치를 얻어서 ViewModel에 저장
                    LaunchedEffect(s) {
                        val isUnknown = vm.isLocationUnknownFromFirst.value
                        if (isUnknown) {
                            val (rawLat, rawLng) = locationHelper.getCurrentLocation()
                            if (rawLat != null && rawLng != null) {
                                vm.setCurrentLocation(rawLat, rawLng)
                            }
                        }
                    }

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

                    Button(
                        onClick = {
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
