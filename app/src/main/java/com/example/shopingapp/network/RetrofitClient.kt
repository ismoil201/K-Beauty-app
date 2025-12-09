import com.example.shopingapp.network.ApiService
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://cosmetic-server-production.up.railway.app/"

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val credentials = Credentials.basic("ismoil", "1111")
            val request = chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build()
            chain.proceed(request)
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
