package com.example.what2do_today.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(
    private val activity: Activity,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>
) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(activity)

    /**
     * 버튼 눌렀을 때 호출됨
     * - 권한 있으면 → "이미 위치 권한이 허용되어 있습니다." Toast
     * - 권한 없으면 → 권한 요청 다이얼로그 launch()
     */
    fun requestLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("LocationHelper", "requestLocationPermission() called, fine=$fineGranted, coarse=$coarseGranted")

        if (fineGranted || coarseGranted) {
            // ✅ 이미 권한 허용됨 → 여기까지 들어오면 Toast는 무조건 떠야 함
            Toast.makeText(
                activity,
                "이미 위치 권한이 허용되어 있습니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d("LocationHelper", "No permission yet, launching permission dialog")

        // ✅ 권한 없으면 → 요청 (여기서 시스템 다이얼로그 떠야 함)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * 현재 위치 얻기 (권한 없으면 null/null)
     */
    suspend fun getCurrentLocation(): Pair<Double?, Double?> =
        suspendCancellableCoroutine { cont ->

            val fineGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            Log.d("LocationHelper", "getCurrentLocation() fine=$fineGranted, coarse=$coarseGranted")

            if (!fineGranted && !coarseGranted) {
                // 권한 없으면 현재 위치 못 받음
                cont.resume(null to null)
                return@suspendCancellableCoroutine
            }

            val tokenSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).addOnSuccessListener { loc ->
                if (!cont.isCompleted) {
                    if (loc != null) {
                        Log.d("LocationHelper", "Location success: ${loc.latitude}, ${loc.longitude}")
                        cont.resume(loc.latitude to loc.longitude)
                    } else {
                        Log.d("LocationHelper", "Location is null")
                        cont.resume(null to null)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("LocationHelper", "getCurrentLocation() failed", e)
                if (!cont.isCompleted) cont.resume(null to null)
            }

            cont.invokeOnCancellation {
                tokenSource.cancel()
            }
        }
}
