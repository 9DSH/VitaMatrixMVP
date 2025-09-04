
import com.swings.vitamatrix.api.OpenAiApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.openai.com/"

//    private const val API_KEY = "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"
    private const val API_KEY = "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"

    val api: OpenAiApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // نمایش لاگ کامل
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // زمان اتصال
            .readTimeout(30, TimeUnit.SECONDS) // زمان خواندن پاسخ
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApi::class.java)
    }
}