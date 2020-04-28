package ro.wethecitizens.firstcontact.services

import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import java.util.*

class MatchingKeysAlgorithm(

    intersectedContacts: List<StreetPassRecord>,
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

    private fun buildDaysList() {

        var dayOfYear = -1
        var currentDayIdx = 0

        for (spr in records) {

            val c = Calendar.getInstance()
            c.timeInMillis = spr.timestamp

            val doy = c.get(Calendar.DAY_OF_YEAR)

            if (dayOfYear == doy) {

                daysList[currentDayIdx].records.add(spr)
            }
            else {

                daysList.add(Day(
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                ))

                currentDayIdx = daysList.lastIndex
                dayOfYear = doy
            }
        }
    }

    private fun calculateAllDaysExposureTime() {

        for (day in daysList)
            calculateOneDayExposureTime(day)
    }

    private fun calculateOneDayExposureTime(day: Day) {

        var lastTempID = ""
        var lastTS: Long = 0
        var isTempIDIgnored = false

        for (spr in day.records) {

            if (isTempIDIgnored) {

                val msDiff = spr.timestamp - lastTS
                val minDiff = (msDiff / (60 * 1000)).toInt()

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
    }

    private fun composeResult() {

        for (day in daysList) {

            if (day.exposureInMinutes > 0)
                resultDaysList.add(DayResult(day.year, day.month, day.dayOfMonth, day.exposureInMinutes))
        }
    }


    /* Private members */

    private val records: List<StreetPassRecord> = intersectedContacts
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
        var year: Int, var month: Int, var dayOfMonth: Int, var exposureInMinutes: Int
    ){

    }
}