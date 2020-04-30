package ro.wethecitizens.firstcontact.infectionalert.persistence

import androidx.room.*

@Dao
interface InfectionAlertRecordDao {

    @Query("SELECT * from infection_alert ORDER BY id ASC")
    suspend fun getCurrentRecords(): List<InfectionAlertRecord>

    @Query("UPDATE infection_alert SET exposure_in_minutes = :exposureInMinutes WHERE id = :id")
    suspend fun updateExposureTime(id: Int, exposureInMinutes: Int)

    @Query("DELETE FROM infection_alert")
    suspend fun nukeDb()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: InfectionAlertRecord)
}
