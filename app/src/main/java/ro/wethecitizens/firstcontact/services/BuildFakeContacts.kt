// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.services

import android.content.Context
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage

class BuildFakeContacts {


    suspend fun run(c: Context) {

        val or = "WE_THE_CITIZENS"
        val mp = "Nexus 5X"
        val mc = "Nexus 7"

        val store = StreetPassRecordStorage(c)


        //store.purgeOldRecords(System.currentTimeMillis())
        store.deleteAllRecords()


        val r1 = StreetPassRecord(
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            rssi = -72,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r1.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:36:40+02:00")


        val r2 = StreetPassRecord(
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            rssi = -78,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r2.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:42:40+02:00")


        val r3 = StreetPassRecord(
            msg = "0d8fb6646f1898c68523f7c79f3a20cec979b6f8abd665f793600cb352fbbbf1",
            rssi = -82,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r3.timestamp = Utils.parseISO8601StringToMillis("2020-04-17T17:47:40+02:00")


        val r4 = StreetPassRecord(
            msg = "0f5d312558497e3bf0fa888578eb35c32164dcfb62a440610d3df88ea8a81004",
            rssi = -74,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r4.timestamp = Utils.parseISO8601StringToMillis("2020-04-18T19:39:40+02:00")


        val r5 = StreetPassRecord(
            msg = "0f5d312558497e3bf0fa888578eb35c32164dcfb62a440610d3df88ea8a81004",
            rssi = -76,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r5.timestamp = Utils.parseISO8601StringToMillis("2020-04-18T19:45:40+02:00")


        val r6 = StreetPassRecord(
            msg = "3b799994e8261627b66b6b18b3c4ad3785e0865d7e0f06ba1d594db9ff981eaa",
            rssi = -72,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r6.timestamp = Utils.parseISO8601StringToMillis("2020-04-19T17:39:40+02:00")


        val r7 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -84,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r7.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:36:40+02:00")


        val r8 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -77,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r8.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:42:40+02:00")


        val r9 = StreetPassRecord(
            msg = "f2bc043970fa36dd1c44e17e0bbf759f1134a1a13011f6bb557a224871b9ea6a",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r9.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:56:40+02:00")


        val r10 = StreetPassRecord(
            msg = "f2bc043970fa36dd1c44e17e0bbf759f1134a1a13011f6bb557a224871b9ea6a",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r10.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T17:59:40+02:00")


        val r11 = StreetPassRecord(
            msg = "f2bc043970fa36dd1c44e17e0bbf759f1134a1a13011f6bb557a224871b9ea6a",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r11.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T18:09:40+02:00")


        val r12 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r12.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T18:12:40+02:00")


        val r13 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r13.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T18:16:44+02:00")


        val r14 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r14.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T18:20:44+02:00")


        val r15 = StreetPassRecord(
            msg = "0c778b2f26ae82f59a64361698e94d1ae4ceade395100d39c4dddae0d6cd5cd0",
            rssi = -50,
            org = or, modelP = mp, modelC = mc, txPower = 1, v = 2
        )
        r15.timestamp = Utils.parseISO8601StringToMillis("2020-04-25T18:34:44+02:00")



        store.saveRecord(r1)
        store.saveRecord(r2)
        store.saveRecord(r3)
        store.saveRecord(r4)
        store.saveRecord(r5)
        store.saveRecord(r6)
        store.saveRecord(r7)
        store.saveRecord(r8)
        store.saveRecord(r9)
        store.saveRecord(r10)
        store.saveRecord(r11)
        store.saveRecord(r12)
        store.saveRecord(r13)
        store.saveRecord(r14)
        store.saveRecord(r15)
    }
}