package ro.wethecitizens.firstcontact.positivekey.persistence

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface PositiveKeyRecordDao {

    @Query("SELECT * from positive_key_table ORDER BY id ASC")
    suspend fun getCurrentRecords(): List<PositiveKeyRecord>

//    @Query("SELECT * from positive_key_table where 'key' = :key ORDER BY id DESC LIMIT 1")
//    suspend fun getMostRecentRecord(key: String): LiveData<PositiveKeyRecord?>

    @Query("SELECT id from positive_key_table ORDER BY id DESC")
    suspend fun getLastId(): Long

    @Query("DELETE FROM positive_key_table")
    suspend fun nukeDb()

    @Query("DELETE FROM positive_key_table WHERE key_date < :before")
    suspend fun purgeOldRecords(before: Long)

    @RawQuery
    suspend fun getRecordsViaQuery(query: SupportSQLiteQuery): List<PositiveKeyRecord>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: PositiveKeyRecord)

}
