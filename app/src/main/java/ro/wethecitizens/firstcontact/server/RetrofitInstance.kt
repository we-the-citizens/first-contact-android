package ro.wethecitizens.firstcontact.server

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.wethecitizens.firstcontact.BuildConfig

internal class RetrofitInstance private constructor() {

    companion object {
        private lateinit var client: Retrofit

        // FIXME: change with actual server endpoint
        // private fun getServerUrl(): String = "https://jsonplaceholder.typicode.com/"
        private fun getServerUrl(): String =
            "https://first-contact-dev.us-east-2.elasticbeanstalk.com/"

        internal fun getInstance(): Retrofit = if (::client.isInitialized.not()) {
            synchronized(this) {
                if (::client.isInitialized.not()) {
                    Retrofit.Builder()
                        .baseUrl(getServerUrl())
                        .addConverterFactory(GsonConverterFactory.create())

                        .also { builder ->
                            // debug logging -- filter by "OkHttp"
                            if (BuildConfig.DEBUG) {
                                val interceptor = HttpLoggingInterceptor()
                                interceptor.level = HttpLoggingInterceptor.Level.BODY
                                val client =
                                    OkHttpClient.Builder().addInterceptor(interceptor).build()

                                builder.client(client)
                            }
                        }
                        .build()
                        .apply { client = this }
                } else client
            }
        } else client
    }
}