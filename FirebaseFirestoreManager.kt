package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseFirestoreManager {

    private const val TAG = "FirebaseFirestore"

    /**
     * Save or update user profile in Firestore 'users' collection
     */
    suspend fun saveUserToFirestore(user: UserEntity): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "gameUsername" to user.gameUsername,
                "freeFireUid" to user.freeFireUid,
                "depositBalance" to user.depositBalance,
                "winningBalance" to user.winningBalance,
                "totalWinnings" to user.totalWinnings,
                "referralCode" to user.referralCode,
                "totalKills" to user.totalKills,
                "matchesPlayed" to user.matchesPlayed,
                "referredBy" to user.referredBy,
                "isAdmin" to user.isAdmin,
                "isBlocked" to user.isBlocked,
                "isBanned" to user.isBanned,
                "banReason" to user.banReason,
                "bannedAt" to user.bannedAt,
                "isPhoneVerified" to user.isPhoneVerified,
                "deviceFingerprint" to user.deviceFingerprint,
                "firebaseUid" to user.firebaseUid,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(user.id)
                .set(userMap, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced user ${user.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync error: ${e.message}")
            false
        }
    }

    /**
     * Fetch user profile from Firestore 'users' collection
     */
    suspend fun getUserFromFirestore(userId: String): Map<String, Any>? {
        return try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user from Firestore: ${e.message}")
            null
        }
    }

    /**
     * Store audit log in Firestore 'audit_logs' collection
     */
    suspend fun saveAuditLogToFirestore(log: AuditLogEntity): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val logMap = hashMapOf(
                "id" to log.id,
                "userId" to log.userId,
                "userName" to log.userName,
                "action" to log.action,
                "details" to log.details,
                "severity" to log.severity,
                "deviceFingerprint" to log.deviceFingerprint,
                "timestamp" to log.timestamp
            )
            db.collection("audit_logs").document(log.id).set(logMap).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save audit log to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Save or update transaction record in Firestore 'transactions' collection
     */
    suspend fun saveTransactionToFirestore(tx: TransactionEntity): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val txMap = hashMapOf(
                "id" to tx.id,
                "userId" to tx.userId,
                "title" to tx.title,
                "amount" to tx.amount,
                "type" to tx.type,
                "status" to tx.status,
                "timestamp" to tx.timestamp,
                "note" to tx.note
            )
            db.collection("transactions")
                .document(tx.id)
                .set(txMap, SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced transaction ${tx.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save transaction to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Fetch user transactions from Firestore 'transactions' collection
     */
    suspend fun getTransactionsFromFirestore(userId: String): List<TransactionEntity> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val uId = doc.getString("userId") ?: userId
                val title = doc.getString("title") ?: "Transaction"
                val amount = doc.getDouble("amount") ?: 0.0
                val type = doc.getString("type") ?: "DEPOSIT"
                val status = doc.getString("status") ?: "SUCCESS"
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val note = doc.getString("note") ?: ""
                TransactionEntity(id, uId, title, amount, type, status, timestamp, note)
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch transactions from Firestore: ${e.message}")
            emptyList()
        }
    }
}
