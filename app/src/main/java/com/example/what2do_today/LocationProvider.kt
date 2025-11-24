package com.example.what2do_today.location

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val activity: Activity) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(activity)

    /**
     * 코루틴 기반 현재 위치 1회 조회.
     * - 권한 없으면 (null, null) 반환
     * - 실패/오류여도 (null, null) 반환
     */
    suspend fun getCurrentLocation(): Pair<Double?, Double?> =
        suspendCancellableCoroutine { cont ->

            val fineGranted = ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            // 권한 없으면 바로 null 리턴
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
                    if (loc != null) {
                        cont.resume(loc.latitude to loc.longitude)
                    } else {
                        cont.resume(null to null)
                    }
                }
            }.addOnFailureListener {
                if (!cont.isCompleted) {
                    cont.resume(null to null)
                }
            }

            // 코루틴이 취소되면 위치 요청도 취소
            cont.invokeOnCancellation {
                tokenSource.cancel()
            }
        }
}
