package org.godotengine.plugin.firebase

import android.app.Activity
import android.content.Intent
import android.view.View
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class FirebasePlugin(godot: Godot) : GodotPlugin(godot) {
    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    private val authManager = FirebaseAuthManager(this)

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
        return signals
    }

    fun emitGodotSignal(signalName: String, arg: Any) {
        emitSignal(signalName, arg)
    }

    /**
     * Authentication
     */
    @UsedByGodot
    fun signInAnonymously() = authManager.signInAnonymously()

    @UsedByGodot
    fun createUserWithEmailPassword(email: String, password: String) = authManager.createUserWithEmailPassword(email, password)

    @UsedByGodot
    fun signInWithEmailPassword(email: String, password: String) = authManager.signInWithEmailPassword(email, password)

    @UsedByGodot
    fun signInWithGoogle() = authManager.signInWithGoogle()

    @UsedByGodot
    fun getCurrentUser() = authManager.getCurrentUser()

    @UsedByGodot
    fun isSignedIn() = authManager.isSignedIn()

    @UsedByGodot
    fun signOut() = authManager.signOut()
}
