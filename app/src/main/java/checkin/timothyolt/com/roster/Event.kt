package checkin.timothyolt.com.roster

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.*

data class Event (
        //var id: String? = null,
        @Suppress("MemberVisibilityCanPrivate") // Used via reflection with Firebase, must be public
        var dateString: String? = null,
        var name: String? = null,
        var attendees: HashMap<String, String>? = null,
        var attendeeCount: Int? = null
) {
    constructor(name: String?) : this(Calendar.getInstance(), name)

    constructor(date: Calendar?, name: String?) : this (/*id = UUID.randomUUID().toString(),*/ dateString = null, name = name) {
        this.date = date
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        fun parseDate(string: String?): Calendar? {
            if (string == null) return null
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(string)
            return cal
        }

        fun parseTime(string: String?): Calendar? {
            if (string == null) return null
            val cal = Calendar.getInstance()
            cal.time = timeFormat.parse(string)
            return cal
        }

        fun formatDate(calendar: Calendar?): String? =
                if (calendar == null) null else dateFormat.format(calendar.time)

        fun formatTime(calendar: Calendar?): String? =
                if (calendar == null) null else timeFormat.format(calendar.time)
    }

    var date: Calendar?
        @Exclude
        get () = parseDate(dateString)
        @Exclude
        set(value) {
            dateString = formatDate(value)
        }

}