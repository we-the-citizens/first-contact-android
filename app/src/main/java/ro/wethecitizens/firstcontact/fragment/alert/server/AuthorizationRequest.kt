// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment.alert.server

import com.google.gson.annotations.SerializedName

data class AuthorizationRequest(
    @SerializedName("pacientId") val pacientId: String,
    @SerializedName("authorizationData") val authorizationData: String
)