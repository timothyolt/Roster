package checkin.timothyolt.com.roster

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.*

data class Event (
        //var id: String? = null,
        @Suppress("MemberVisibilityCanPrivate") // Used via reflection with Firebase, must be public
        var dateString: String? = null,
        var name: String? = null,
        var attendees: HashMap<String, Boolean>? = null,
        var attendeeCount: Int? = null
) {
    constructor(name: String?) : this(Calendar.getInstance(), name)

    constructor(date: Calendar?, name: String?) : this (/*id = UUID.randomUUID().toString(),*/ dateString = null, name = name) {
        this.date = date
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun parse(string: String?): Calendar? {
            if (string == null) return null
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(string)
            return cal
        }

        fun format(calendar: Calendar?): String? =
                if (calendar == null) null else dateFormat.format(calendar.time)
    }

    var date: Calendar?
        @Exclude
        get () = parse(dateString)
        @Exclude
        set(value) {
            dateString = format(value)
        }

}