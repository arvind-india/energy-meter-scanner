package ds.meterscanner.auth

import L
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.experimental.suspendAtomicCancellableCoroutine


class Authenticator(
    private val auth: FirebaseAuth,
    private val firebaseAnalytics: FirebaseAnalytics
) {

    private val authListeners = mutableMapOf<String, FirebaseAuth.AuthStateListener>()

    init {
        L.i("::: AuthManager initialized")
    }

    fun startListen(obj: Any, callback: (Boolean) -> Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            callback(user != null)
            firebaseAnalytics.setUserId(user?.email ?: user?.uid)
            firebaseAnalytics.setUserProperty("email", user?.email)
        }
        authListeners.put(obj.toString(), listener)
        auth.addAuthStateListener(listener)
    }

    fun stopListen(obj: Any) {
        auth.removeAuthStateListener(authListeners[obj.toString()]!!)
        authListeners.remove(obj.toString())
    }

    suspend fun signIn(login: String, pass: String) = suspendAtomicCancellableCoroutine<Unit> { suspendable ->
        auth.signInWithEmailAndPassword(login, pass)
            .addOnSuccessListener { suspendable.resume(Unit) }
            .addOnFailureListener { suspendable.resumeWithException(it) }
    }

    suspend fun signUp(login: String, pass: String) = suspendAtomicCancellableCoroutine<Unit> { suspendable ->
        auth.createUserWithEmailAndPassword(login, pass)
            .addOnSuccessListener { suspendable.resume(Unit) }
            .addOnFailureListener { suspendable.resumeWithException(it) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn() = getUser() != null
}