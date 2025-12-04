
import com.example.what2do_today.network.CourseResponse
import com.example.what2do_today.network.DirectionsResponse
import com.example.what2do_today.network.RecommendResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    //로그인



    // 1) 자연어 → 카테고리 리스트 (GET, sentences 쿼리)
    @GET("api/v1/first")
    suspend fun getRecommend(
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("sentences") sentences: String
    ): RecommendResponse
    // 서버 응답 JSON: "location":"중앙대학교","tags":["amusement_park","zoo","park"]}


    // 2) 카테고리 리스트 → 코스(플랜) 목록 (GET, categories 리스트 쿼리)
    @GET("api/v1/course")
    suspend fun getPlans(
        @Query("categories") categories: List<String>
    ): CourseResponse
    // 서버 응답 JSON: { "plans": [ ... ] }

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


