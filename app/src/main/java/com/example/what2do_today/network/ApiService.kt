import com.example.what2do_today.network.CategoryListResponse
import com.example.what2do_today.network.Plans
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // 1) 자연어 → 카테고리 리스트 (GET, sentences 쿼리)
    @GET("api/v1/recommend")
    suspend fun getCategories(
        @Query("sentences") sentences:  String
    ): CategoryListResponse
    // 서버 응답 JSON: "location":"중앙대학교","tags":["amusement_park","zoo","park"]}

    // 2) 카테고리 리스트 → 코스(플랜) 목록 (GET, categories 리스트 쿼리)
    @GET("api/v1/plans")
    suspend fun getPlans(
        @Query("categories") categories: List<String>
    ): Plans
    // 서버 응답 JSON: { "plans": [ ... ] }
}
