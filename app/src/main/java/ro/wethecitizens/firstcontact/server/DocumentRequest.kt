package ro.wethecitizens.firstcontact.server

import com.google.gson.annotations.SerializedName
import ro.wethecitizens.firstcontact.server.PositiveIdsRequest
import java.util.*

data class DocumentRequest(
    @SerializedName("data") val data: List<PositiveIdsRequest.PositiveId>,
    @SerializedName("signature") val signature: String
) {
    data class PositiveId(
        @SerializedName("tempId") val tempId: String,
        @SerializedName("date") val date: Date // string date time value in ISO8601 format
    )
}