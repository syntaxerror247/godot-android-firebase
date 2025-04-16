package org.godotengine.plugin.firebase

import android.app.Activity
import android.content.Intent
import android.view.View
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class FirebasePlugin(godot: Godot) : GodotPlugin(godot) {
    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    private val authManager = FirebaseAuthManager(this)
    private val firestoreManager = FirebaseFirestoreManager(this)

    override fun onMainCreate(activity: Activity?): View? {
        activity?.let { authManager.init(it) }
        return super.onMainCreate(activity)
    }

    override fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authManager.handleActivityResult(requestCode, resultCode, data)
    }

    override fun getPluginSignals(): MutableSet<SignalInfo> {
        val signals: MutableSet<SignalInfo> = mutableSetOf()
        signals.addAll(authManager.authSignals())
        signals.addAll(firestoreManager.firestoreSignals())
        return signals
    }

    fun emitGodotSignal(signalName: String, arg: Any) {
        emitSignal(signalName, arg)
    }

    /**
     * Authentication Manager
     */
    @UsedByGodot
    fun signInAnonymously() = authManager.signInAnonymously()

    @UsedByGodot
    fun createUserWithEmailPassword(email: String, password: String) = authManager.createUserWithEmailPassword(email, password)

    @UsedByGodot
    fun signInWithEmailPassword(email: String, password: String) = authManager.signInWithEmailPassword(email, password)

    @UsedByGodot
    fun sendEmailVerification() = authManager.sendEmailVerification()

    @UsedByGodot
    fun sendPasswordResetEmail(email: String) = authManager.sendPasswordResetEmail(email)

    @UsedByGodot
    fun signInWithGoogle() = authManager.signInWithGoogle()

    @UsedByGodot
    fun getCurrentUser() = authManager.getCurrentUser()

    @UsedByGodot
    fun isSignedIn() = authManager.isSignedIn()

    @UsedByGodot
    fun signOut() = authManager.signOut()

    @UsedByGodot
    fun deleteUser() = authManager.deleteUser()

    /**
     * Firestore Manager
     */

    @UsedByGodot
    fun firestoreAddOrSetDocument(collection: String, documentId: String, data: Dictionary) = firestoreManager.addOrSetDocument(collection, documentId, data)

    @UsedByGodot
    fun firestoreGetDocument(collection: String, documentId: String) = firestoreManager.getDocument(collection, documentId)

    @UsedByGodot
    fun firestoreUpdateDocument(collection: String, documentId: String, data: Dictionary) = firestoreManager.updateDocument(collection, documentId, data)

    @UsedByGodot
    fun firestoreDeleteDocument(collection: String, documentId: String) = firestoreManager.deleteDocument(collection, documentId)
}
