package org.godotengine.plugin.firebase

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin

class FirebasePlugin(godot: Godot) : GodotPlugin(godot) {
    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME
}
