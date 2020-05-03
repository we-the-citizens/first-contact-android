package ro.wethecitizens.firstcontact.idmanager

import android.content.Context
import ro.wethecitizens.firstcontact.logging.CentralLog
import java.util.*

object TempIDManager {

    private const val TAG = "TempIDManager"

    private var isSaved: Boolean = false
    private var createMS: Long = 0
    private var tempID: String = "---"
    private var expireMS: Long = 0

    fun retrieveNewTemporaryID(context: Context): TemporaryID? {

        isSaved = false
        createMS = System.currentTimeMillis();
        tempID = UUID.randomUUID().toString()
        expireMS = createMS + (15 * 60 * 1000);

        return TemporaryID(
            createMS,
            tempID,
            expireMS
        )
    }

    fun retrieveCurrentTemporaryID(context: Context): TemporaryID {

        return TemporaryID(
            createMS,
            tempID,
            expireMS
        )
    }

    fun needToUpdate(context: Context): Boolean {

        val currentTime = System.currentTimeMillis()
        val update = currentTime >= expireMS

        if (update) {
            CentralLog.i(
                TAG,
                "Yes, need to update and fetch TemporaryIDs? $expireMS vs $currentTime: $update"
            )
        }

        return update
    }

    fun needToBeSaved(): Boolean {

        return !isSaved
    }

    fun markAsSaved() {

        isSaved = true
    }


    //Can Cleanup, this function always return true
    fun bmValid(context: Context): Boolean {

        return true
    }
}
