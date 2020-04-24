package ro.wethecitizens.firstcontact.positivekey.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface PositiveKeyRecordDao {

    @Query("SELECT * from positive_key_table ORDER BY id ASC")
    fun getRecords(): LiveData<List<PositiveKeyRecord>>

    @Query("SELECT * from positive_key_table ORDER BY id ASC")
    fun getCurrentRecords(): List<PositiveKeyRecord>

    @Query("SELECT * from positive_key_table where 'key' = :key ORDER BY id DESC LIMIT 1")
    fun getMostRecentRecord(key: String): LiveData<PositiveKeyRecord?>


    @Query("DELETE FROM positive_key_table")
    fun nukeDb()

    @Query("DELETE FROM positive_key_table WHERE key_date < :before")
    suspend fun purgeOldRecords(before: Long)

    @RawQuery
    fun getRecordsViaQuery(query: SupportSQLiteQuery): List<PositiveKeyRecord>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: PositiveKeyRecord)

}
