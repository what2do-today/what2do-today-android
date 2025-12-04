package com.example.what2do_today.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.what2do_today.viewmodel.CategoryUiState
import com.example.what2do_today.viewmodel.What2DoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    vm: What2DoViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.categoryState.collectAsState()
    val selected by vm.selectedCategories.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("카테고리 선택") }) },
        bottomBar = {
            BottomAppBar {
                OutlinedButton(onClick = onBack) { Text("뒤로") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        vm.loadCourses()
                        onNext()
                    },
                    enabled = selected.isNotEmpty() && state is CategoryUiState.Success
                ) { Text("코스 보기") }
            }
        }
    ) { inner ->
        when (val s = state) {
            is CategoryUiState.Success -> {
                val cats = s.categories
                LazyColumn(
                    modifier = Modifier
                        .padding(inner)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text("가고 싶은 카테고리를 골라주세요 ") }
                    items(cats) { name ->
                        val checked = selected.contains(name)
                        FilterChip(
                            selected = checked,
                            onClick = {
                                val new = if (checked) selected - name else selected + name
                                vm.setSelectedCategories(new)
                            },
                            label = { Text(name) }
                        )
                    }
                }
            }
            is CategoryUiState.Error -> Column(
                Modifier.padding(inner).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("불러오기 실패: ${s.message}", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onBack) { Text("뒤로") }
            }
            is CategoryUiState.Loading -> Box(
                Modifier.padding(inner).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }
            CategoryUiState.Idle -> Box(
                Modifier.padding(inner).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { Text("메인 화면에서 먼저 카테고리를 받아오세요.") }
        }
    }
}
