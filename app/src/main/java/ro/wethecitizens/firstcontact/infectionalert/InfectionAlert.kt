package ro.wethecitizens.firstcontact.infectionalert

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InfectionAlert(
    val msg: String
) : Parcelable
