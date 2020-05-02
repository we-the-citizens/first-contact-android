package ro.wethecitizens.firstcontact.temp_id_db

import android.content.Context

class TempIdStorage(val context: Context) {

    val recordDao = TempIdDatabase.getDatabase(context).recordDao()

     suspend fun saveRecord(record: TempId) {
        recordDao.insert(record)
    }

    suspend fun nukeDb() {
        recordDao.nukeDb()
    }

    suspend fun getAllRecords(): List<TempId> {
        return recordDao.getCurrentRecords()
    }

    suspend fun getLast10Records(): List<TempId> {
        return recordDao.getLast10Records()
    }


    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}