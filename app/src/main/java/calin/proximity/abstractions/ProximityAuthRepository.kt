package calin.proximity.abstractions

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import rx.Single

/**
 * Created by calin on 10/24/2016.
 */
data class ProximityUser(var details: FirebaseUser)

object ProximityAuthRepository {
    private var mFirebaseAuth = FirebaseAuth.getInstance()

    val user: ProximityUser?
        get() = when (mFirebaseAuth.currentUser) {
            null -> null
            else -> ProximityUser(mFirebaseAuth.currentUser!!)
        }

    fun signInWithGoogle(account: GoogleSignInAccount): Single<ProximityUser> {
        return Single.create { subscriber ->
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    subscriber.onError(task.exception)
                } else {
                    subscriber.onSuccess(ProximityUser(task.result.user!!))
                }
            }
        }
    }
}