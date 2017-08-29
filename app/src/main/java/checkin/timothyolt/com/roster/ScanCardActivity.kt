package checkin.timothyolt.com.roster

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.util.Log
import android.view.LayoutInflater
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ScanCardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null || NfcAdapter.ACTION_TECH_DISCOVERED != intent.action) return
        val tag = intent.getParcelableExtra<Tag?>(NfcAdapter.EXTRA_TAG)
        val mfc = if (tag == null || tag.id == null) null else MifareClassic.get(tag)
        val card = if (tag == null || mfc == null) null else Card(tag, mfc)
        Log.d("tim", card?.toString() ?: "null")
        if (card?.id == null) return
        val db = FirebaseDatabase.getInstance().reference
        val cardRef = db.child(Card::class.java.dbNames()).child(card.id)
        val progress = ProgressDialog.show(this, "", "")
        cardRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot?) {
                progress.dismiss()
                if (data != null && data.exists() &&
                        !data.getValue(Card::class.java)?.personId.isNullOrEmpty()) {
                    showPersonInfo(data.getValue(Card::class.java)?.personId!!)
                    return
                }
                db.child(Card::class.java.dbNames()).child(card.id).setValue(card)
                getPersonInfo(card.id!!)
            }

            override fun onCancelled(err: DatabaseError?) {
                progress.dismiss()
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    fun showPersonInfo(personId: String) {
        val personRef = FirebaseDatabase.getInstance().reference
                .child(Person::class.java.dbNames()).child(personId)
        personRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot?) {
                val person = data?.getValue(Person::class.java) ?: return
                AlertDialog.Builder(this@ScanCardActivity)
                        .setTitle("Checked in")
                        .setMessage("${person.firstName} ${person.lastName}\n${person.netId}@students.kennesaw.edu")
                        .show()
            }

            override fun onCancelled(err: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    @SuppressLint("InflateParams")
    fun getPersonInfo(cardId: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_person, null)
        val alert = AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("submit", null)
                .setNegativeButton("cancel", null)
                .show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val firstNameLayout = view.findViewById<TextInputLayout>(R.id.person_first_name_layout)
            val firstNameText = view.findViewById<TextInputEditText>(R.id.person_first_name_text)
            val lastNameLayout = view.findViewById<TextInputLayout>(R.id.person_last_name_layout)
            val lastNameText = view.findViewById<TextInputEditText>(R.id.person_last_name_text)
            val netIdLayout = view.findViewById<TextInputLayout>(R.id.person_netid_layout)
            val netIdText = view.findViewById<TextInputEditText>(R.id.person_netid_text)

            val firstNameMissing = firstNameText.text.isBlank()
            val lastNameMissing = lastNameText.text.isBlank()
            val netIdMissing = netIdText.text.isBlank()
            val netIdMalformed = netIdMissing || Regex("[@.#\$\\[\\]]").containsMatchIn(netIdText.text)

            firstNameLayout.error = if (firstNameMissing) "required" else ""
            firstNameLayout.isErrorEnabled = firstNameMissing
            lastNameLayout.error = if (lastNameMissing) "required" else ""
            lastNameLayout.isErrorEnabled = lastNameMissing
            netIdLayout.error = when {
                netIdMissing -> "required"
                netIdMalformed -> "without @students.kennesaw.edu"
                else -> ""
            }
            netIdLayout.isErrorEnabled = netIdMalformed

            if (firstNameMissing || lastNameMissing || netIdMalformed) return@setOnClickListener

            val person = Person(
                    firstNameText.text.toString(),
                    lastNameText.text.toString(),
                    netIdText.text.toString()
            )
            val db = FirebaseDatabase.getInstance().reference
            db.child(Person::class.java.dbNames()).child(person.netId).setValue(person)
            db.child(Card::class.java.dbNames()).child(cardId).child("personId").setValue(person.netId)

            alert.dismiss()
        }
    }
}