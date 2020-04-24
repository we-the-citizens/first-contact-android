package ro.wethecitizens.firstcontact.temp_id_db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord

@Dao
interface TempIdDao {

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    fun getRecords(): LiveData<List<TempId>>

    @Query("SELECT * from record_table ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentRecord(): LiveData<TempId?>

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    fun getCurrentRecords(): List<TempId>

    @Query("DELETE FROM record_table")
    fun nukeDb()

    @Query("DELETE FROM record_table WHERE timestamp < :before")
    suspend fun purgeOldRecords(before: Long)

    @RawQuery
    fun getRecordsViaQuery(query: SupportSQLiteQuery): List<TempId>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(record: TempId)

}