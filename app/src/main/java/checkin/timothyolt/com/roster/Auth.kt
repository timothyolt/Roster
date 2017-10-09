package checkin.timothyolt.com.roster

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object Auth {
    private val auth = FirebaseAuth.getInstance()!!

    fun checkAuth(context: Context) : Boolean {
        auth.currentUser
        return true
    }
}