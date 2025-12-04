package com.example.what2do_today.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.what2do_today.location.LocationHelper
import com.example.what2do_today.ui.screen.*
import com.example.what2do_today.viewmodel.What2DoViewModel
import java.time.LocalDate

@Composable
fun AppNav(
    locationHelper: LocationHelper
) {
    val nav = rememberNavController()
    val vm: What2DoViewModel = viewModel()

    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onClickWhat2DoScreen = { nav.navigate(Routes.WHAT2DO) },
                onClickCalendarScreen = { nav.navigate(Routes.CALENDAR) }
            )
        }

        composable(Routes.CALENDAR) {
            val dummyEvents = listOf(
                CalendarEvent(LocalDate.now(), "점심 약속"),
                CalendarEvent(LocalDate.now().plusDays(1), "스터디 모임"),
                CalendarEvent(LocalDate.now().plusDays(3), "영화 보기")
            )
            CalendarScreen(
                events = dummyEvents,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.WHAT2DO) {
            What2DoScreen(
                vm = vm,
                goPlan = { nav.navigate(Routes.PLAN) },
                locationHelper = locationHelper
            )
        }

        composable(Routes.CATEGORY) {
            CategoryScreen(
                vm = vm,
                onNext = { nav.navigate(Routes.PLAN) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.PLAN) {
            PlanScreen(
                vm = vm,
                onSelectCourse = { course ->
                    vm.selectCourse(course)
                    nav.navigate(Routes.RESULT)
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                sharedVm = vm,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
