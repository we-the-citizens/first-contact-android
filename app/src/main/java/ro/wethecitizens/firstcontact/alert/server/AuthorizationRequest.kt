package ro.wethecitizens.firstcontact.alert.server

import com.google.gson.annotations.SerializedName

data class AuthorizationRequest(
    @SerializedName("pacientId") val pacientId: String,
    @SerializedName("authorizationData") val authorizationData: String
)