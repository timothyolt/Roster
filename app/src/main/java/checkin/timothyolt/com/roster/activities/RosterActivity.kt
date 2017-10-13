package checkin.timothyolt.com.roster.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import checkin.timothyolt.com.roster.adapters.PersonAdapter
import checkin.timothyolt.com.roster.R
import checkin.timothyolt.com.roster.data.Card
import checkin.timothyolt.com.roster.data.Event
import checkin.timothyolt.com.roster.data.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.*
import java.util.*

class RosterActivity : Activity() {

    companion object {
        fun createIntent(context: Context) : Intent {
            return Intent(context, RosterActivity::class.java)
        }

        private val REQUEST_LOGIN = 1
    }

    private var eventCreate: Button? = null
    private var recycler: RecyclerView? = null

    private var event: Event? = null
    private var eventRef: DatabaseReference? = null
    private var eventListener: ValueEventListener? = null
    private var adapter: PersonAdapter? = null

    private fun startEventEditActivity() = startActivity(EventActivity.createIntent(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roster)
        setActionBar(findViewById(R.id.toolbar))

        eventCreate = findViewById(R.id.roster_event_create)
        eventCreate?.setOnClickListener { startEventEditActivity() }

        eventRef = FirebaseDatabase.getInstance().reference
                .child("events").child(Event.formatDate(Calendar.getInstance()))

        recycler = findViewById(R.id.roster_recycler)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recycler?.layoutManager = layoutManager
        adapter = PersonAdapter(eventRef?.child("attendees")?.orderByValue()!!, FirebaseDatabase.getInstance().reference.child("people")!!)
        adapter?.onClickListener = {person ->
            startActivity(PersonActivity.createIntent(this, person?.netId))
        }
        recycler?.adapter = adapter

        if (FirebaseAuth.getInstance().currentUser == null)
            startActivityForResult(LoginActivity.createIntent(this, intent), REQUEST_LOGIN)
        else
            eventListener = eventRef!!.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(err: DatabaseError?) {
                    val ex = err?.toException()
                    if (ex != null) FirebaseCrash.report(ex)
                }

                override fun onDataChange(data: DataSnapshot?) {
                    val dataExists = data?.exists() == true
                    if (dataExists) {
                        event = data?.getValue(Event::class.java)
                        eventCreate?.visibility = View.GONE
                        actionBar?.subtitle = event?.name ?: "(no event name)"
                        actionBar?.title = "${event?.attendeeCount ?: "No"} attendees"
                        onNewIntent(intent)
                    } else {
                        startEventEditActivity()
                        eventCreate?.visibility = View.VISIBLE
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_LOGIN -> if (resultCode != RESULT_OK) finish() else onNewIntent(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventRef?.removeEventListener(eventListener)
        adapter?.cleanup()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions_roster, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_event_edit -> {
                startEventEditActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return
        val tag = intent.getParcelableExtra<Tag?>(NfcAdapter.EXTRA_TAG) ?: return
        val mfc = if (tag.id == null) return else MifareClassic.get(tag) ?: return
        val card = Card(tag, mfc)
        if (card.id == null) return
        val db = FirebaseDatabase.getInstance().reference
        val cardRef = db.child("cards").child(card.id)
        cardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(err: DatabaseError?) {
                val ex = err?.toException()
                if (ex != null) FirebaseCrash.report(ex)
            }

            override fun onDataChange(data: DataSnapshot?) {
                if (data != null && data.exists() &&
                        !data.getValue(Card::class.java)?.personId.isNullOrEmpty()) {
                    val personId = data.getValue(Card::class.java)?.personId!!
                    checkPerson(card.id!!, event!!.dateString!!, personId)
                    if (event?.dateString != null)
                        db.child("events").child(event?.dateString).child("attendees").child(personId)
                                .setValue(Event.formatTime(Calendar.getInstance()))
                    return
                }
                db.child("cards").child(card.id).setValue(card)
                startActivity(PersonActivity.createIntent(this@RosterActivity, card.id!!, event!!.dateString!!))
            }
        })
    }

    private fun checkPerson(cardId: String, eventId: String, personId: String) {
        val personRef = FirebaseDatabase.getInstance().reference
                .child("people").child(personId)
        personRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(err: DatabaseError?) {
                val ex = err?.toException()
                if (ex != null) FirebaseCrash.report(ex)
            }

            override fun onDataChange(data: DataSnapshot?) {
                //TODO check person validity and perform an update if necessary
                val person = data?.getValue(Person::class.java)
                if (person == null)
                    startActivity(PersonActivity.createIntent(this@RosterActivity, cardId, eventId))
            }
        })
    }
}