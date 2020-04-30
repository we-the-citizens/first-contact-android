package ro.wethecitizens.firstcontact.positivekey.server

import com.google.gson.annotations.SerializedName

data class PositiveKeyModel(
    @SerializedName("id") val id: Int,
    @SerializedName("tempId") val tempId: String
)
