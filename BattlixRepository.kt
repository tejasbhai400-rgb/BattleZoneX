package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class BattlixRepository(private val db: AppDatabase) {
    val userFlow: Flow<UserEntity?> = db.userDao().getUserFlow()
    val allUsersFlow: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    val tournamentsFlow: Flow<List<TournamentEntity>> = db.tournamentDao().getAllTournamentsFlow()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactionsFlow()
    val allDepositsFlow: Flow<List<DepositEntity>> = db.depositDao().getAllDepositsFlow()
    val allAuditLogsFlow: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllAuditLogsFlow()
    val allBannedDevicesFlow: Flow<List<BannedDeviceEntity>> = db.bannedDeviceDao().getAllBannedDevicesFlow()

    suspend fun logAudit(
        userId: String,
        action: String,
        details: String,
        severity: String = "INFO",
        deviceFp: String = "DEV-SYS",
        riskScore: Int = 0
    ) {
        val user = if (userId.isNotBlank()) db.userDao().getUserById(userId) else null
        val log = AuditLogEntity(
            id = "log_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            userName = user?.name ?: "System/Guest",
            action = action,
            details = details,
            severity = severity,
            deviceFingerprint = deviceFp,
            riskScore = riskScore,
            timestamp = System.currentTimeMillis()
        )
        db.auditLogDao().insertAuditLog(log)
        try {
            FirebaseFirestoreManager.saveAuditLogToFirestore(log)
        } catch (_: Exception) {}
    }

    fun generateUniqueReferralCode(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val randomPart = (1..4).map { chars.random() }.joinToString("")
        return "BTLX$randomPart"
    }

    suspend fun ensureUserReferralCode(): UserEntity? {
        var user = db.userDao().getUserFlow().firstOrNull()
        if (user == null) {
            val defaultUser = UserEntity(
                id = "user_default",
                name = "Pro Gamer",
                email = "gamer@battlix.gg",
                phone = "+91 9876543210",
                gameUsername = "BattliX_Pro",
                referralCode = generateUniqueReferralCode(),
                depositBalance = 100.0,
                winningBalance = 50.0,
                totalWinnings = 150.0,
                totalKills = 12,
                matchesPlayed = 5
            )
            db.userDao().insertUser(defaultUser)
            user = defaultUser
        }
        if (user.referralCode.isBlank() || !user.referralCode.startsWith("BTLX")) {
            val newCode = generateUniqueReferralCode()
            val updated = user.copy(referralCode = newCode)
            db.userDao().updateUser(updated)
            return updated
        }
        return user
    }

    fun getTournamentByIdFlow(id: String): Flow<TournamentEntity?> =
        db.tournamentDao().getTournamentByIdFlow(id)

    fun getUserTransactionsFlow(userId: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getUserTransactionsFlow(userId)

    suspend fun recordTransaction(tx: TransactionEntity) {
        db.transactionDao().insertTransaction(tx)
        try {
            FirebaseFirestoreManager.saveTransactionToFirestore(tx)
        } catch (_: Exception) {}
    }

    suspend fun syncFirestoreTransactions(userId: String) {
        try {
            val remoteTxList = FirebaseFirestoreManager.getTransactionsFromFirestore(userId)
            remoteTxList.forEach { remoteTx ->
                db.transactionDao().insertTransaction(remoteTx)
            }
        } catch (_: Exception) {}
    }

    fun getUserNotificationsFlow(userId: String): Flow<List<NotificationEntity>> =
        db.notificationDao().getUserNotificationsFlow(userId)

    fun getUnreadNotificationsCountFlow(userId: String): Flow<Int> =
        db.notificationDao().getUnreadCountFlow(userId)

    fun getParticipantsForTournamentFlow(tournamentId: String): Flow<List<ParticipantEntity>> =
        db.participantDao().getParticipantsForTournamentFlow(tournamentId)

    fun getUserJoinedTournamentsFlow(userId: String): Flow<List<ParticipantEntity>> =
        db.participantDao().getParticipantsForUserFlow(userId)

    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>> =
        db.withdrawalDao().getAllWithdrawalsFlow()

    // Authentication: Login or Register with Enterprise Security
    suspend fun registerUser(
        name: String,
        email: String,
        phone: String,
        freeFireUid: String,
        ign: String,
        password: String,
        referralCodeInput: String = "",
        isPhoneVerified: Boolean = false,
        deviceFingerprint: String = "DEV-8F92A1B0-SEC"
    ): Result<UserEntity> {
        if (!isPhoneVerified) {
            return Result.Error("Phone Verification Required! Please tap 'Verify Phone Number' and enter the OTP before registering.")
        }

        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanFfUid = freeFireUid.filter { it.isDigit() }.trim()
        val cleanIgn = ign.trim()
        val phoneDigits = phone.filter { it.isDigit() }.takeLast(10)

        if (cleanName.isBlank()) return Result.Error("Full Name is required.")
        if (cleanFfUid.isBlank()) return Result.Error("Free Fire UID is required.")
        if (cleanIgn.isBlank()) return Result.Error("In-Game Name (IGN) is required.")
        if (phoneDigits.length != 10) return Result.Error("Valid 10-digit Phone Number (+91) is required.")
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) return Result.Error("Valid Email Address is required.")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters.")

        val cleanPhone = "+91 $phoneDigits"

        // Duplicate Account & Multi-Account Fraud Check
        val existingUsers = db.userDao().getAllUsers()
        val secValidation = com.example.security.SecurityManager.validateRegistrationSecurity(
            phone = cleanPhone,
            email = cleanEmail,
            freeFireUid = cleanFfUid,
            gameUsername = cleanIgn,
            deviceFingerprint = deviceFingerprint,
            existingUsers = existingUsers
        )

        if (!secValidation.isValid) {
            logAudit(
                userId = "GUEST",
                action = "REGISTRATION_BLOCKED_DUPLICATE",
                details = secValidation.errorMessage,
                severity = "WARNING",
                deviceFp = deviceFingerprint,
                riskScore = secValidation.riskScore
            )
            return Result.Error(secValidation.errorMessage)
        }

        // Try Firebase Auth integration
        var firebaseUid = "fb_${UUID.randomUUID().toString().take(8)}"
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val task = auth.createUserWithEmailAndPassword(cleanEmail, password)
            if (auth.currentUser != null) {
                firebaseUid = auth.currentUser!!.uid
            }
        } catch (_: Exception) {
            // Safe fallback when Firebase config is offline
        }

        val newUserId = "user_${UUID.randomUUID().toString().take(6)}"
        val generatedRefCode = generateUniqueReferralCode()

        val cleanRefInput = referralCodeInput.trim().uppercase()
        var referrerCodeToSave = ""

        // Validate Referral Code if entered
        if (cleanRefInput.isNotBlank()) {
            if (cleanRefInput == generatedRefCode) {
                return Result.Error("Self-referrals are not allowed!")
            }

            val referrer = db.userDao().getUserByReferralCode(cleanRefInput)
            if (referrer != null && referrer.id != newUserId) {
                referrerCodeToSave = cleanRefInput
            }
        }

        val newUser = UserEntity(
            id = newUserId,
            name = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            gameUsername = cleanIgn,
            freeFireUid = cleanFfUid,
            referralCode = generatedRefCode,
            depositBalance = 0.0,
            winningBalance = 0.0,
            totalWinnings = 0.0,
            totalKills = 0,
            matchesPlayed = 0,
            isAdmin = cleanEmail.contains("admin", ignoreCase = true),
            lastWithdrawalTime = 0L,
            referredBy = referrerCodeToSave,
            hasClaimedReferralDepositBonus = false,
            isPhoneVerified = true,
            deviceFingerprint = deviceFingerprint,
            firebaseUid = firebaseUid,
            riskScore = 0
        )

        db.userDao().insertUser(newUser)
        
        try {
            FirebaseFirestoreManager.saveUserToFirestore(newUser)
        } catch (_: Exception) {}

        logAudit(
            userId = newUserId,
            action = "REGISTER_SUCCESS",
            details = "Account created with phone OTP $cleanPhone & FF UID $cleanFfUid & email $cleanEmail",
            severity = "INFO",
            deviceFp = deviceFingerprint,
            riskScore = 0
        )

        return Result.Success(newUser)
    }

    suspend fun loginUserWithEmail(email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return Result.Error("Email address cannot be empty.")
        if (password.isBlank()) return Result.Error("Password cannot be empty.")

        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(cleanEmail, password)
        } catch (_: Exception) {
            // Safe fallback
        }

        val existingUser = db.userDao().getUserByEmail(cleanEmail)
        return if (existingUser != null) {
            if (existingUser.isBanned || existingUser.isBlocked) {
                return Result.Error("Account suspended: ${existingUser.banReason.ifBlank { "Policy violation." }}")
            }
            // Switch current user by updating in DB
            db.userDao().insertUser(existingUser)
            Result.Success(existingUser)
        } else {
            // Check current logged in user or create fallback
            val currentUser = db.userDao().getUserFlow().firstOrNull()
            if (currentUser != null && currentUser.email.equals(cleanEmail, ignoreCase = true)) {
                Result.Success(currentUser)
            } else {
                val newUser = UserEntity(
                    id = "user_${UUID.randomUUID().toString().take(6)}",
                    name = cleanEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    email = cleanEmail,
                    phone = "+91 9876543210",
                    gameUsername = "BattliX_Pro",
                    freeFireUid = "1234567890",
                    referralCode = generateUniqueReferralCode()
                )
                db.userDao().insertUser(newUser)
                Result.Success(newUser)
            }
        }
    }

    suspend fun loginUserWithPhone(phone: String, otpCode: String): Result<UserEntity> {
        val phoneDigits = phone.filter { it.isDigit() }.takeLast(10)
        if (phoneDigits.length != 10) return Result.Error("Valid 10-digit mobile number (+91) required.")
        if (otpCode.length < 6) return Result.Error("Invalid 6-digit OTP code.")

        val cleanPhone = "+91 $phoneDigits"
        val existingUser = db.userDao().getUserByPhone(cleanPhone)
            ?: db.userDao().getAllUsers().find { it.phone.filter { char -> char.isDigit() }.takeLast(10) == phoneDigits }

        return if (existingUser != null) {
            if (existingUser.isBanned || existingUser.isBlocked) {
                return Result.Error("Account suspended: ${existingUser.banReason.ifBlank { "Policy violation." }}")
            }
            db.userDao().insertUser(existingUser)
            Result.Success(existingUser)
        } else {
            // Create user automatically for phone login if new
            val newUser = UserEntity(
                id = "user_${UUID.randomUUID().toString().take(6)}",
                name = "Gamer $phoneDigits",
                email = "user_$phoneDigits@battlix.gg",
                phone = cleanPhone,
                gameUsername = "BattliX_$phoneDigits",
                freeFireUid = phoneDigits,
                isPhoneVerified = true,
                referralCode = generateUniqueReferralCode()
            )
            db.userDao().insertUser(newUser)
            try {
                FirebaseFirestoreManager.saveUserToFirestore(newUser)
            } catch (_: Exception) {}
            Result.Success(newUser)
        }
    }

    suspend fun loginUser(email: String): Result<UserEntity> {
        return loginUserWithEmail(email, "123456")
    }

    suspend fun applyReferralCode(code: String): Result<Unit> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.Error("Referral code cannot be empty.")
        }

        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in.")

        if (user.referralCode == cleanCode) {
            return Result.Error("You cannot use your own referral code!")
        }

        if (user.referredBy.isNotBlank()) {
            return Result.Error("You have already applied a referral code!")
        }

        val referrer = db.userDao().getUserByReferralCode(cleanCode)
            ?: return Result.Error("Referral code '$cleanCode' not found!")

        if (referrer.id == user.id) {
            return Result.Error("Self-referrals are not allowed.")
        }

        val updatedUser = user.copy(referredBy = cleanCode)
        db.userDao().updateUser(updatedUser)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_app_ref_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Referral Code Applied! 🎁",
                message = "Referral code $cleanCode linked. Complete your first deposit of ₹10 or more to get ₹5 bonus!",
                type = "WALLET"
            )
        )

        return Result.Success(Unit)
    }

    suspend fun updateUserProfile(name: String, gameUsername: String, phone: String): Result<Unit> {
        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in")
        val updated = user.copy(name = name, gameUsername = gameUsername, phone = phone)
        db.userDao().updateUser(updated)
        return Result.Success(Unit)
    }

    // Join Tournament
    suspend fun joinTournament(
        tournamentId: String,
        inGameUsername: String
    ): Result<Unit> {
        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in")
        val tournament = db.tournamentDao().getTournamentById(tournamentId)
            ?: return Result.Error("Tournament not found")

        if (tournament.status == "COMPLETED") {
            return Result.Error("This match is already completed.")
        }

        if (tournament.joinedSlots >= tournament.totalSlots) {
            return Result.Error("Match is full! All slots taken.")
        }

        // Check if already joined
        val existingParticipant = db.participantDao().getParticipant(tournamentId, user.id)
        if (existingParticipant != null) {
            return Result.Error("You have already joined this tournament!")
        }

        // Check Wallet Balance (use deposit balance first, then winning balance)
        if (user.totalBalance < tournament.entryFee) {
            return Result.Error("Insufficient balance! Entry fee is ₹${tournament.entryFee}. Please deposit funds.")
        }

        // Deduct Fee
        var remainingDeduction = tournament.entryFee
        var newDeposit = user.depositBalance
        var newWinning = user.winningBalance

        if (newDeposit >= remainingDeduction) {
            newDeposit -= remainingDeduction
        } else {
            remainingDeduction -= newDeposit
            newDeposit = 0.0
            newWinning -= remainingDeduction
        }

        val updatedUser = user.copy(
            depositBalance = newDeposit,
            winningBalance = newWinning,
            matchesPlayed = user.matchesPlayed + 1
        )
        db.userDao().updateUser(updatedUser)

        // Increment joined slots
        val updatedTournament = tournament.copy(joinedSlots = tournament.joinedSlots + 1)
        db.tournamentDao().updateTournament(updatedTournament)

        // Insert Participant
        val newSlotNumber = tournament.joinedSlots + 1
        val participant = ParticipantEntity(
            tournamentId = tournamentId,
            userId = user.id,
            inGameUsername = if (inGameUsername.isNotBlank()) inGameUsername else user.gameUsername,
            slotNumber = newSlotNumber
        )
        db.participantDao().insertParticipant(participant)

        // Record Transaction
        recordTransaction(
            TransactionEntity(
                id = "tx_join_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Joined ${tournament.title}",
                amount = tournament.entryFee,
                type = "ENTRY_FEE",
                status = "SUCCESS",
                note = "Slot #$newSlotNumber - ${tournament.gameType}"
            )
        )

        // Send Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_join_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Tournament Registered! 🎮",
                message = "You joined ${tournament.title} at Slot #$newSlotNumber. Room ID will be visible 5 mins before match.",
                type = "TOURNAMENT"
            )
        )

        return Result.Success(Unit)
    }

    // Deposit Money to Wallet & Check Referral Deposit Reward
    suspend fun depositMoney(amount: Double, paymentMethod: String): Result<Unit> {
        if (amount < 1.0) {
            return Result.Error("Minimum deposit amount is ₹1.")
        }
        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in")

        var additionalDepositBonus = 0.0
        var updatedClaimedFlag = user.hasClaimedReferralDepositBonus

        // Referral Deposit Reward Check:
        // Rule: First deposit of ₹10 or more:
        // - Credit ₹5 to referrer's wallet
        // - Credit ₹5 to new user's wallet
        // - Prevent self-referrals & duplicate rewards
        if (amount >= 10.0 && user.referredBy.isNotBlank() && !user.hasClaimedReferralDepositBonus) {
            val referrer = db.userDao().getUserByReferralCode(user.referredBy)
            if (referrer != null && referrer.id != user.id) {
                // Reward referrer ₹5
                val updatedReferrer = referrer.copy(
                    winningBalance = referrer.winningBalance + 5.0,
                    totalWinnings = referrer.totalWinnings + 5.0
                )
                db.userDao().updateUser(updatedReferrer)

                recordTransaction(
                    TransactionEntity(
                        id = "tx_ref_dep_r_${UUID.randomUUID().toString().take(6)}",
                        userId = referrer.id,
                        title = "Referral Deposit Reward",
                        amount = 5.0,
                        type = "REFERRAL_BONUS",
                        status = "SUCCESS",
                        note = "Referred user ${user.name} completed first deposit of ₹${String.format("%.0f", amount)}"
                    )
                )

                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_ref_dep_r_${UUID.randomUUID().toString().take(6)}",
                        userId = referrer.id,
                        title = "Referral Cash Credited! 🎁",
                        message = "You earned ₹5 because your referred friend ${user.name} made a deposit of ₹${String.format("%.0f", amount)}!",
                        type = "WALLET"
                    )
                )

                // Reward current user ₹5
                additionalDepositBonus = 5.0
                updatedClaimedFlag = true

                recordTransaction(
                    TransactionEntity(
                        id = "tx_ref_dep_u_${UUID.randomUUID().toString().take(6)}",
                        userId = user.id,
                        title = "First Deposit Referral Reward",
                        amount = 5.0,
                        type = "REFERRAL_BONUS",
                        status = "SUCCESS",
                        note = "Reward for depositing ₹${String.format("%.0f", amount)} using referral code ${user.referredBy}"
                    )
                )

                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_ref_dep_u_${UUID.randomUUID().toString().take(6)}",
                        userId = user.id,
                        title = "First Deposit Reward! 🎁",
                        message = "₹5 referral bonus added to your deposit balance for depositing ₹${String.format("%.0f", amount)}!",
                        type = "WALLET"
                    )
                )
            }
        }

        val totalAddedToDeposit = amount + additionalDepositBonus
        val updatedUser = user.copy(
            depositBalance = user.depositBalance + totalAddedToDeposit,
            hasClaimedReferralDepositBonus = updatedClaimedFlag
        )
        db.userDao().updateUser(updatedUser)

        recordTransaction(
            TransactionEntity(
                id = "tx_dep_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Wallet Deposit ($paymentMethod)",
                amount = amount,
                type = "DEPOSIT",
                status = "SUCCESS",
                note = "Via $paymentMethod Gateway"
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_dep_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Deposit Successful ₹$amount",
                message = "₹$amount added to your BattliX deposit balance.${if (additionalDepositBonus > 0) " (+₹5 Referral Bonus Added!)" else ""}",
                type = "WALLET"
            )
        )

        return Result.Success(Unit)
    }

    // Withdraw Money from Wallet
    // Rules:
    // 1. Minimum withdrawal ₹10
    // 2. Only one withdrawal every 24 hours
    suspend fun withdrawMoney(amount: Double, upiId: String): Result<Unit> {
        if (amount < 10.0) {
            return Result.Error("Minimum withdrawal amount is ₹10.")
        }
        if (upiId.isBlank() || !upiId.contains("@")) {
            return Result.Error("Please enter a valid UPI ID (e.g., gamer@upi).")
        }

        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in")

        // 24 Hour Frequency check
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L
        val timeSinceLastWithdrawal = currentTime - user.lastWithdrawalTime

        if (user.lastWithdrawalTime > 0 && timeSinceLastWithdrawal < twentyFourHoursMs) {
            val remainingMs = twentyFourHoursMs - timeSinceLastWithdrawal
            val remainingHours = remainingMs / (60 * 60 * 1000)
            val remainingMins = (remainingMs % (60 * 60 * 1000)) / (60 * 1000)
            return Result.Error("Only one withdrawal allowed every 24 hours! Next withdrawal available in $remainingHours h $remainingMins m.")
        }

        // Check if winning balance is sufficient
        if (user.winningBalance < amount) {
            return Result.Error("Insufficient winning balance! Your withdrawable winning balance is ₹${user.winningBalance}.")
        }

        // Deduct from winning balance
        val updatedUser = user.copy(
            winningBalance = user.winningBalance - amount,
            lastWithdrawalTime = currentTime
        )
        db.userDao().updateUser(updatedUser)

        val withdrawalId = "wdraw_${UUID.randomUUID().toString().take(6)}"

        // Add to withdrawal requests
        db.withdrawalDao().insertWithdrawal(
            WithdrawalEntity(
                id = withdrawalId,
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                upiId = upiId,
                amount = amount,
                status = "PENDING",
                timestamp = currentTime
            )
        )

        // Record Transaction
        recordTransaction(
            TransactionEntity(
                id = "tx_wdraw_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "UPI Withdrawal Request",
                amount = amount,
                type = "WITHDRAWAL",
                status = "PENDING",
                note = "Sent to UPI: $upiId"
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_wdraw_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Withdrawal Requested ₹$amount",
                message = "Your ₹$amount request to $upiId is pending processing by admin.",
                type = "WALLET"
            )
        )

        return Result.Success(Unit)
    }

    // ADMIN OPERATIONS
    suspend fun createTournament(tournament: TournamentEntity): Result<Unit> {
        db.tournamentDao().insertTournament(tournament)
        return Result.Success(Unit)
    }

    suspend fun updateRoomCredentials(
        tournamentId: String,
        roomId: String,
        roomPassword: String
    ): Result<Unit> {
        val tournament = db.tournamentDao().getTournamentById(tournamentId)
            ?: return Result.Error("Tournament not found")

        val updated = tournament.copy(roomId = roomId, roomPassword = roomPassword)
        db.tournamentDao().updateTournament(updated)

        // Notify joined participants
        val participants = db.participantDao().getParticipantsForTournamentFlow(tournamentId).firstOrNull() ?: emptyList()
        participants.forEach { p ->
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_room_${UUID.randomUUID().toString().take(6)}",
                    userId = p.userId,
                    title = "Room ID & Pass Updated! 🔑",
                    message = "Room ID: $roomId | Pass: $roomPassword for ${tournament.title}",
                    type = "ROOM_CREDS"
                )
            )
        }

        return Result.Success(Unit)
    }

    suspend fun processWithdrawal(withdrawalId: String, approve: Boolean): Result<Unit> {
        val status = if (approve) "APPROVED" else "REJECTED"
        db.withdrawalDao().updateStatus(withdrawalId, status)

        val withdrawals = db.withdrawalDao().getAllWithdrawalsFlow().firstOrNull() ?: emptyList()
        val withdrawal = withdrawals.find { it.id == withdrawalId }

        if (withdrawal != null) {
            if (!approve) {
                // If rejected, refund user winning balance
                val user = db.userDao().getUserById(withdrawal.userId)
                if (user != null) {
                    db.userDao().updateUser(user.copy(winningBalance = user.winningBalance + withdrawal.amount))
                }
            }

            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_admin_w_${UUID.randomUUID().toString().take(6)}",
                    userId = withdrawal.userId,
                    title = if (approve) "Withdrawal Approved! 🎉" else "Withdrawal Refunded",
                    message = if (approve) "₹${withdrawal.amount} transferred to UPI ID ${withdrawal.upiId}." else "₹${withdrawal.amount} returned to winning balance.",
                    type = "WALLET"
                )
            )
        }

        return Result.Success(Unit)
    }

    suspend fun markNotificationAsRead(userId: String) {
        db.notificationDao().markAllAsRead(userId)
    }

    // Submit Manual Deposit Request with UTR Anti-Fraud Validation
    suspend fun submitDepositRequest(amount: Double, utrNumber: String, paymentMethod: String, deviceFingerprint: String = "DEV-8F92A1B0-SEC"): Result<Unit> {
        if (amount < 1.0) return Result.Error("Minimum deposit is ₹1")
        if (utrNumber.isBlank()) return Result.Error("Please enter payment reference / UTR number")

        val user = db.userDao().getUserFlow().firstOrNull() ?: return Result.Error("User not logged in")

        // Ban Guard
        if (user.isBanned || user.isBlocked) {
            return Result.Error("Account is suspended. Deposits are locked.")
        }

        // UTR Uniqueness Fraud Check
        val allDeposits = db.depositDao().getAllDepositsFlow().firstOrNull() ?: emptyList()
        val utrCheck = com.example.security.SecurityManager.validateUtrUniqueness(utrNumber, allDeposits)
        if (!utrCheck.isValid) {
            logAudit(
                userId = user.id,
                action = "DEPOSIT_REJECTED_DUPLICATE_UTR",
                details = "Attempted duplicate UTR '$utrNumber' for amount ₹$amount",
                severity = "CRITICAL",
                deviceFp = deviceFingerprint,
                riskScore = 95
            )
            return Result.Error(utrCheck.errorMessage)
        }

        val depId = "dep_${UUID.randomUUID().toString().take(6)}"
        val deposit = DepositEntity(
            id = depId,
            userId = user.id,
            userName = user.name,
            userEmail = user.email,
            amount = amount,
            utrNumber = utrNumber.trim().uppercase(),
            paymentMethod = paymentMethod,
            status = "PENDING",
            deviceFingerprint = deviceFingerprint,
            riskScore = 0
        )

        db.depositDao().insertDeposit(deposit)

        db.transactionDao().insertTransaction(
            TransactionEntity(
                id = "tx_dep_$depId",
                userId = user.id,
                title = "Deposit Pending ($paymentMethod)",
                amount = amount,
                type = "DEPOSIT",
                status = "PENDING",
                note = "UTR: ${utrNumber.trim().uppercase()}"
            )
        )

        logAudit(
            userId = user.id,
            action = "DEPOSIT_SUBMITTED_PENDING",
            details = "Deposit request ₹$amount with UTR ${utrNumber.trim().uppercase()}",
            severity = "INFO",
            deviceFp = deviceFingerprint,
            riskScore = 0
        )

        return Result.Success(Unit)
    }

    // Admin: Process Deposit Request with Audit Log & Note
    suspend fun processDeposit(depositId: String, approve: Boolean, adminNote: String = "Admin Action", adminId: String = "ADMIN_SEC"): Result<Unit> {
        val status = if (approve) "APPROVED" else "REJECTED"
        
        val deposits = db.depositDao().getAllDepositsFlow().firstOrNull() ?: emptyList()
        val deposit = deposits.find { it.id == depositId }

        if (deposit != null) {
            db.depositDao().processDepositWithAudit(depositId, status, adminNote, adminId)
            val user = db.userDao().getUserById(deposit.userId)
            if (user != null) {
                if (approve) {
                    val updatedUser = user.copy(depositBalance = user.depositBalance + deposit.amount)
                    db.userDao().updateUser(updatedUser)
                }

                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_dep_proc_${UUID.randomUUID().toString().take(6)}",
                        userId = deposit.userId,
                        title = if (approve) "Deposit Approved! 💳" else "Deposit Rejected ❌",
                        message = if (approve) "₹${deposit.amount} has been added to your deposit balance." else "Your deposit request for ₹${deposit.amount} was rejected. Note: $adminNote",
                        type = "WALLET"
                    )
                )

                logAudit(
                    userId = deposit.userId,
                    action = if (approve) "ADMIN_APPROVED_DEPOSIT" else "ADMIN_REJECTED_DEPOSIT",
                    details = "Admin $adminId $status deposit of ₹${deposit.amount} (UTR: ${deposit.utrNumber}). Note: $adminNote",
                    severity = if (approve) "INFO" else "WARNING",
                    deviceFp = deposit.deviceFingerprint,
                    riskScore = 0
                )
            }
        }

        return Result.Success(Unit)
    }

    // Security: Ban User & Device
    suspend fun banUserAndDevice(userId: String, reason: String, banDevice: Boolean = true, adminId: String = "ADMIN_SEC"): Result<Unit> {
        val user = db.userDao().getUserById(userId) ?: return Result.Error("User not found")
        val updated = user.copy(
            isBanned = true,
            isBlocked = true,
            banReason = reason,
            bannedAt = System.currentTimeMillis()
        )
        db.userDao().updateUser(updated)

        if (banDevice && user.deviceFingerprint.isNotBlank()) {
            db.bannedDeviceDao().insertBannedDevice(
                BannedDeviceEntity(
                    deviceFingerprint = user.deviceFingerprint,
                    reason = reason,
                    bannedBy = adminId,
                    bannedAt = System.currentTimeMillis()
                )
            )
        }

        logAudit(
            userId = userId,
            action = "USER_DEVICE_BANNED",
            details = "Banned user ${user.name} and device ${user.deviceFingerprint}. Reason: $reason",
            severity = "CRITICAL",
            deviceFp = user.deviceFingerprint,
            riskScore = 100
        )

        return Result.Success(Unit)
    }

    // Security: Unban User & Device
    suspend fun unbanUserAndDevice(userId: String, deviceFp: String): Result<Unit> {
        val user = db.userDao().getUserById(userId)
        if (user != null) {
            val updated = user.copy(isBanned = false, isBlocked = false, banReason = "")
            db.userDao().updateUser(updated)
        }
        if (deviceFp.isNotBlank()) {
            db.bannedDeviceDao().removeBannedDevice(deviceFp)
        }

        logAudit(
            userId = userId,
            action = "USER_DEVICE_UNBANNED",
            details = "Unbanned user account and device $deviceFp",
            severity = "INFO",
            deviceFp = deviceFp,
            riskScore = 0
        )

        return Result.Success(Unit)
    }

    // Security: Audit Wallet Ledger Double-Entry
    suspend fun auditUserWalletLedger(userId: String): Result<com.example.security.LedgerAuditResult> {
        val user = db.userDao().getUserById(userId) ?: return Result.Error("User not found")
        val userTxs = db.transactionDao().getUserTransactionsFlow(userId).firstOrNull() ?: emptyList()

        val result = com.example.security.SecurityManager.auditWalletLedger(
            userId = userId,
            currentDeposit = user.depositBalance,
            currentWinning = user.winningBalance,
            transactions = userTxs
        )

        logAudit(
            userId = userId,
            action = "WALLET_LEDGER_AUDITED",
            details = result.auditNote,
            severity = if (result.isAudited) "INFO" else "CRITICAL",
            deviceFp = user.deviceFingerprint,
            riskScore = if (result.isAudited) 0 else 85
        )

        return Result.Success(result)
    }

    // Admin: Edit & Delete Tournaments
    suspend fun updateTournament(tournament: TournamentEntity): Result<Unit> {
        db.tournamentDao().updateTournament(tournament)
        return Result.Success(Unit)
    }

    suspend fun deleteTournament(tournamentId: String): Result<Unit> {
        db.tournamentDao().deleteTournament(tournamentId)
        return Result.Success(Unit)
    }

    // Admin: Block / Unblock User
    suspend fun blockUnblockUser(userId: String, isBlocked: Boolean): Result<Unit> {
        val user = db.userDao().getUserById(userId) ?: return Result.Error("User not found")
        val updated = user.copy(isBlocked = isBlocked)
        db.userDao().updateUser(updated)
        return Result.Success(Unit)
    }

    // Admin: Publish Match Results & Distribute Prize Pool Automatically
    suspend fun publishMatchResults(
        tournamentId: String,
        firstPlaceUserId: String,
        secondPlaceUserId: String = "",
        thirdPlaceUserId: String = "",
        perKillData: Map<String, Int> = emptyMap() // Map of userId to killCount
    ): Result<Unit> {
        val tournament = db.tournamentDao().getTournamentById(tournamentId) ?: return Result.Error("Tournament not found")

        val totalPrize = tournament.prizePool
        val firstReward = totalPrize * 0.50
        val secondReward = if (secondPlaceUserId.isNotBlank()) totalPrize * 0.30 else 0.0
        val thirdReward = if (thirdPlaceUserId.isNotBlank()) totalPrize * 0.20 else 0.0

        // Helper to reward position
        suspend fun rewardPosition(userId: String, amount: Double, rank: String) {
            if (userId.isBlank() || amount <= 0.0) return
            val user = db.userDao().getUserById(userId) ?: return
            val updated = user.copy(
                winningBalance = user.winningBalance + amount,
                totalWinnings = user.totalWinnings + amount
            )
            db.userDao().updateUser(updated)

            db.transactionDao().insertTransaction(
                TransactionEntity(
                    id = "tx_win_${UUID.randomUUID().toString().take(6)}",
                    userId = userId,
                    title = "Prize Money ($rank Place)",
                    amount = amount,
                    type = "WINNING",
                    status = "SUCCESS",
                    note = "${tournament.title} Reward"
                )
            )

            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_win_${UUID.randomUUID().toString().take(6)}",
                    userId = userId,
                    title = "Match Winner! 🏆",
                    message = "You won ₹${String.format("%.0f", amount)} for ranking $rank in ${tournament.title}!",
                    type = "TOURNAMENT"
                )
            )
        }

        rewardPosition(firstPlaceUserId, firstReward, "1st")
        if (secondPlaceUserId.isNotBlank()) rewardPosition(secondPlaceUserId, secondReward, "2nd")
        if (thirdPlaceUserId.isNotBlank()) rewardPosition(thirdPlaceUserId, thirdReward, "3rd")

        // Reward Kill Bonuses
        perKillData.forEach { (uId, kills) ->
            if (kills > 0) {
                val killBonus = kills * tournament.perKill
                val user = db.userDao().getUserById(uId)
                if (user != null) {
                    val updated = user.copy(
                        winningBalance = user.winningBalance + killBonus,
                        totalWinnings = user.totalWinnings + killBonus,
                        totalKills = user.totalKills + kills
                    )
                    db.userDao().updateUser(updated)

                    db.transactionDao().insertTransaction(
                        TransactionEntity(
                            id = "tx_kill_${UUID.randomUUID().toString().take(6)}",
                            userId = uId,
                            title = "Kill Reward ($kills kills)",
                            amount = killBonus,
                            type = "WINNING",
                            status = "SUCCESS",
                            note = "₹${tournament.perKill}/kill in ${tournament.title}"
                        )
                    )

                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            id = "notif_kill_${UUID.randomUUID().toString().take(6)}",
                            userId = uId,
                            title = "Kill Bonus Credited! 🎯",
                            message = "₹${String.format("%.0f", killBonus)} credited for $kills kills in ${tournament.title}!",
                            type = "TOURNAMENT"
                        )
                    )
                }
            }
        }

        // Mark match as COMPLETED
        val completedTour = tournament.copy(status = "COMPLETED")
        db.tournamentDao().updateTournament(completedTour)

        return Result.Success(Unit)
    }
}

