package ro.wethecitizens.firstcontact.positivekey.persistence

import android.content.Context
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordDatabase

class PositiveKeyRecordStorage(val context: Context) {

    val statusDao = StreetPassRecordDatabase.getDatabase(context).positiveKeyDao();

    suspend fun saveRecord(record: PositiveKeyRecord) {
        statusDao.insert(record)
    }

    fun nukeDb() {
        statusDao.nukeDb()
    }

    fun getAllRecords(): List<PositiveKeyRecord> {
        return statusDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        statusDao.purgeOldRecords(before)
    }
}
