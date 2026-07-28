package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE gameUsername = :gameUser LIMIT 1")
    suspend fun getUserByGameUsername(gameUser: String): UserEntity?

    @Query("SELECT * FROM users WHERE freeFireUid = :ffUid LIMIT 1")
    suspend fun getUserByFreeFireUid(ffUid: String): UserEntity?

    @Query("SELECT * FROM users WHERE deviceFingerprint = :fp")
    suspend fun getUsersByDeviceFingerprint(fp: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY startTime ASC")
    fun getAllTournamentsFlow(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentByIdFlow(id: String): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentById(id: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournament(id: String)
}

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE tournamentId = :tournamentId")
    fun getParticipantsForTournamentFlow(tournamentId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE userId = :userId")
    fun getParticipantsForUserFlow(userId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE tournamentId = :tournamentId AND userId = :userId")
    suspend fun getParticipant(tournamentId: String, userId: String): ParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ParticipantEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserTransactionsFlow(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserNotificationsFlow(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCountFlow(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserWithdrawalsFlow(userId: String): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY timestamp DESC")
    fun getAllDepositsFlow(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserDepositsFlow(userId: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE utrNumber = :utr LIMIT 1")
    suspend fun getDepositByUtr(utr: String): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity)

    @Query("UPDATE deposits SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE deposits SET status = :status, adminNote = :note, processedBy = :adminId WHERE id = :id")
    suspend fun processDepositWithAudit(id: String, status: String, note: String, adminId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAuditLogsForUserFlow(userId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface BannedDeviceDao {
    @Query("SELECT * FROM banned_devices WHERE deviceFingerprint = :fp LIMIT 1")
    suspend fun getBannedDevice(fp: String): BannedDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBannedDevice(device: BannedDeviceEntity)

    @Query("DELETE FROM banned_devices WHERE deviceFingerprint = :fp")
    suspend fun removeBannedDevice(fp: String)

    @Query("SELECT * FROM banned_devices")
    fun getAllBannedDevicesFlow(): Flow<List<BannedDeviceEntity>>
}
