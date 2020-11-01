// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.idmanager.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = arrayOf(TempId::class),
    version = 1,
    exportSchema = true
)
abstract class TempIdDatabase : RoomDatabase() {

    abstract fun recordDao(): TempIdDao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TempIdDatabase? = null

        fun getDatabase(context: Context): TempIdDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    TempIdDatabase::class.java,
                    "tempId_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}