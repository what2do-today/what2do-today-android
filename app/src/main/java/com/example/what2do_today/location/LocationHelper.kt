package com.example.what2do_today.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val activity: Activity) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(activity)

    // 권한 런처 내부에서 초기화하기 위해 lazy로 둠
    private val permissionLauncher by lazy {
        (activity as ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // OS가 권한 상태 기억하므로 추가 처리 필요 없음.
        }
    }

    /** 앱 실행 시 호출해서 권한이 없으면 요청 */
    fun ensureLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** suspend: 현재 위치 1회 획득 */
    suspend fun getCurrentLocation(): Pair<Double?, Double?> =
        suspendCancellableCoroutine { cont ->

            val fineGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!fineGranted && !coarseGranted) {
                cont.resume(null to null)
                return@suspendCancellableCoroutine
            }

            val tokenSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).addOnSuccessListener { loc ->
                if (!cont.isCompleted) {
                    if (loc != null) cont.resume(loc.latitude to loc.longitude)
                    else cont.resume(null to null)
                }
            }.addOnFailureListener {
                if (!cont.isCompleted) cont.resume(null to null)
            }

            cont.invokeOnCancellation {
                tokenSource.cancel()
            }
        }
}
