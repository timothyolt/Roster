package checkin.timothyolt.com.roster.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.util.Log
import android.widget.Button
import android.widget.Toast
import checkin.timothyolt.com.roster.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash


class LoginActivity : Activity() {
    private val TAG = LoginActivity::class.java.simpleName

    companion object {
        fun createIntent(context: Context, resultIntent: Intent?) : Intent {
            val intent = Intent(context, LoginActivity::class.java)
            if (resultIntent != null)
                intent.putExtras(resultIntent)
            return intent
        }
    }

    private var emailLayout: TextInputLayout? = null
    private var emailText: TextInputEditText? = null
    private var passwordLayout: TextInputLayout? = null
    private var passwordText: TextInputEditText? = null
    private var submit: Button? = null

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailLayout = findViewById(R.id.login_email_layout)
        emailText = findViewById(R.id.login_email_text)
        passwordLayout = findViewById(R.id.login_password_layout)
        passwordText = findViewById(R.id.login_password_text)
        submit = findViewById(R.id.login_submit)

        submit?.setOnClickListener {
            val emailError = checkEmail()
            val passwordError = checkPassword()
            if (emailError || passwordError) return@setOnClickListener
            lockForm(true)
            signIn("${emailText!!.text}@students.kennesaw.edu", "${passwordText!!.text}")
        }
    }

    private fun lockForm(lock: Boolean) {
        emailLayout?.isEnabled = !lock
        passwordLayout?.isEnabled = !lock
        submit?.isEnabled = !lock
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, { task ->
                    lockForm(false)
                    if (task.isSuccessful) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        Toast.makeText(this@LoginActivity, "Authenticated ${user?.email}",
                                Toast.LENGTH_SHORT).show()
                        passwordLayout?.isErrorEnabled = false
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        FirebaseCrash.logcat(Log.WARN, TAG, "signInWithEmail:failure")
                        FirebaseCrash.report(task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed. ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        passwordLayout?.error = task.exception?.message ?: "couldn't log in"
                        passwordLayout?.isErrorEnabled = true
                    }
                })
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, { task ->
                    lockForm(false)
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        Toast.makeText(this@LoginActivity, "Authenticated ${user?.email}",
                                Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        FirebaseCrash.logcat(Log.WARN, TAG, "createUserWithEmail:failure")
                        FirebaseCrash.report(task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed. ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun checkEmail() : Boolean {
        val emailMissing = emailText?.text?.isBlank() ?: true
        val emailMalformed = emailMissing || Regex("[@.#\$\\[\\]]").containsMatchIn(emailText?.text ?: "")
        passwordLayout?.error = when {
            emailMissing -> "required"
            emailMalformed -> "without @students.kennesaw.edu"
            else -> ""
        }
        passwordLayout?.isErrorEnabled = emailMalformed
        return emailMalformed
    }

    private fun checkPassword() : Boolean {
        val passwordMissing = passwordText?.text?.isBlank() ?: true
        passwordLayout?.error = if (passwordMissing) "required" else ""
        passwordLayout?.isErrorEnabled = passwordMissing
        return passwordMissing
    }
    
}