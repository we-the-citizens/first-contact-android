// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.idmanager.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import ro.wethecitizens.firstcontact.idmanager.persistence.TempId

@Dao
interface TempIdDao {

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    fun getRecords(): LiveData<List<TempId>>

    @Query("SELECT * from record_table ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentRecord(): LiveData<TempId?>

    @Query("SELECT * from record_table ORDER BY timestamp ASC")
    suspend fun getCurrentRecords(): List<TempId>

    @Query("SELECT * from record_table ORDER BY timestamp DESC LIMIT 10")
    suspend fun getLast10Records(): List<TempId>

    @Query("DELETE FROM record_table")
    suspend fun nukeDb()

    @Query("DELETE FROM record_table WHERE timestamp < :before")
    suspend fun purgeOldRecords(before: Long)

    @Query("SELECT * FROM record_table WHERE v = :id")
    suspend fun checkIfPresent(id: String): List<TempId>

    @RawQuery
    fun getRecordsViaQuery(query: SupportSQLiteQuery): List<TempId>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: TempId)

}