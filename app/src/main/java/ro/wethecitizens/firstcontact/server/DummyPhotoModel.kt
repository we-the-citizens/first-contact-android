package ro.wethecitizens.firstcontact.server

import com.google.gson.annotations.SerializedName

// FIXME: Delete after using actual method
data class DummyPhotoModel(
    @SerializedName("albumId") val albumId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String
)
