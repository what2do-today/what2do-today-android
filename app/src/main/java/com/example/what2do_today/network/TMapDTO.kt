package com.example.what2do_today.network

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.kakao.vectormap.LatLng

data class TMapRouteRequest(
    val startX: Double, val startY: Double,
    val endX: Double, val endY: Double,
    val startName: String = "출발", val endName: String = "도착",
    val searchOption: Int = 0,
    val passList: String? = null
)

data class TMapRouteResponse(
    val features: List<Feature>
)

data class Feature(val geometry: Geometry)

data class Geometry(
    val type: String,
    val coordinates: JsonElement
) {
    /**
     * 경로 그리기용 좌표 리스트 반환
     * Point(안내지점)는 제외하고, LineString(실제 경로 선)만 반환하도록 수정함
     */
    fun getAllPoints(): List<LatLng> {
        val list = mutableListOf<LatLng>()

        if (!coordinates.isJsonArray) return list
        val array = coordinates.asJsonArray

        when (type) {
            "LineString" -> {
                // 구조: [[lon, lat], [lon, lat], ...]
                for (elem in array) {
                    if (elem.isJsonArray) {
                        val p = elem.asJsonArray
                        if (p.size() >= 2) {
                            val lon = p[0].asDouble // TMap은 경도가 0번
                            val lat = p[1].asDouble // TMap은 위도가 1번

                            // 카카오는 (위도, 경도) 순서
                            list.add(LatLng.from(lat, lon))
                        }
                    }
                }
            }

            // TMap 보행자 경로에서는 잘 안 나오지만 방어 코드 유지
            "MultiLineString" -> {
                for (line in array) {
                    if (line.isJsonArray) {
                        for (elem in line.asJsonArray) {
                            if (elem.isJsonArray) {
                                val p = elem.asJsonArray
                                if (p.size() >= 2) {
                                    val lon = p[0].asDouble
                                    val lat = p[1].asDouble
                                    list.add(LatLng.from(lat, lon))
                                }
                            }
                        }
                    }
                }
            }

            // "Point" 타입은 경로 선을 그릴 때 중복되거나 필요 없으므로 제외 (빈 리스트 반환)
            else -> {
                // 필요하다면 로그만 남김
                // Log.d("TMAP", "Skipping geometry type: $type")
            }
        }

        return list
    }
    fun getAllPointsAsMapPoint(): List<MapPoint> {
        val list = mutableListOf<MapPoint>()

        if (!coordinates.isJsonArray) return list
        val array = coordinates.asJsonArray

        when (type) {
            "Point" -> {
                if (array.size() >= 2) {
                    val lon = array[0].asDouble
                    val lat = array[1].asDouble
                    list.add(MapPoint(lat, lon))
                }
            }
            "LineString" -> {
                for (elem in array) {
                    if (elem.isJsonArray) {
                        val p = elem.asJsonArray
                        if (p.size() >= 2) {
                            val lon = p[0].asDouble
                            val lat = p[1].asDouble
                            list.add(MapPoint(lat, lon))
                        }
                    }
                }
            }
            "MultiLineString" -> {
                for (line in array) {
                    if (line.isJsonArray) {
                        for (elem in line.asJsonArray) {
                            if (elem.isJsonArray) {
                                val p = elem.asJsonArray
                                if (p.size() >= 2) {
                                    val lon = p[0].asDouble
                                    val lat = p[1].asDouble
                                    list.add(MapPoint(lat, lon))
                                }
                            }
                        }
                    }
                }
            }
        }
        return list
    }
}

data class MapPoint(
    val lat: Double,
    val lng: Double
)