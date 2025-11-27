package com.example.what2do_today.ui.screen

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
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
    goCategory: () -> Unit,
    locationHelper: LocationHelper
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val uiState by vm.categoryState.collectAsState()
    val scope = rememberCoroutineScope()

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
                        if (query.text.isNotBlank() && uiState !is CategoryUiState.Loading) {
                            scope.launch {
                                val (lat, lng) = locationHelper.getCurrentLocation()
                                if (lat != null && lng != null) {
                                    vm.setCurrentLocation(lat, lng)
                                }
                                vm.loadCategories(query.text, lat, lng)
                                goCategory()
                            }
                        }
                    }
                )
            )

            Button(
                onClick = {
                    if (query.text.isNotBlank() && uiState !is CategoryUiState.Loading) {
                        scope.launch {
                            val (lat, lng) = locationHelper.getCurrentLocation()
                            if (lat != null && lng != null) {
                                vm.setCurrentLocation(lat, lng)
                            }
                            vm.loadCategories(query.text, lat, lng)
                            goCategory()
                        }
                    }
                },
                enabled = uiState !is CategoryUiState.Loading && query.text.isNotBlank()
            ) {
                Text(
                    if (uiState is CategoryUiState.Loading)
                        "요청 중..."
                    else
                        "카테고리 받기"
                )
            }
        }
    }
}
