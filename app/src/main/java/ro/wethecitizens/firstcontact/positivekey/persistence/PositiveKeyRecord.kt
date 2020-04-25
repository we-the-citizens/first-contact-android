package ro.wethecitizens.firstcontact.positivekey.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "positive_key_table"
)
data class PositiveKeyRecord (

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "key_date")
    val keyDate: Calendar = Calendar.getInstance()
)
