package org.godotengine.plugin.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File
import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo

class FirebaseStorageManager(private val plugin: FirebasePlugin) {
	private val TAG = "FirebaseStorage"
	private val storage = Firebase.storage
	private val storageRef = storage.reference

	fun storageSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("storage_upload_success", Boolean::class.javaObjectType))
		signals.add(SignalInfo("storage_download_success", Boolean::class.javaObjectType))
		signals.add(SignalInfo("storage_delete_success", Boolean::class.javaObjectType))
		signals.add(SignalInfo("storage_list_success", Dictionary::class.java))
		signals.add(SignalInfo("storage_failure", String::class.java))
		return signals
	}

	fun uploadFile(path: String, localFilePath: String) {
		val fileUri = Uri.fromFile(File(localFilePath))
		val ref = storageRef.child(path)

		ref.putFile(fileUri)
			.addOnSuccessListener {
				Log.d(TAG, "File uploaded successfully: $path")
				plugin.emitGodotSignal("storage_upload_success", true)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Upload failed: $path", e)
				plugin.emitGodotSignal("storage_upload_success", false)
				plugin.emitGodotSignal("storage_failure", "Upload failed: ${e.message}")
			}
	}

	fun downloadFile(path: String, destinationPath: String) {
		val localFile = File(destinationPath)
		val ref = storageRef.child(path)

		ref.getFile(localFile)
			.addOnSuccessListener {
				Log.d(TAG, "File downloaded successfully to $destinationPath")
				plugin.emitGodotSignal("storage_download_success", true)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Download failed: $path", e)
				plugin.emitGodotSignal("storage_download_success", false)
				plugin.emitGodotSignal("storage_failure", "Download failed: ${e.message}")
			}
	}

	fun deleteFile(path: String) {
		val ref = storageRef.child(path)

		ref.delete()
			.addOnSuccessListener {
				Log.d(TAG, "File deleted: $path")
				plugin.emitGodotSignal("storage_delete_success", true)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Deletion failed: $path", e)
				plugin.emitGodotSignal("storage_delete_success", false)
				plugin.emitGodotSignal("storage_failure", "Delete failed: ${e.message}")
			}
	}

	fun listFiles(path: String) {
		val ref = storageRef.child(path)

		ref.listAll()
			.addOnSuccessListener { listResult ->
				val result = Dictionary()
				val items = listResult.items.map { it.name }
				val prefixes = listResult.prefixes.map { it.name }

				result["files"] = items.toTypedArray()
				result["folders"] = prefixes.toTypedArray()

				Log.d(TAG, "Listed files at $path: $items, folders: $prefixes")
				plugin.emitGodotSignal("storage_list_success", result)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Failed to list files at $path", e)
				plugin.emitGodotSignal("storage_failure", "List failed: ${e.message}")
			}
	}

}