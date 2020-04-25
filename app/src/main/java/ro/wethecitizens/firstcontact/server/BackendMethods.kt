package ro.wethecitizens.firstcontact.server

import retrofit2.Call
import retrofit2.http.GET

/**
 * Targets [RetrofitInstance.getServerUrl] endpoint.
 */
interface BackendMethods {

    // FIXME: Delete after using actual method
    @GET("/photos")
    fun getAllPhotos(): Call<List<DummyPhotoModel>>
}