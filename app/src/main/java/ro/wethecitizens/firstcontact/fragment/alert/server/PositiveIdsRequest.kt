// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment.alert.server

import com.google.gson.annotations.SerializedName

data class PositiveIdsRequest(
    @SerializedName("pacientId") val pacientId: String,
    @SerializedName("pin") val pin: String,
    @SerializedName("data") val data: List<PositiveId>
) {
    data class PositiveId(
        @SerializedName("tempId") val tempId: String,
        @SerializedName("date") val date: String // string date time value in ISO8601 format
    )
}