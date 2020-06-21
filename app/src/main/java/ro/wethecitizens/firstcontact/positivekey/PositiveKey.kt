// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.positivekey

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PositiveKey(
    val msg: String
) : Parcelable
