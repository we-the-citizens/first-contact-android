// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.services

import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import java.util.*
import kotlin.collections.ArrayList

class ExposureAlgorithm(

    contacts: List<StreetPassRecord>,
    minimumExposureTimeInMinutes: Int,
    enableTrace: Boolean

)
{

    fun getExposureDays(): List<DayResult> {

        buildDaysList()
        calculateAllDaysExposureTime()
        composeResult()

        return resultDaysList.toList()
    }



    /* Private fun */

    fun d(s: String) {

        if (isTraceActive)
            CentralLog.d("ExposureAlgorithm", s)
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

        d("-----------------------")
        d("calculateAllDaysExposureTime")
        d("")


        for (day in daysList)
            calculateOneDayExposureTime(day)
    }

    private fun calculateOneDayExposureTime(day: Day) {

        d("-----------------------")
        d("calculateOneDayExposureTime")
        d("")

        val records: MutableList<StreetPassRecord> = ArrayList(day.records)    //clone day records, so we can remove elements

        var i = 0;
        while (i < records.size - 1) {

            var rec = records[i]
            val c = Calendar.getInstance()
            c.timeInMillis = rec.timestamp
            d("${rec.id} ${rec.msg} ${Utils.formatCalendarToISO8601String(c)}")

            var lastRec: StreetPassRecord = rec
            var j = i + 1;
            while (j < Math.min(i + 50, records.size)) {   //50 records later and we're sure to cover the 15 min lifespan of the current id
                if (rec.msg == records[j].msg) {
                    lastRec = records[j]    //remember the lat occurance of the tempID
                    records.removeAt(j)     //remove that record
                    j--;
                }
                j++;
            }

            if(rec != lastRec) {
                val diff: Int = ((lastRec.timestamp - rec.timestamp) / (60 * 1000)).toInt()
                if (diff >= deltaMinutes)
                    day.exposureInMinutes += diff;
            }

            i++;
        }

        d("day.exposureInMinutes = ${day.exposureInMinutes}")
        d("")
        d("")
    }

    private fun composeResult() {

        for (day in daysList) {

            if (day.exposureInMinutes < deltaMinutes * 2)   //remove
                continue

            val c = Calendar.getInstance()
            c.set(day.year, day.month, day.dayOfMonth, 17, 0, 0)

            resultDaysList.add(
                DayResult(
                    date = c,
                    exposureInMinutes = day.exposureInMinutes
                )
            )
        }
    }


    /* Private members */

    private val isTraceActive = enableTrace

    private val records: List<StreetPassRecord> = contacts
    private val deltaMinutes: Int = minimumExposureTimeInMinutes
    private val daysList: MutableList<Day> = mutableListOf()
    private val resultDaysList: MutableList<DayResult> = mutableListOf()


    /* Inner classes */

    class Day(
        var year: Int, var month: Int, var dayOfMonth: Int
    ){

        var exposureInMinutes: Int = 0
        var records: MutableList<StreetPassRecord> = mutableListOf()
    }


    class DayResult(
        var date: Calendar, var exposureInMinutes: Int
    ){

    }
}