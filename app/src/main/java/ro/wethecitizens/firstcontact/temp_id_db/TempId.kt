// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.temp_id_db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
class TempId constructor(
    @ColumnInfo(name = "v")
    var v: String

) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

}
