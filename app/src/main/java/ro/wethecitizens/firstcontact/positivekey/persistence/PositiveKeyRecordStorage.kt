package ro.wethecitizens.firstcontact.positivekey.persistence

import android.content.Context
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordDatabase

class PositiveKeyRecordStorage(val context: Context) {

    private val dao = StreetPassRecordDatabase.getDatabase(context).positiveKeyDao()

    suspend fun saveRecord(record: PositiveKeyRecord) {

        dao.insert(record)
    }

    suspend fun nukeDb() {

        dao.nukeDb()
    }

    suspend fun getAllRecords(): List<PositiveKeyRecord> {

        return dao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {

        dao.purgeOldRecords(before)
    }

    suspend fun getLastId() : Int {

        val id:Int? = dao.getLastId()

        return id?:0
    }

    suspend fun getMatchedKeysRecords(rssi: Int): List<StreetPassRecord> {

        return dao.getMatchedKeysRecords(rssi)
    }
}
