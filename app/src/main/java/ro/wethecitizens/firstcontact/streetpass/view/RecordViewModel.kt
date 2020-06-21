// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.streetpass.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordDatabase
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordRepository

class RecordViewModel(app: Application) : AndroidViewModel(app) {

    private var repo: StreetPassRecordRepository

    var allRecords: LiveData<List<StreetPassRecord>>

    init {
        val recordDao = StreetPassRecordDatabase.getDatabase(app).recordDao()
        repo = StreetPassRecordRepository(recordDao)
        allRecords = repo.allRecords
    }


}
