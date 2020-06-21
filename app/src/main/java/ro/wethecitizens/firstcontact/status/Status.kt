// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.status

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Status(
    val msg: String
) : Parcelable
