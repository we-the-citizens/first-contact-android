package ro.wethecitizens.firstcontact.services

import android.content.Context
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage

class BuildFakeContacts {


    suspend fun run(c: Context) {

        val store = StreetPassRecordStorage(c)


        store.purgeOldRecords(System.currentTimeMillis())

        val r1 = StreetPassRecord(
            v = 2,
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -72,
            txPower = 1
        )
        r1.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:36:40.614+02:00")


        val r2 = StreetPassRecord(
            v = 2,
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -78,
            txPower = 1
        )
        r2.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:37:40.614+02:00")


        val r3 = StreetPassRecord(
            v = 2,
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -82,
            txPower = 1
        )
        r3.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:38:40.614+02:00")


        val r4 = StreetPassRecord(
            v = 2,
            msg = "0f5d312558497e3bf0fa888578eb35c32164dcfb62a440610d3df88ea8a81004",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -74,
            txPower = 1
        )
        r4.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:39:40.614+02:00")


        val r5 = StreetPassRecord(
            v = 2,
            msg = "0f5d312558497e3bf0fa888578eb35c32164dcfb62a440610d3df88ea8a81004",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -76,
            txPower = 1
        )
        r5.timestamp = Utils.parseISO8601StringToMillis("2020-04-19T17:36:40.614+02:00")


        val r6 = StreetPassRecord(
            v = 2,
            msg = "3b799994e8261627b66b6b18b3c4ad3785e0865d7e0f06ba1d594db9ff981eaa",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -72,
            txPower = 1
        )
        r6.timestamp = Utils.parseISO8601StringToMillis("2020-04-19T17:39:40.614+02:00")


        val r7 = StreetPassRecord(
            v = 2,
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -84,
            txPower = 1
        )
        r7.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:36:40.614+02:00")


        val r8 = StreetPassRecord(
            v = 2,
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -77,
            txPower = 1
        )
        r8.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:42:40.614+02:00")


        val r9 = StreetPassRecord(
            v = 2,
            msg = "f2bc043970fa36dd1c44e17e0bbf759f1134a1a13011f6bb557a224871b9ea6a",
            org = "WE_THE_CITIZENS",
            modelP = "Nexus 5X",
            modelC = "Nexus 7",
            rssi = -50,
            txPower = 1
        )
        r9.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:46:40.614+02:00")


        store.saveRecord(r1)
        store.saveRecord(r2)
        store.saveRecord(r3)
        store.saveRecord(r4)
        store.saveRecord(r5)
        store.saveRecord(r6)
        store.saveRecord(r7)
        store.saveRecord(r8)
        store.saveRecord(r9)
    }
}