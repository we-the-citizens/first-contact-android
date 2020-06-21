// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

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

    suspend fun getLast10Records(): List<StreetPassRecord> {
        return recordDao.getLast10Records()
    }


    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}
