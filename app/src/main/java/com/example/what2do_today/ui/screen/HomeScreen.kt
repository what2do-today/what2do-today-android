package com.example.what2do_today.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickWhat2DoScreen: () -> Unit,
    onClickCalendarScreen : () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("오늘 뭐하지?") })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 메인 기능 카드
            FeatureCard(
                title = "메인 기능",
                desc = "자연어로 하고 싶은 걸 입력해 보세요",
                onClick = onClickWhat2DoScreen
            )

            // 부가 기능 카드 (비활성/준비중)
            FeatureCard(
                title = "캘린더",
                desc = "나만의 일정을 확인해 보세요",
                onClick = onClickCalendarScreen
            )


            FeatureCard(
                title = "마이페이지",
                desc = "준비 중입니다",
                onClick = {},
                enabled = false
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    desc: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .alpha(alpha)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


