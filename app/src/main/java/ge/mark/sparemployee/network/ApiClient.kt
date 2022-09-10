package ge.mark.sparemployee.network

import com.google.gson.GsonBuilder
import ge.mark.sparemployee.network.services.ApiServices
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    private val retrofit: Retrofit
        get() {
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build()
                chain.proceed(request)
            })
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClientLogging: OkHttpClient =
                OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("https://td.self.ge/")
                .client(httpClient.build())
                .build()
        }

    fun getService(): ApiServices? {
        return retrofit.create(ApiServices::class.java)
    }

}
