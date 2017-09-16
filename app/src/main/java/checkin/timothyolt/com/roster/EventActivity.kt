package checkin.timothyolt.com.roster

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.*

class EventActivity : Activity() {
    companion object {
        private val PARAM_EVENT_KEY = "eventKey"

        fun createIntent(context: Context, event: Event? = null) : Intent {
            val intent = Intent(context, EventActivity::class.java)
            if (!event?.dateString.isNullOrEmpty())
                intent.putExtra(PARAM_EVENT_KEY, event?.dateString)
            return intent
        }
    }

    private var event: Event? = null
    private var eventRef: DatabaseReference? = null
    private var update = false

    private var nameLayout: TextInputLayout? = null
    private var nameText: TextInputEditText? = null
    private var dateLayout: TextInputLayout? = null
    private var dateText: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        setActionBar(findViewById(R.id.toolbar))
        actionBar.setDisplayHomeAsUpEnabled(true)
        nameLayout = findViewById(R.id.event_name_layout)
        nameText = findViewById(R.id.event_name_text)
        dateLayout = findViewById(R.id.event_date_layout)
        dateText = findViewById(R.id.event_date_text)

        val eventsRef = FirebaseDatabase.getInstance().reference.child("events")
        eventRef =
                if (intent.hasExtra(PARAM_EVENT_KEY))
                    eventsRef.child(intent.getStringExtra(PARAM_EVENT_KEY))
                else
                    eventsRef.child(Event("(new event)").dateString)
        eventRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(err: DatabaseError?) {
                val ex = err?.toException()
                if (ex != null) FirebaseCrash.report(ex)
            }

            override fun onDataChange(data: DataSnapshot?) {
                event = data?.getValue(Event::class.java)
                nameText?.setText(event?.name ?: "")
                dateText?.setText(event?.dateString ?: "")
                update = false
                if (data == null || !data.exists() || event == null)
                    eventRef?.setValue(Event("(new event)"))
            }
        })

        val updateWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                update = true
            }
        }

        nameText?.addTextChangedListener(updateWatcher)
        nameText?.setOnFocusChangeListener({ _: View, focused: Boolean ->
            if (focused || !update) return@setOnFocusChangeListener
            eventRef?.child("name")?.setValue(nameText?.text.toString())
            update = false
        })
        dateText?.addTextChangedListener(updateWatcher)
        dateText?.setOnFocusChangeListener({ _: View, focused: Boolean ->
            if (focused || !update) return@setOnFocusChangeListener
            eventRef?.child("dateString")?.setValue(dateText?.text.toString())
            //TODO add date validation
            update = false
        })
    }

    override fun onPause() {
        super.onPause()
        if (!update) return
        eventRef?.child("name")?.setValue(nameText?.text.toString())
        eventRef?.child("dateString")?.setValue(dateText?.text.toString())
        //TODO add date validation
        update = false
    }
}