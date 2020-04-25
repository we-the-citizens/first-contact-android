package ro.wethecitizens.firstcontact.server

import androidx.annotation.WorkerThread
import retrofit2.Call
import retrofit2.http.GET

/**
 * Use [getInstance] to target [RetrofitInstance.getServerUrl] endpoint.
 */
interface BackendMethods {

    // FIXME: Delete after using actual method
    @GET("/photos")
    @WorkerThread
    fun getAllPhotos(): Call<List<DummyPhotoModel>>

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