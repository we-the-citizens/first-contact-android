package ro.wethecitizens.firstcontact.positivekey.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "positive_key_table",
    indices = [
        Index("key"),
        Index("key_date")
    ]
)
data class PositiveKeyRecord (

    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "key_date")
    val keyDate: Calendar = Calendar.getInstance()
)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}
