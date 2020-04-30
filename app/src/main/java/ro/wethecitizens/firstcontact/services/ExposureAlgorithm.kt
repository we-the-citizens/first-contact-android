package ro.wethecitizens.firstcontact.services

import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import java.util.*

class ExposureAlgorithm(

    contacts: List<StreetPassRecord>,
    minimumExposureTimeInMinutes: Int

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


        var lastTempID = ""
        var lastTS: Long = 0
        var isTempIDIgnored = false

        for (spr in day.records) {

            val c = Calendar.getInstance()
            c.timeInMillis = spr.timestamp

            d("${spr.id} ${spr.msg} ${Utils.formatCalendarToISO8601String(c)}")

            if (isTempIDIgnored) {

                val msDiff = spr.timestamp - lastTS
                val minDiff = (msDiff / (60 * 1000)).toInt()

                d("1 minDiff = $minDiff")

                if (minDiff < deltaMinutes) {

                    day.exposureInMinutes += minDiff
                }
                else {

                    isTempIDIgnored = false
                }
            }
            else {

                if (lastTempID == spr.msg) {

                    val msDiff = spr.timestamp - lastTS
                    val minDiff = (msDiff / (60 * 1000)).toInt()

                    d("2 minDiff = $minDiff")

                    if (minDiff < deltaMinutes) {

                        day.exposureInMinutes += minDiff

                        isTempIDIgnored = true
                    }
                }
                else {

                    lastTempID = spr.msg
                }
            }

            lastTS = spr.timestamp
        }

        d("day.exposureInMinutes = ${day.exposureInMinutes}")
        d("")
        d("")
    }

    private fun composeResult() {

        for (day in daysList) {

            if (day.exposureInMinutes == 0)
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

    private val isTraceActive = false

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