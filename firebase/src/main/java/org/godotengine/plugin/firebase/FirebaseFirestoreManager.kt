package org.godotengine.plugin.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo

class FirebaseFirestoreManager(private val plugin: FirebasePlugin) {
	companion object {
		private const val TAG = "FirebaseFirestore"
	}

	private val db = Firebase.firestore

	fun firestoreSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("firestore_write_success", String::class.java))
		signals.add(SignalInfo("firestore_get_success", Dictionary::class.java))
		signals.add(SignalInfo("firestore_update_success", String::class.java))
		signals.add(SignalInfo("firestore_delete_success", String::class.java))
		signals.add(SignalInfo("firestore_failure", String::class.java))
		return signals
	}

	fun addOrSetDocument(collection: String, documentId: String, data: Dictionary) {
		val map = data.toMap()

		if (documentId.isEmpty()) {
			db.collection(collection).add(map)
				.addOnSuccessListener { documentRef ->
					val docId = documentRef.id
					Log.d(TAG, "Document added with ID: $docId")
					plugin.emitGodotSignal("firestore_write_success", docId)
				}
				.addOnFailureListener { e ->
					Log.e(TAG, "Error adding document:", e)
					plugin.emitGodotSignal("firestore_failure", "Firestore addDocument: ${e.message}")
				}
		} else {
			db.collection(collection).document(documentId).set(map)
				.addOnSuccessListener {
					Log.d(TAG, "Document $documentId set successfully")
					plugin.emitGodotSignal("firestore_write_success", documentId)
				}
				.addOnFailureListener { e ->
					Log.e(TAG, "Error setting document:", e)
					plugin.emitGodotSignal("firestore_failure", "Firestore setDocument: ${e.message}")
				}
		}
	}

	fun getDocument(collection: String, documentId: String) {
		db.collection(collection).document(documentId).get()
			.addOnSuccessListener { documentSnapshot ->
				if (documentSnapshot.exists()) {
					// Convert the document data (Map) to a Godot Dictionary
					val data = documentSnapshot.data?.let { map ->
						val dictionary = Dictionary()
						for ((key, value) in map) {
							dictionary[key] = value
						}
						dictionary
					} ?: Dictionary()
					Log.d(TAG, "Document $documentId retrieved successfully")
					plugin.emitGodotSignal("firestore_get_success", data)
				} else {
					Log.e(TAG, "Document $documentId does not exist")
					plugin.emitGodotSignal("firestore_failure", "Document does not exist")
				}
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Error getting document:", e)
				plugin.emitGodotSignal("firestore_failure", "Firestore getDocument: ${e.message}")
			}
	}

	fun updateDocument(collection: String, documentId: String, data: Dictionary) {
		val map = data.toMap()
		db.collection(collection).document(documentId).update(map)
			.addOnSuccessListener {
				Log.d(TAG, "Document $documentId updated successfully")
				plugin.emitGodotSignal("firestore_update_success", documentId)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Error updating document:", e)
				plugin.emitGodotSignal("firestore_failure", "Firestore updateDocument: ${e.message}")
			}
	}

	fun deleteDocument(collection: String, documentId: String) {
		db.collection(collection).document(documentId).delete()
			.addOnSuccessListener {
				Log.d(TAG, "Document $documentId deleted successfully")
				plugin.emitGodotSignal("firestore_delete_success", documentId)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Error deleting document:", e)
				plugin.emitGodotSignal("firestore_failure", "Firestore deleteDocument: ${e.message}")
			}
	}
}
