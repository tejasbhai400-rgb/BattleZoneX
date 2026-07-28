package com.example.security

import android.content.Context
import android.os.Build
import android.os.Debug
import android.provider.Settings
import com.example.data.*
import java.io.File
import java.security.MessageDigest
import java.util.UUID

data class SecurityStatus(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isDebuggerAttached: Boolean,
    val isTampered: Boolean,
    val riskScore: Int, // 0 to 100
    val riskLevel: String // "SAFE", "MODERATE", "HIGH_RISK", "CRITICAL"
)

data class SecurityValidationResult(
    val isValid: Boolean,
    val errorMessage: String = "",
    val riskScore: Int = 0
)

data class LedgerAuditResult(
    val isAudited: Boolean,
    val expectedDeposit: Double,
    val expectedWinning: Double,
    val discrepancy: Double,
    val auditNote: String
)

data class BanCheckResult(
    val isBanned: Boolean,
    val banReason: String = "",
    val bannedAt: Long = 0L
)

object SecurityManager {

    /**
     * Generate a unique, deterministic device fingerprint
     */
    fun getDeviceFingerprint(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ID"
            val raw = "${Build.BRAND}:${Build.MODEL}:${Build.HARDWARE}:$androidId"
            val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
            val hex = bytes.take(6).joinToString("") { "%02x".format(it) }.uppercase()
            "DEV-$hex-SEC"
        } catch (_: Exception) {
            "DEV-STANDARD-8F92A1"
        }
    }

    /**
     * Perform root, emulator, debugger, and anti-tampering checks
     */
    fun performSecurityScan(context: Context): SecurityStatus {
        var riskScore = 0

        // Root Detection
        val isRooted = checkRootPaths()
        if (isRooted) riskScore += 35

        // Emulator Detection
        val isEmulator = checkEmulatorProperties()
        if (isEmulator) riskScore += 25

        // Debugger Check
        val isDebuggerAttached = Debug.isDebuggerConnected()
        if (isDebuggerAttached) riskScore += 15

        // Tamper Check (Package Name Verification)
        val expectedPackage = "com.aistudio.battlix.esports"
        val isTampered = context.packageName != expectedPackage && !context.packageName.startsWith("com.example")
        if (isTampered) riskScore += 40

        val level = when {
            riskScore >= 70 -> "CRITICAL"
            riskScore >= 40 -> "HIGH_RISK"
            riskScore >= 20 -> "MODERATE"
            else -> "SAFE"
        }

        return SecurityStatus(
            isRooted = isRooted,
            isEmulator = isEmulator,
            isDebuggerAttached = isDebuggerAttached,
            isTampered = isTampered,
            riskScore = riskScore.coerceIn(0, 100),
            riskLevel = level
        )
    }

    private fun checkRootPaths(): Boolean {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in rootPaths) {
            if (File(path).exists()) return true
        }
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkEmulatorProperties(): Boolean {
        val isGenymotion = Build.MANUFACTURER.contains("Genymotion")
        val isQemu = Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
        return isGenymotion || isQemu
    }

    /**
     * Compute a HMAC-like checksum for wallet protection
     */
    fun computeWalletChecksum(deposit: Double, winning: Double, userId: String): String {
        val raw = "BTX_SEC:$userId:DEP_%.2f:WIN_%.2f".format(deposit, winning)
        val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(8).uppercase()
    }

    /**
     * Verify double-entry ledger against transaction history
     */
    fun auditWalletLedger(
        userId: String,
        currentDeposit: Double,
        currentWinning: Double,
        transactions: List<TransactionEntity>
    ): LedgerAuditResult {
        var calculatedDeposit = 0.0
        var calculatedWinning = 0.0

        for (tx in transactions) {
            if (tx.userId != userId || tx.status != "SUCCESS") continue

            when (tx.type) {
                "DEPOSIT" -> calculatedDeposit += tx.amount
                "REFERRAL_BONUS" -> calculatedDeposit += tx.amount
                "ENTRY_FEE" -> {
                    // Deducted from deposit first, then winning
                    if (calculatedDeposit >= tx.amount) {
                        calculatedDeposit -= tx.amount
                    } else {
                        val remainder = tx.amount - calculatedDeposit
                        calculatedDeposit = 0.0
                        calculatedWinning -= remainder
                    }
                }
                "WINNING" -> calculatedWinning += tx.amount
                "WITHDRAWAL" -> calculatedWinning -= tx.amount
            }
        }

        val totalCurrent = currentDeposit + currentWinning
        val totalCalculated = calculatedDeposit + calculatedWinning
        val diff = Math.abs(totalCurrent - totalCalculated)

        val isClean = diff < 1.0

        return LedgerAuditResult(
            isAudited = isClean,
            expectedDeposit = Math.max(0.0, calculatedDeposit),
            expectedWinning = Math.max(0.0, calculatedWinning),
            discrepancy = diff,
            auditNote = if (isClean) "Double-Entry Ledger Verified OK" else "Ledger discrepancy detected: ₹%.2f".format(diff)
        )
    }

    /**
     * Prevent duplicate accounts on same device / phone / email / Free Fire UID / game IGN
     */
    fun validateRegistrationSecurity(
        phone: String,
        email: String,
        freeFireUid: String,
        gameUsername: String,
        deviceFingerprint: String,
        existingUsers: List<UserEntity>
    ): SecurityValidationResult {
        // Clean input & extract 10 digits for phone
        val cleanPhone = phone.trim()
        val phoneDigits = cleanPhone.filter { it.isDigit() }.takeLast(10)
        val cleanEmail = email.trim().lowercase()
        val cleanFfUid = freeFireUid.filter { it.isDigit() }.trim()
        val cleanIgn = gameUsername.trim().lowercase()

        // Validate phone number presence and length
        if (phoneDigits.length != 10) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "A valid 10-digit mobile number (+91) is required for registration!",
                riskScore = 50
            )
        }

        // Check duplicate phone (digit comparison)
        if (existingUsers.any { u -> u.phone.filter { it.isDigit() }.takeLast(10) == phoneDigits }) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Security Error: Phone number (+91 $phoneDigits) is already registered!",
                riskScore = 80
            )
        }

        // Check duplicate email
        if (cleanEmail.isNotBlank() && existingUsers.any { it.email.trim().lowercase() == cleanEmail }) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Security Error: Email address '$cleanEmail' is already registered!",
                riskScore = 80
            )
        }

        // Check duplicate Free Fire UID
        if (cleanFfUid.isNotBlank() && existingUsers.any { it.freeFireUid.filter { char -> char.isDigit() } == cleanFfUid }) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Security Error: Free Fire UID '$cleanFfUid' is already registered to another account!",
                riskScore = 85
            )
        }

        // Check duplicate Game IGN
        if (cleanIgn.isNotBlank() && existingUsers.any { it.gameUsername.trim().lowercase() == cleanIgn }) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Security Error: In-Game Name '$gameUsername' is already claimed!",
                riskScore = 60
            )
        }

        // Check device account count (Max 2 accounts per physical device)
        val accountsOnDevice = existingUsers.count { it.deviceFingerprint == deviceFingerprint }
        if (accountsOnDevice >= 2) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Multi-Account Security Violation: Maximum 2 accounts allowed per device!",
                riskScore = 90
            )
        }

        return SecurityValidationResult(isValid = true, riskScore = 0)
    }

    /**
     * Check UTR uniqueness across deposit records
     */
    fun validateUtrUniqueness(utr: String, deposits: List<DepositEntity>): SecurityValidationResult {
        val cleanUtr = utr.trim().uppercase()
        if (cleanUtr.length < 6) {
            return SecurityValidationResult(isValid = false, errorMessage = "UTR / Payment Reference must be at least 6 digits.")
        }

        val duplicate = deposits.find { it.utrNumber.trim().uppercase() == cleanUtr && it.status != "REJECTED" }
        if (duplicate != null) {
            return SecurityValidationResult(
                isValid = false,
                errorMessage = "Security Flag: UTR '$cleanUtr' has already been submitted for deposit!",
                riskScore = 95
            )
        }

        return SecurityValidationResult(isValid = true)
    }

    /**
     * Check Ban status for user or device
     */
    fun checkBanStatus(
        userId: String,
        deviceFingerprint: String,
        user: UserEntity?,
        bannedDevices: List<BannedDeviceEntity>
    ): BanCheckResult {
        if (user != null && (user.isBanned || user.isBlocked)) {
            return BanCheckResult(
                isBanned = true,
                banReason = if (user.banReason.isNotBlank()) user.banReason else "Account suspended due to policy violation or security flag.",
                bannedAt = user.bannedAt
            )
        }

        val bannedDev = bannedDevices.find { it.deviceFingerprint == deviceFingerprint }
        if (bannedDev != null) {
            return BanCheckResult(
                isBanned = true,
                banReason = "Device banned (${bannedDev.reason})",
                bannedAt = bannedDev.bannedAt
            )
        }

        return BanCheckResult(isBanned = false)
    }
}
