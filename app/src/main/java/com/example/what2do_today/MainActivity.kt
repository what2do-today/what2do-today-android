package com.example.what2do_today

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.what2do_today.location.LocationHelper
import com.example.what2do_today.navigation.AppNav
import com.kakao.vectormap.KakaoMapSdk

class MainActivity : ComponentActivity() {

    lateinit var locationHelper: LocationHelper
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 권한 런처 등록 (이건 팝업 안 뜸, 기능만 등록)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            // 결과 필요하면 여기서 로그 찍어도 됨
            // android.util.Log.d("MainActivity", "permission result: $result")
        }

        // ✅ 헬퍼 생성
        locationHelper = LocationHelper(
            activity = this,
            permissionLauncher = permissionLauncher
        )

        try {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        } catch (e: Exception) {
            Log.e("KakaoMap", "초기화 실패: ${e.message}")
        }

        // ✅ 앱 UI 띄우기
        setContent {


            AppNav(locationHelper = locationHelper)
        }


    }
}
