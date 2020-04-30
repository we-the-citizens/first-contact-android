package ro.wethecitizens.firstcontact.server

import androidx.annotation.WorkerThread
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ro.wethecitizens.firstcontact.alert.server.AuthorizationRequest
import retrofit2.http.Query
import ro.wethecitizens.firstcontact.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.positivekey.server.PositiveKeyModel

/**
 * Use [getInstance] to target [RetrofitInstance.getServerUrl] endpoint.
 */
interface BackendMethods {

    @GET("/positiveIds")
    @WorkerThread
    suspend fun getPositiveKeys(
        @Query("clientInstallDate") installDate: String
    ): List<PositiveKeyModel>

    @GET("/positiveIds")
    @WorkerThread
    suspend fun getPositiveKeys(
        @Query("clientInstallDate") installDate: String,
        @Query("clientMaxId") lastId: Int
    ): List<PositiveKeyModel>

    @POST("/positiveIds/authorization")
    @WorkerThread
    suspend fun checkUploadAuthorization(@Body authorizationRequest: AuthorizationRequest): Response<Unit>

    @POST("/positiveIds")
    @WorkerThread
    suspend fun uploadPositiveIds(@Body positiveIdsRequest: PositiveIdsRequest): Response<Unit>

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