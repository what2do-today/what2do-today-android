

import com.example.what2do_today.network.DirectionsResponse
import com.example.what2do_today.network.FirstResponse
import com.example.what2do_today.network.SecondResponse
import com.example.what2do_today.network.TMapRouteRequest
import com.example.what2do_today.network.TMapRouteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    //로그인


    // 1) 자연어 → 카테고리 리스트 (GET, sentences 쿼리)
    @GET("api/v1/first")
    suspend fun getFirst(
        @Query("sentences") sentences: String
    ): FirstResponse
    // ⚠ RecommendResponse 안에 sessionId, activityTags, location,
    //    searchLatitude, searchLongitude 등이 있어야 함


    // 2) 위치 없는 경우에만 호출하는 2단계 엔드포인트
    //    GET /api/v1/second?sessionId=...&latitude=...&longitude=...
    @GET("api/v1/second")
    suspend fun getSecond(
        @Query("sessionid") sessionId: String,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("selectedtags") selectedTags: List<String>
    ): SecondResponse
}


interface DirectionsApi {

    @GET("maps/api/directions/json")
    suspend fun getWalkingRoute(
        @Query("origin") origin: String,          // "37.5665,126.9780"
        @Query("destination") destination: String, // "37.4979,127.0276"
        @Query("waypoints") waypoints: String?,  // "optimize:true|lat,lng|lat,lng..."
        @Query("mode") mode: String = "walking", // 도보
        @Query("key") apiKey: String
    ): DirectionsResponse

}
//TMAP
interface TMapApiService {

    @Headers("Content-Type: application/json") // JSON으로 보낸다는 것을 명시
    @POST("tmap/routes/pedestrian")
    suspend fun getWalkingRoute(
        @Header("appKey") appKey: String,          // API Key 헤더

        @Query("version") version: Int = 1,        // 필수 파라미터 (보통 Query로 보냄)

        // ⚠️ 중요: 좌표계를 WGS84로 고정 (Body에 안 넣고 Query로 보내도 TMap이 인식함)
        @Query("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Query("resCoordType") resCoordType: String = "WGS84GEO",

        @Body request: TMapRouteRequest            // 좌표 정보가 담긴 DTO (JSON Body)
    ): Response<TMapRouteResponse>
}
