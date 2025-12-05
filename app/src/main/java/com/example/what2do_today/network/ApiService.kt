

import com.example.what2do_today.network.DirectionsResponse
import com.example.what2do_today.network.FirstResponse
import com.example.what2do_today.network.SecondResponse
import retrofit2.http.Body
import retrofit2.http.GET
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



