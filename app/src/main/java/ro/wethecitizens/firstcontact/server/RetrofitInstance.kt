package ro.wethecitizens.firstcontact.server

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class RetrofitInstance private constructor() {

    companion object {
        private lateinit var client: Retrofit

        // FIXME: change with actual server endpoint
        private fun getServerUrl() : String = "https://jsonplaceholder.typicode.com/"

        fun getInstance(): Retrofit = if (::client.isInitialized.not()) {
            synchronized(this) {
                if (::client.isInitialized.not()) {
                    Retrofit.Builder()
                        .baseUrl(getServerUrl())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .apply { client = this }
                }
                else client
            }
        } else client
    }
}