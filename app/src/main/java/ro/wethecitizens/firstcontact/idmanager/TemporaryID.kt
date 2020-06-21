// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.idmanager

import ro.wethecitizens.firstcontact.logging.CentralLog

class TemporaryID(
    val startTime: Long,
    val tempID: String,
    val expiryTime: Long
) {

    fun isValidForCurrentTime(): Boolean {

        val currentTime = System.currentTimeMillis()
        return ((currentTime > startTime) && (currentTime < expiryTime))
    }

    fun print() {

        val tempIDStartTime = startTime
        val tempIDExpiryTime = expiryTime

        CentralLog.d(
            TAG,
            "[TempID] Start time: ${tempIDStartTime}"
        )
        CentralLog.d(
            TAG,
            "[TempID] Expiry time: ${tempIDExpiryTime}"
        )
    }

    companion object {
        private const val TAG = "TempID"
    }
}
