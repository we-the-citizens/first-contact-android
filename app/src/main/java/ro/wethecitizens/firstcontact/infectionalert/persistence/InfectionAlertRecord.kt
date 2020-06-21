// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.infectionalert.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "infection_alert"
)
data class InfectionAlertRecord (

    @ColumnInfo(name = "exposure_date")
    val exposureDate: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "exposure_in_minutes")
    val exposureInMinutes: Int = 0
)
{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "create_date")
    var createDate: Calendar = Calendar.getInstance()
}
