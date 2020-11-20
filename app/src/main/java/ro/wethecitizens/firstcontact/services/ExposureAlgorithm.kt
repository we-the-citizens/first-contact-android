// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.services

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import ro.wethecitizens.firstcontact.utils.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import java.util.*
import kotlin.collections.ArrayList

class ExposureAlgorithm(contacts: List<StreetPassRecord>)
{

    fun getExposureDays(): List<DayResult> {

        buildDaysList()
        calculateAllDaysExposureTime()
        composeResult()

        return resultDaysList.toList()
    }

    private fun buildDaysList() {

        var dayOfYear = -1
        var currentDayIdx = 0

        for (spr in records) {

            val c = Calendar.getInstance()
            c.timeInMillis = spr.timestamp

            val doy = c.get(Calendar.DAY_OF_YEAR)

            if (dayOfYear != doy) {

                daysList.add(Day(
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                ))

                currentDayIdx = daysList.lastIndex
                dayOfYear = doy
            }

            daysList[currentDayIdx].records.add(spr)
        }
    }

    private fun calculateAllDaysExposureTime() {

        for (day in daysList)
            calculateOneDayExposureTime(day)
    }

    private fun calculateOneDayExposureTime(day: Day) {

        val records: MutableList<StreetPassRecord> = ArrayList(day.records)    //clone day records, so we can remove elements

        val recordsPerPhone = mutableMapOf<String, MutableList<StreetPassRecord>>()

        for (record in records)
        {
            val phone = getOtherPhone(record)
            if (!recordsPerPhone.containsKey(phone))
                recordsPerPhone.put(phone, mutableListOf<StreetPassRecord>())

            recordsPerPhone.get(phone)?.add(record)
        }

        for (phoneRecords in recordsPerPhone.values)
            for (i in 0..phoneRecords.size - 2)
            {
                val time = ((phoneRecords[i + 1].timestamp - phoneRecords[i].timestamp) / 1000).toInt()

                if (time / 60 < timeGapMaxValue)    //skip gaps longer then 20 mins
                    day.exposureInSeconds += time;
            }

        CentralLog.d(TAG,"day.exposureInSeconds = ${day.exposureInSeconds}")
    }

    private fun getOtherPhone(event: StreetPassRecord): String {

        if (event.modelC.indexOf("SELF") != -1)
            return event.modelP
        else
            return event.modelC
    }

    private fun composeResult() {

        for (day in daysList) {

            if (day.exposureInSeconds / 60 < minDailyExposureTimeForAlert)   //remove days with too little exposure time
                continue

            val c = Calendar.getInstance()
            c.set(day.year, day.month, day.dayOfMonth, 17, 0, 0)

            resultDaysList.add(
                DayResult(
                    date = c,
                    exposureInMinutes = (day.exposureInSeconds / 60).toInt()
                )
            )
        }
    }


    /* Private members */
    private val records: List<StreetPassRecord> = contacts
    private val timeGapMaxValue: Int = Firebase.remoteConfig.getLong("time_gap_max_value").toInt()
    private val minDailyExposureTimeForAlert: Int = Firebase.remoteConfig.getLong("min_daily_exposure_time_for_alert").toInt()
    private val daysList: MutableList<Day> = mutableListOf()
    private val resultDaysList: MutableList<DayResult> = mutableListOf()


    /* Inner classes */

    class Day(
        var year: Int, var month: Int, var dayOfMonth: Int
    ){

        var exposureInSeconds: Int = 0
        var records: MutableList<StreetPassRecord> = mutableListOf()
    }


    class DayResult(
        var date: Calendar, var exposureInMinutes: Int
    ){ }

    companion object {

        private val TAG = "BTMService"
    }
}