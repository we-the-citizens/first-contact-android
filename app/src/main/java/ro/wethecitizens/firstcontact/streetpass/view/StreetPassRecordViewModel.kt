// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.streetpass.view

import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord

class StreetPassRecordViewModel(record: StreetPassRecord, phoneAliases : MutableMap<String, String>, val number: Int = 1) {
    val version = record.v
    val modelC = phoneAliases.get(record.modelC)
    val modelP = phoneAliases.get(record.modelP)
    val msg = record.msg
    val timeStamp = record.timestamp
    val rssi = record.rssi
    val transmissionPower = record.txPower
    val org = record.org
}
