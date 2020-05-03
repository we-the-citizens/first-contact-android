package ro.wethecitizens.firstcontact.infectionalert.persistence

import android.content.Context
import androidx.lifecycle.LiveData
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordDatabase

class InfectionAlertRecordStorage(val context: Context) {

    private val dao = StreetPassRecordDatabase.getDatabase(context).infectionAlertDao()

    suspend fun getAllRecords(): List<InfectionAlertRecord> {

        return dao.getCurrentRecords()
    }

    suspend fun updateExposureTime(id: Int, exposureInMinutes: Int) {

        return dao.updateExposureTime(id, exposureInMinutes)
    }

    suspend fun nukeDb() {

        dao.nukeDb()
    }

    suspend fun saveRecord(record: InfectionAlertRecord) {

        dao.insert(record)
    }

    fun getall(): LiveData<List<InfectionAlertRecord>> {

        return dao.allRecords()
    }
}
