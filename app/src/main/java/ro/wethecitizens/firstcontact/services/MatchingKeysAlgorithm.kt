package ro.wethecitizens.firstcontact.services

import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import java.util.*

class MatchingKeysAlgorithm(
        intersectedContacts: List<StreetPassRecord>,
        minimumTimeExposureInMinutes: Int
    ) {

    fun run() {

        buildDaysList();
        calculateAllDaysTimeExposure();
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

                daysList.add(Day())

                currentDayIdx = daysList.lastIndex
                dayOfYear = doy
            }
        }
    }

    private fun calculateAllDaysTimeExposure() {

        for (day in daysList)
            calculateOneDayTimeExposure(day)
    }

    private fun calculateOneDayTimeExposure(day: Day) {

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

            lastTS = spr.timestamp;
        }
    }



    /* Private members */

    private val records: List<StreetPassRecord> = intersectedContacts
    private val deltaMinutes: Int = minimumTimeExposureInMinutes
    private val daysList: MutableList<Day> = mutableListOf()



    /* Inner classes */

    class Day {

        var exposureInMinutes: Int = 0
        var records: MutableList<StreetPassRecord> = mutableListOf()
    }
}