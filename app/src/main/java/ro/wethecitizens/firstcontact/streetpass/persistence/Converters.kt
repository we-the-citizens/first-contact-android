// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.streetpass.persistence


import androidx.room.TypeConverter
import java.util.Calendar

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {
    @TypeConverter fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }
}