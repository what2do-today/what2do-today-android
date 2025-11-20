package com.example.what2do_today.ui.screen

import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.YearMonth


data class CalendarEvent(
    val date: LocalDate,
    val title: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    onBack: () -> Unit
) {
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val monthEvents = remember(visibleMonth, events) {
        events.filter { YearMonth.from(it.date) == visibleMonth }
    }

    val selectedDayEvents = remember(selectedDate, events) {
        events.filter { it.date == selectedDate }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("캘린더") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 월 변경 + 현재 월 표시
            MonthHeader(
                visibleMonth = visibleMonth,
                onPrevMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) }
            )

            Spacer(Modifier.height(12.dp))

            // 요일 헤더 (일~토)
            WeekDaysHeader()

            Spacer(Modifier.height(8.dp))

            // 달력 (날짜 그리드)
            CalendarMonthGrid(
                month = visibleMonth,
                events = monthEvents,
                selectedDate = selectedDate,
                onDayClick = { day -> selectedDate = day }
            )

            Spacer(Modifier.height(16.dp))

            // 선택된 날짜 + 일정 리스트
            Text(
                text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 일정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            if (selectedDayEvents.isEmpty()) {
                Text(
                    text = "등록된 일정이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // 한 줄씩 일정 표시
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    selectedDayEvents.forEach { ev ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "· ${ev.title}",      // 한 줄 설명
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    visibleMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "이전 달"
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "다음 달"
            )
        }
    }
}

@Composable
private fun WeekDaysHeader() {
    val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    events: List<CalendarEvent>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    // 일요일 = 0, 월요일 = 1 ...
    val firstDayIndex = when (firstDayOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 0
    }

    // 1) 앞쪽 공백(null) + 실제 날짜들 리스트
    val days: MutableList<LocalDate?> = mutableListOf()

    // 앞에 비어있는 칸들(null)
    repeat(firstDayIndex) {
        days.add(null)
    }

    // 실제 날짜들
    for (day in 1..daysInMonth) {
        days.add(month.atDay(day))
    }

    // 2) 뒤쪽도 주(7칸) 단위로 딱 떨어지도록 null로 채우기
    while (days.size % 7 != 0) {
        days.add(null)
    }

    // 3) 7개씩 끊어서 주 단위로 만들기
    val weeks: List<List<LocalDate?>> = days.chunked(7)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)   // 정사각형 유지
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date == null) {
                            // 공백 칸 (아무것도 그리지 않음)
                        } else {
                            val hasEvent = events.any { it.date == date }
                            val isSelected = date == selectedDate

                            DayCell(
                                date = date,
                                hasEvent = hasEvent,
                                isSelected = isSelected,
                                onClick = { onDayClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    hasEvent: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(Modifier.height(4.dp))

        // 일정 있는 날은 색 점 또는 작은 배경으로 표시
        if (hasEvent) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    )
            )
        }
    }
}

