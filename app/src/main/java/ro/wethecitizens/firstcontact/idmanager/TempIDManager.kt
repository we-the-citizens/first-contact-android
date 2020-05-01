package ro.wethecitizens.firstcontact.idmanager

import android.content.Context
import ro.wethecitizens.firstcontact.logging.CentralLog
import java.util.*

object TempIDManager {

    private const val TAG = "TempIDManager"

    private var createMS: Long = 0
    private var expireMS: Long = 0

    fun retrieveNewTemporaryID(context: Context): TemporaryID? {

        createMS = System.currentTimeMillis();
        expireMS = createMS + (5 * 60 * 1000);

        return TemporaryID(
            createMS,
            UUID.randomUUID().toString(),
            expireMS
        )
    }

    fun needToUpdate(context: Context): Boolean {

        val currentTime = System.currentTimeMillis()
        val update = currentTime >= expireMS

        CentralLog.i(
            TAG,
            "Need to update and fetch TemporaryIDs? $expireMS vs $currentTime: $update"
        )

        return update
    }

    //Can Cleanup, this function always return true
    fun bmValid(context: Context): Boolean {

        return true
    }
}
