package com.example.spenso.data

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a user in the application.
 * This class is used for structured data storage and retrieval from Firestore.
 */
data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val currency: String = "USD", // Default currency
    val joinDate: Timestamp = Timestamp.now(), // When the user first joined
    val lastLogin: Timestamp = Timestamp.now(), // User's last login
    // Additional user profile fields can be added here
) {
    /**
     * Convert the User object to a Map for storage in Firestore
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "name" to name,
            "photoUrl" to photoUrl,
            "currency" to currency,
            "joinDate" to joinDate,
            "lastLogin" to lastLogin
        )
    }
    
    /**
     * Get the join date as a formatted string
     */
    fun getFormattedJoinDate(): String {
        return formatDate(joinDate.toDate())
    }
    
    /**
     * Get the last login date as a formatted string
     */
    fun getFormattedLastLogin(): String {
        return formatDate(lastLogin.toDate())
    }
    
    companion object {
        /**
         * Create a User object from a Firestore document data Map
         */
        fun fromMap(data: Map<String, Any>): User {
            return User(
                userId = data["userId"] as? String ?: "",
                email = data["email"] as? String ?: "",
                name = data["name"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                currency = data["currency"] as? String ?: "USD",
                joinDate = data["joinDate"] as? Timestamp ?: Timestamp.now(),
                lastLogin = data["lastLogin"] as? Timestamp ?: Timestamp.now()
            )
        }
        
        /**
         * Format a date to a readable string
         */
        private fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return sdf.format(date)
        }
    }
} 