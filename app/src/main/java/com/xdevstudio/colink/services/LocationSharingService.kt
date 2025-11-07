package com.xdevstudio.colink.services

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.xdevstudio.colink.models.UserLocation
import kotlinx.coroutines.tasks.await

class LocationSharingService {
    private val db = FirebaseFirestore.getInstance()

    suspend fun shareLocationWithGroup(userId: String, groupId: String, location: UserLocation) {
        try {
            val locationData = hashMapOf(
                "userId" to userId,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to System.currentTimeMillis(),
                "movementMode" to location.movementMode
            )

            db.collection("groups")
                .document(groupId)
                .collection("locations")
                .document(userId)
                .set(locationData)
                .await()

        } catch (e: Exception) {
            Log.e("LocationSharing", "Failed to share location: ${e.message}")
        }
    }

    suspend fun stopSharingLocation(groupId: String, userId: String) {
        try {
            db.collection("groups")
                .document(groupId)
                .collection("locations")
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("LocationSharing", "Failed to stop sharing: ${e.message}")
        }
    }
}