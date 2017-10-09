package checkin.timothyolt.com.roster.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import checkin.timothyolt.com.roster.R
import checkin.timothyolt.com.roster.data.Event
import checkin.timothyolt.com.roster.data.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class PersonActivity : Activity() {

    companion object {
        private val PARAM_CARD_ID = "cardId"
        private val PARAM_EVENT_ID = "eventId"
        fun createIntent(context: Context, cardId: String?, eventId: String?) : Intent {
            val intent = Intent(context, PersonActivity::class.java)
            intent.putExtra(PARAM_CARD_ID, cardId)
            intent.putExtra(PARAM_EVENT_ID, eventId)
            return intent
        }
    }

    private var cardId: String? = null
    private var eventId: String? = null
    private var firstNameLayout: TextInputLayout? = null
    private var firstNameText: TextInputEditText? = null
    private var lastNameLayout : TextInputLayout? = null
    private var lastNameText: TextInputEditText? = null
    private var netIdLayout: TextInputLayout? = null
    private var netIdText: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardId = intent?.getStringExtra(PARAM_CARD_ID)
        eventId = intent?.getStringExtra(PARAM_EVENT_ID)

        setContentView(R.layout.activity_person)
        firstNameLayout = findViewById(R.id.person_first_name_layout)
        firstNameText = findViewById(R.id.person_first_name_text)
        lastNameLayout = findViewById(R.id.person_last_name_layout)
        lastNameText = findViewById(R.id.person_last_name_text)
        netIdLayout = findViewById(R.id.person_netid_layout)
        netIdText = findViewById(R.id.person_netid_text)

        class afterTextChangedListener(val function: () -> Unit) : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                function()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        }

        firstNameText?.addTextChangedListener (afterTextChangedListener({ checkFirstName() }))
        lastNameText?.addTextChangedListener (afterTextChangedListener({ checkLastName() }))
        netIdText?.addTextChangedListener (afterTextChangedListener({ checkNetid() }))

        setActionBar(findViewById(R.id.toolbar))
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun checkFirstName() : Boolean {
        val firstNameMissing = firstNameText?.text?.isBlank() ?: true
        firstNameLayout?.error = if (firstNameMissing) "required" else ""
        firstNameLayout?.isErrorEnabled = firstNameMissing
        return firstNameMissing
    }

    private fun checkLastName() : Boolean {
        val lastNameMissing = lastNameText?.text?.isBlank() ?: true
        lastNameLayout?.error = if (lastNameMissing) "required" else ""
        lastNameLayout?.isErrorEnabled = lastNameMissing
        return lastNameMissing
    }

    private fun checkNetid() : Boolean {
        val netIdMissing = netIdText?.text?.isBlank() ?: true
        val netIdMalformed = netIdMissing || Regex("[@.#\$\\[\\]]").containsMatchIn(netIdText?.text ?: "")
        netIdLayout?.error = when {
            netIdMissing -> "required"
            netIdMalformed -> "without @students.kennesaw.edu"
            else -> ""
        }
        netIdLayout?.isErrorEnabled = netIdMalformed
        return netIdMalformed
    }

    override fun onPause() {
        super.onPause()
        val firstError = checkFirstName()
        val lastError = checkLastName()
        val netidError = checkNetid()
        if (firstError || lastError || netidError) return

        val person = Person(
                firstNameText?.text.toString(),
                lastNameText?.text.toString(),
                netIdText?.text.toString()
        )
        val db = FirebaseDatabase.getInstance().reference
        db.child("people").child(person.netId).setValue(person)
        if (cardId != null) db.child("cards").child(cardId).child("personId").setValue(person.netId)
        if (eventId != null) db.child("events").child(eventId).child("attendees").child(person.netId)
                    .setValue(Event.formatTime(Calendar.getInstance()))
    }
}