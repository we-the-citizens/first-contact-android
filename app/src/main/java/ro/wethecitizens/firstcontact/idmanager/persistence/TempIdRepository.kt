// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.idmanager.persistence

import androidx.lifecycle.LiveData
import ro.wethecitizens.firstcontact.idmanager.persistence.TempId
import ro.wethecitizens.firstcontact.idmanager.persistence.TempIdDao

class TempIdRepository(private val recordDao: TempIdDao) {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRecords: LiveData<List<TempId>> = recordDao.getRecords()

    suspend fun insert(word: TempId) {
        recordDao.insert(word)
    }
}