package ro.wethecitizens.firstcontact.temp_id_db

import android.content.Context

class TempIdStorage(val context: Context) {

    val recordDao = TempIdDatabase.getDatabase(context).recordDao()

     fun saveRecord(record: TempId) {
        recordDao.insert(record)
    }

    fun nukeDb() {
        recordDao.nukeDb()
    }

    fun getAllRecords(): List<TempId> {
        return recordDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}