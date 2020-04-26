package ro.wethecitizens.firstcontact.server

import androidx.annotation.WorkerThread
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ro.wethecitizens.firstcontact.positivekey.server.PositiveKeyModel

/**
 * Use [getInstance] to target [RetrofitInstance.getServerUrl] endpoint.
 */
interface BackendMethods {

    // FIXME: Delete after using actual method
    @GET("/photos")
    @WorkerThread
    fun getAllPhotos(): Call<List<DummyPhotoModel>>


    @GET("/positiveIds")
    @WorkerThread
    suspend fun getPositiveKeys(
        @Query("clientInstallDate") clientInstallDate: String,
        @Query("clientMaxId") clientMaxId: Int,
        @Query("limit") limit: Int
    ): List<PositiveKeyModel>



    companion object {
        private lateinit var instance: BackendMethods

        internal fun getInstance(): BackendMethods = if (::instance.isInitialized.not()) {
            synchronized(this) {
                if (::instance.isInitialized.not()) {
                    RetrofitInstance.getInstance()
                        .create(BackendMethods::class.java)
                        .apply { instance = this }
                } else instance
            }
        } else instance
    }
}