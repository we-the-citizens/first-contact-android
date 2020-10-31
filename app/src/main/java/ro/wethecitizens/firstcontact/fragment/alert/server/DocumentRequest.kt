package ro.wethecitizens.firstcontact.fragment.alert.server

import com.google.gson.annotations.SerializedName
import java.util.*

data class DocumentRequest(
    @SerializedName("data") val data: List<PositiveIdsRequest.PositiveId>
) {
    data class PositiveId(
        @SerializedName("tempId") val tempId: String,
        @SerializedName("date") val date: Date // string date time value in ISO8601 format
    )
}