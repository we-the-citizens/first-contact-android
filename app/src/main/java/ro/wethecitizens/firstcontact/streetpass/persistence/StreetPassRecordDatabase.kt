package ro.wethecitizens.firstcontact.streetpass.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ro.wethecitizens.firstcontact.positivekey.persistence.PositiveKeyRecord
import ro.wethecitizens.firstcontact.positivekey.persistence.PositiveKeyRecordDao
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.status.persistence.StatusRecordDao


@Database(
    entities = arrayOf(StreetPassRecord::class, StatusRecord::class, PositiveKeyRecord::class),
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StreetPassRecordDatabase : RoomDatabase() {

    abstract fun recordDao(): StreetPassRecordDao
    abstract fun statusDao(): StatusRecordDao
    abstract fun positiveKeyDao(): PositiveKeyRecordDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: StreetPassRecordDatabase? = null

        fun getDatabase(context: Context): StreetPassRecordDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    StreetPassRecordDatabase::class.java,
                    "record_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
