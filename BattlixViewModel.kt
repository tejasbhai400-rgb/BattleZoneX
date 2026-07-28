package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BattlixViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BattlixRepository = BattlixRepository(AppDatabase.getDatabase(application))

    init {
        viewModelScope.launch {
            repository.ensureUserReferralCode()
        }
    }

    val currentUser: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.tournamentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGameFilter = MutableStateFlow("ALL")
    val selectedStatusFilter = MutableStateFlow("ALL")

    val filteredTournaments: StateFlow<List<TournamentEntity>> = combine(
        allTournaments,
        selectedGameFilter,
        selectedStatusFilter
    ) { tournaments, game, status ->
        tournaments.filter { t ->
            val matchGame = (game == "ALL" || t.gameType.equals(game, ignoreCase = true))
            val matchStatus = (status == "ALL" || t.status.equals(status, ignoreCase = true))
            matchGame && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserTransactionsFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserNotificationsFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationsCountFlow(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userJoinedTournaments: StateFlow<List<ParticipantEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserJoinedTournamentsFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositEntity>> = repository.allDepositsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBannedDevices: StateFlow<List<BannedDeviceEntity>> = repository.allBannedDevicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security Status Scan
    val securityStatus: StateFlow<com.example.security.SecurityStatus> = flow {
        val context = getApplication<Application>().applicationContext
        emit(com.example.security.SecurityManager.performSecurityScan(context))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.security.SecurityStatus(false, false, false, false, 0, "SAFE"))

    val deviceFingerprint: String by lazy {
        com.example.security.SecurityManager.getDeviceFingerprint(getApplication<Application>().applicationContext)
    }

    fun getParticipantsForTournamentFlow(tournamentId: String): Flow<List<ParticipantEntity>> =
        repository.getParticipantsForTournamentFlow(tournamentId)

    // UI Feedback Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showUiMessage(message: String) {
        _uiMessage.value = message
    }

    // Actions
    fun registerUser(
        name: String,
        email: String,
        phone: String,
        freeFireUid: String,
        ign: String,
        password: String,
        refCode: String,
        isPhoneVerified: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = repository.registerUser(
                name = name,
                email = email,
                phone = phone,
                freeFireUid = freeFireUid,
                ign = ign,
                password = password,
                referralCodeInput = refCode,
                isPhoneVerified = isPhoneVerified,
                deviceFingerprint = deviceFingerprint
            )) {
                is Result.Success -> {
                    _uiMessage.value = "Account created successfully! Welcome to BattliX, ${res.data.name}! 🚀"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun loginUserWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.loginUserWithEmail(email, password)) {
                is Result.Success -> {
                    _uiMessage.value = "Welcome back, ${res.data.name}! ⚡"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun loginUserWithPhone(phone: String, otpCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.loginUserWithPhone(phone, otpCode)) {
                is Result.Success -> {
                    _uiMessage.value = "Phone OTP Verified! Welcome back, ${res.data.name}! ⚡"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun loginUser(email: String, onSuccess: () -> Unit) {
        loginUserWithEmail(email, "123456", onSuccess)
    }

    fun joinTournament(tournamentId: String, inGameUsername: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.joinTournament(tournamentId, inGameUsername)) {
                is Result.Success -> {
                    _uiMessage.value = "Successfully joined tournament! Good luck! 🎮"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun refreshTransactionsFromFirestore() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.syncFirestoreTransactions(user.id)
        }
    }

    fun depositMoney(amount: Double, paymentMethod: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.depositMoney(amount, paymentMethod)) {
                is Result.Success -> {
                    _uiMessage.value = "Deposit of ₹$amount successful!"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun withdrawMoney(amount: Double, upiId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.withdrawMoney(amount, upiId)) {
                is Result.Success -> {
                    _uiMessage.value = "Withdrawal request of ₹$amount submitted successfully!"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun createTournament(tournament: TournamentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.createTournament(tournament)) {
                is Result.Success -> {
                    _uiMessage.value = "Tournament created successfully!"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun updateRoomCredentials(tournamentId: String, roomId: String, roomPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.updateRoomCredentials(tournamentId, roomId, roomPass)) {
                is Result.Success -> {
                    _uiMessage.value = "Room credentials published to players!"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun processWithdrawal(withdrawalId: String, approve: Boolean) {
        viewModelScope.launch {
            repository.processWithdrawal(withdrawalId, approve)
            _uiMessage.value = if (approve) "Withdrawal approved!" else "Withdrawal rejected & refunded."
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.markNotificationAsRead(user.id)
            }
        }
    }

    fun updateProfile(name: String, gameUsername: String, phone: String) {
        viewModelScope.launch {
            when (val res = repository.updateUserProfile(name, gameUsername, phone)) {
                is Result.Success -> {
                    _uiMessage.value = "Profile updated successfully!"
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun applyReferralCode(code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val res = repository.applyReferralCode(code)) {
                is Result.Success -> {
                    _uiMessage.value = "Referral code applied successfully! 🎁"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    fun retryLoadUser() {
        viewModelScope.launch {
            repository.ensureUserReferralCode()
        }
    }

    // Manual Deposit Request
    fun submitDepositRequest(amount: Double, utrNumber: String, paymentMethod: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.submitDepositRequest(amount, utrNumber, paymentMethod, deviceFingerprint)) {
                is Result.Success -> {
                    _uiMessage.value = "Deposit request submitted! Admin will verify UTR."
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    // Admin: Process Deposit with Note
    fun processDeposit(depositId: String, approve: Boolean, note: String = "Verified UTR") {
        viewModelScope.launch {
            repository.processDeposit(depositId, approve, note, "ADMIN_SEC")
            _uiMessage.value = if (approve) "Deposit approved & wallet credited!" else "Deposit request rejected."
        }
    }

    // Security: Ban User & Device
    fun banUserAndDevice(userId: String, reason: String, banDevice: Boolean = true) {
        viewModelScope.launch {
            when (val res = repository.banUserAndDevice(userId, reason, banDevice)) {
                is Result.Success -> {
                    _uiMessage.value = "User and device suspended successfully."
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    // Security: Unban User & Device
    fun unbanUserAndDevice(userId: String, deviceFp: String) {
        viewModelScope.launch {
            when (val res = repository.unbanUserAndDevice(userId, deviceFp)) {
                is Result.Success -> {
                    _uiMessage.value = "Account and device restriction removed."
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    // Security: Audit Wallet Ledger Double-Entry
    fun auditUserWalletLedger(userId: String, onResult: (com.example.security.LedgerAuditResult) -> Unit = {}) {
        viewModelScope.launch {
            when (val res = repository.auditUserWalletLedger(userId)) {
                is Result.Success -> {
                    _uiMessage.value = res.data.auditNote
                    onResult(res.data)
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }

    // Admin: Edit & Delete Tournament
    fun updateTournament(tournament: TournamentEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTournament(tournament)
            _uiMessage.value = "Tournament updated successfully!"
            onSuccess()
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            repository.deleteTournament(tournamentId)
            _uiMessage.value = "Tournament deleted!"
        }
    }

    // Admin: Block / Unblock User
    fun blockUnblockUser(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.blockUnblockUser(userId, isBlocked)
            _uiMessage.value = if (isBlocked) "User account blocked!" else "User account unblocked!"
        }
    }

    // Admin: Publish Match Results & Distribute Cash Rewards
    fun publishMatchResults(
        tournamentId: String,
        firstPlaceUserId: String,
        secondPlaceUserId: String = "",
        thirdPlaceUserId: String = "",
        perKillData: Map<String, Int> = emptyMap(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val res = repository.publishMatchResults(tournamentId, firstPlaceUserId, secondPlaceUserId, thirdPlaceUserId, perKillData)) {
                is Result.Success -> {
                    _uiMessage.value = "Results published & prize money distributed to wallets! 🏆"
                    onSuccess()
                }
                is Result.Error -> {
                    _uiMessage.value = res.message
                }
            }
        }
    }
}
