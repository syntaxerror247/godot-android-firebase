package org.godotengine.plugin.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo

class FirebaseAuthManager(private val plugin: FirebasePlugin) {
	companion object {
		private const val GOOGLE_SIGN_IN = 9001
		private const val TAG = "FirebaseAuthManager"
	}

	private lateinit var activity: android.app.Activity
	private val auth: FirebaseAuth = Firebase.auth
	private lateinit var googleSignInClient: GoogleSignInClient

	fun authSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("auth_success", Dictionary::class.java))
		signals.add(SignalInfo("auth_failure", String::class.java))
		signals.add(SignalInfo("sign_out_success", Boolean::class.javaObjectType))
		return signals
	}

	fun init(activity: Activity) {
		this.activity = activity
		val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)

		if (resId == 0) {
			Log.e("FirebaseAuthManager", "default_web_client_id not found in app resources.")
			return
		}

		val webClientId = activity.getString(resId)

		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(webClientId)
			.requestEmail()
			.build()

		googleSignInClient = GoogleSignIn.getClient(activity, gso)
	}

	fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == GOOGLE_SIGN_IN) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			try {
				val account = task.getResult(ApiException::class.java)!!
				Log.d(TAG, "authWithGoogle:" + account.id)
				authWithGoogle(account.idToken!!)
			} catch (e: ApiException) {
				Log.w(TAG, "Google sign in failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
		}
	}

	fun signInAnonymously() {
		auth.signInAnonymously()
			.addOnSuccessListener {
				val uid = it.user?.uid
				Log.d(TAG, "Signed in anonymously as $uid")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Anonymous sign-in failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun createUserWithEmailPassword(email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password)
			.addOnSuccessListener {
				Log.d(TAG, "User created with email: $email")
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "User creation failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun signInWithEmailPassword(email: String, password: String) {
		auth.signInWithEmailAndPassword(email, password)
			.addOnSuccessListener {
				Log.d(TAG, "Signed in with email: $email")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Sign-in with email failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun signInWithGoogle() {
		try {
			val signInIntent = googleSignInClient.signInIntent
			activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
		} catch (e: Exception) {
			Log.e(TAG, "Error starting Google Sign-In", e)
		}
	}

	private fun authWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnSuccessListener { authResult ->
				val uid = authResult.user?.uid
				Log.d(TAG, "signInWithCredential:success -> $uid")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.w(TAG, "signInWithCredential:failure", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun getCurrentUser(): Dictionary {
		val user = auth.currentUser
		val userData = Dictionary()
		if (user != null) {
			userData["name"] = user.displayName
			userData["email"] = user.email
			userData["photoUrl"] = user.photoUrl?.toString()
			userData["emailVerified"] = user.isEmailVerified
			userData["uid"] = user.uid
		} else {
			Log.d(TAG, "No user signed in")
		}

		return userData
	}

	fun isSignedIn(): Boolean {
		return auth.currentUser != null
	}

	fun signOut() {
		auth.signOut()
		googleSignInClient.signOut()
			.addOnCompleteListener(activity) {
				plugin.emitGodotSignal("sign_out_success", true)
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Sign out failed", e)
				plugin.emitGodotSignal("sign_out_success", false)
			}
	}
}
