package ro.wethecitizens.firstcontact.streetpass.persistence

import android.content.Context

class StreetPassRecordStorage(val context: Context) {

    val recordDao = StreetPassRecordDatabase.getDatabase(context).recordDao()

    suspend fun saveRecord(record: StreetPassRecord) {
        recordDao.insert(record)
    }

    fun nukeDb() {
        recordDao.nukeDb()
    }

    suspend fun deleteAllRecords() {
        recordDao.deleteAllRecords();
    }

    fun getAllRecords(): List<StreetPassRecord> {
        return recordDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}
