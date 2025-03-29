package com.example.spenso.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing user data in Firestore.
 */
class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val TAG = "UserRepository"
    
    /**
     * Creates or updates a user document in Firestore.
     * 
     * @param user The Firebase user to save
     * @return True if the operation was successful, false otherwise
     */
    suspend fun saveUser(firebaseUser: FirebaseUser): Boolean {
        return try {
            // Check if user already exists
            val existingUser = getUser(firebaseUser.uid)
            val currentTimestamp = Timestamp.now()
            
            // Create a User object
            val userData = if (existingUser != null) {
                // Update existing user, preserving currency and join date
                existingUser.copy(
                    name = (firebaseUser.displayName ?: existingUser.name),
                    email = (firebaseUser.email ?: existingUser.email),
                    photoUrl = (firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl),
                    lastLogin = currentTimestamp
                )
            } else {
                // Create new user
                User(
                    userId = firebaseUser.uid,
                    name = (firebaseUser.displayName ?: ""),
                    email = (firebaseUser.email ?: ""),
                    photoUrl = (firebaseUser.photoUrl?.toString() ?: ""),
                    joinDate = currentTimestamp,
                    lastLogin = currentTimestamp
                )
            }
            
            Log.d(TAG, "Saving user to Firestore: ${firebaseUser.uid}")
            
            // Save the user data using the user's UID as the document ID
            // Use merge option to avoid overwriting existing data if user exists
            usersCollection.document(firebaseUser.uid).set(userData.toMap(), SetOptions.merge()).await()
            Log.d(TAG, "User saved successfully to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore", e)
            false
        }
    }
    
    /**
     * Checks if a user exists in Firestore.
     * 
     * @param userId The user ID to check
     * @return True if the user exists, false otherwise
     */
    suspend fun userExists(userId: String): Boolean {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user exists", e)
            false
        }
    }
    
    /**
     * Gets a user from Firestore.
     * 
     * @param userId The user ID to get
     * @return The User object, or null if the user doesn't exist
     */
    suspend fun getUser(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.data?.let { User.fromMap(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user", e)
            null
        }
    }
    
    /**
     * Updates a user's profile in Firestore.
     * 
     * @param userId The user ID to update
     * @param updates Map of fields to update
     * @return True if the operation was successful, false otherwise
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(userId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            false
        }
    }
    
    /**
     * Updates a user's profile in Firestore using a User object.
     * 
     * @param user The User object with updated fields
     * @return True if the operation was successful, false otherwise
     */
    suspend fun updateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId).set(user.toMap(), SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }
    
    /**
     * Updates the last login timestamp for a user.
     * 
     * @param userId The user ID to update
     * @return True if the operation was successful, false otherwise
     */
    suspend fun updateLastLogin(userId: String): Boolean {
        return try {
            val updates = mapOf("lastLogin" to Timestamp.now())
            usersCollection.document(userId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last login", e)
            false
        }
    }
} 