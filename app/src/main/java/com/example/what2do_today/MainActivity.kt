package com.example.what2do_today

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.what2do_today.location.LocationProvider
import com.example.what2do_today.navigation.AppNav
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var locationProvider: LocationProvider

    // 🔔 위치 권한 요청 런처 (앱 처음 켰을 때 한 번)
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* 결과는 따로 안 써도 됨. 이후 getCurrentLocation()에서 알아서 처리 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LocationProvider 준비
        locationProvider = LocationProvider(this)

        // ✅ 앱 시작 시 권한 체크 + 필요하면 한 번만 팝업
        ensureLocationPermissionOnce()

        setContent {
            // AppNav에 "위치 요청 함수" 전달
            AppNav(
                onRequestLocation = { callback ->
                    // Activity의 lifecycleScope로 suspend 함수 호출
                    lifecycleScope.launch {
                        val (lat, lng) = locationProvider.getCurrentLocation()
                        callback(lat, lng)
                    }
                }
            )
        }
    }

    /**
     * 앱 켤 때 딱 한 번 호출해서 위치 권한 없으면 요청.
     */
    private fun ensureLocationPermissionOnce() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        // 이미 허용 상태면 아무 것도 안 함
    }
}
