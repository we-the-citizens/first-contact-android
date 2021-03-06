// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.idmanager.persistence

import android.content.Context
import ro.wethecitizens.firstcontact.idmanager.persistence.TempId
import ro.wethecitizens.firstcontact.idmanager.persistence.TempIdDatabase

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

    suspend fun checkIfPresent(id: String): List<TempId> {
        return recordDao.checkIfPresent(id)
    }

    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}