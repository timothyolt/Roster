package checkin.timothyolt.com.roster.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.Group
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import checkin.timothyolt.com.roster.R
import checkin.timothyolt.com.roster.data.Event
import checkin.timothyolt.com.roster.data.Person
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PersonActivity : Activity() {

    companion object {
        private val PARAM_CARD_ID = "cardId"
        private val PARAM_EVENT_ID = "eventId"
        private val PARAM_PERSON_ID = "personId"
        fun createIntent(context: Context, cardId: String?, eventId: String?) : Intent {
            val intent = Intent(context, PersonActivity::class.java)
            intent.putExtra(PARAM_CARD_ID, cardId)
            intent.putExtra(PARAM_EVENT_ID, eventId)
            return intent
        }
        fun createIntent(context: Context, personId: String?) : Intent {
            val intent = Intent(context, PersonActivity::class.java)
            intent.putExtra(PARAM_PERSON_ID, personId)
            return intent
        }
    }

    private var cardId: String? = null
    private var eventId: String? = null
    private var personId: String? = null
    private var firstNameLayout: TextInputLayout? = null
    private var firstNameText: TextInputEditText? = null
    private var lastNameLayout : TextInputLayout? = null
    private var lastNameText: TextInputEditText? = null
    private var netIdLayout: TextInputLayout? = null
    private var netIdText: TextInputEditText? = null
    private var discordName: TextView? = null
    private var discordGroup: Group? = null
    private var tempDiscordName: TextView? = null
    private var tempDiscordConfirm: Button? = null
    private var tempDiscordGroup: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardId = intent?.getStringExtra(PARAM_CARD_ID)
        eventId = intent?.getStringExtra(PARAM_EVENT_ID)
        personId = intent?.getStringExtra(PARAM_PERSON_ID)

        setContentView(R.layout.activity_person)
        firstNameLayout = findViewById(R.id.person_first_name_layout)
        firstNameText = findViewById(R.id.person_first_name_text)
        lastNameLayout = findViewById(R.id.person_last_name_layout)
        lastNameText = findViewById(R.id.person_last_name_text)
        netIdLayout = findViewById(R.id.person_netid_layout)
        netIdText = findViewById(R.id.person_netid_text)
        discordName = findViewById(R.id.person_discord)
        discordGroup = findViewById(R.id.person_discord_group)
        tempDiscordName = findViewById(R.id.person_temp_discord)
        tempDiscordConfirm = findViewById(R.id.person_temp_discord_confirm)
        tempDiscordGroup = findViewById(R.id.person_temp_discord_group)

        class AfterTextChangedListener(val function: (Editable?) -> Unit) : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                function(editable)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        }

        firstNameText?.addTextChangedListener (AfterTextChangedListener({ checkFirstName() }))
        lastNameText?.addTextChangedListener (AfterTextChangedListener({ checkLastName() }))
        netIdText?.addTextChangedListener (AfterTextChangedListener({ checkNetid() }))

        setActionBar(findViewById(R.id.toolbar))
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (personId != null) {
            netIdText?.setText(personId)
            netIdLayout?.isEnabled = false
            val personRef = FirebaseDatabase.getInstance().reference.child("people").child(personId)
            personRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    FirebaseCrash.report(error?.toException())
                }

                override fun onDataChange(data: DataSnapshot?) {
                    val person = data?.getValue(Person::class.java) ?: return
                    firstNameText?.setText(person.firstName)
                    lastNameText?.setText(person.lastName)
                    if (person.discordId != null && person.discordName != null) {
                        discordName?.text = person.discordName
                        discordGroup?.visibility = View.VISIBLE
                    }
                    else
                        discordGroup?.visibility = View.GONE
                    if (person.tempDiscordId != null && person.tempDiscordName != null) {
                        tempDiscordName?.text = person.tempDiscordName
                        tempDiscordGroup?.visibility = View.VISIBLE
                        tempDiscordConfirm?.setOnClickListener {
                            personRef.child("discordName").setValue(person.tempDiscordName)
                            personRef.child("discordId").setValue(person.tempDiscordId)
                            personRef.child("tempDiscordName").removeValue()
                            personRef.child("tempDiscordId").removeValue()
                        }
                    }
                    else
                        tempDiscordGroup?.visibility = View.GONE
                }
            })
            firstNameText?.setOnFocusChangeListener { _, focused ->
                if (!focused && !checkFirstName())
                    personRef.child("firstName").setValue(firstNameText?.text)
            }
            lastNameText?.setOnFocusChangeListener { _, focused ->
                if (!focused && !checkLastName())
                    personRef.child("lastName").setValue(lastNameText?.text)
            }
        }
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
        fun netIdMissing() = netIdText?.text?.isBlank() ?: true
        fun netIdMalformed() = Regex("[@.#\$\\[\\]]").containsMatchIn(netIdText?.text ?: "")
        while (netIdMalformed())
            netIdText?.setText(netIdText?.text?.substring((netIdText?.text?.length ?: 0) - 1))
        netIdLayout?.error = when {
            netIdMissing() -> "required"
            netIdMalformed() -> "without @students.kennesaw.edu"
            else -> ""
        }
        netIdLayout?.isErrorEnabled = netIdMissing() || netIdMalformed()
        return netIdMissing() || netIdMalformed()
    }

    override fun onPause() {
        super.onPause()
        if (personId == null) {
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
        else {
            val personRef = FirebaseDatabase.getInstance().reference.child("people").child(personId)
            if (!checkFirstName())
                personRef.child("firstName").setValue(firstNameText?.text.toString())
            if (!checkLastName())
                personRef.child("lastName").setValue(lastNameText?.text.toString())
        }
    }
}