package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TournamentEntity::class,
        ParticipantEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        WithdrawalEntity::class,
        DepositEntity::class,
        AuditLogEntity::class,
        BannedDeviceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun participantDao(): ParticipantDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun depositDao(): DepositDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun bannedDeviceDao(): BannedDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "battlix_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedDatabase(database)
                }
            }
        }

        suspend fun seedDatabase(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val tenMinsFromNow = now + (10 * 60 * 1000) // Unlocks room credentials now!
            val twoHoursFromNow = now + (2 * 60 * 60 * 1000)
            val tomorrow = now + (24 * 60 * 60 * 1000)
            val yesterday = now - (24 * 60 * 60 * 1000)

            // Seed Default User
            val defaultUser = UserEntity(
                id = "user_101",
                name = "Pro Gamer",
                email = "gamer@battlix.gg",
                phone = "+91 9876543210",
                gameUsername = "BattliX_Slayer",
                referralCode = "BTLX7A9K",
                depositBalance = 150.0,
                winningBalance = 240.0,
                totalWinnings = 850.0,
                totalKills = 42,
                matchesPlayed = 18,
                isAdmin = true,
                lastWithdrawalTime = 0L,
                referredBy = "",
                hasClaimedReferralDepositBonus = false
            )
            db.userDao().insertUser(defaultUser)

            // Seed Initial Tournaments
            val tournaments = listOf(
                TournamentEntity(
                    id = "tour_101",
                    title = "BGMI Championship Solo #101",
                    gameType = "BGMI",
                    map = "Erangel",
                    matchType = "Solo",
                    entryFee = 20.0,
                    prizePool = 1000.0,
                    perKill = 15.0,
                    startTime = tenMinsFromNow,
                    totalSlots = 100,
                    joinedSlots = 87,
                    status = "UPCOMING",
                    roomId = "8492041",
                    roomPassword = "7721",
                    bannerImageResName = "img_bgmi_card_1785044714566",
                    rules = "1. Erangel map TPP mode.\n2. No team-up allowed in solo matches.\n3. Room ID & Password are provided above!"
                ),
                TournamentEntity(
                    id = "tour_102",
                    title = "Free Fire Clash Squad #205",
                    gameType = "Free Fire",
                    map = "Bermuda",
                    matchType = "Squad",
                    entryFee = 50.0,
                    prizePool = 2500.0,
                    perKill = 30.0,
                    startTime = twoHoursFromNow,
                    totalSlots = 48,
                    joinedSlots = 32,
                    status = "UPCOMING",
                    roomId = "5510932",
                    roomPassword = "ff88",
                    bannerImageResName = "img_freefire_card_1785044725320",
                    rules = "1. Classic Bermuda map.\n2. Character skills are allowed.\n3. Room ID unlocks 15 mins before match."
                ),
                TournamentEntity(
                    id = "tour_103",
                    title = "Call of Duty Mobile Battle Royale",
                    gameType = "Call of Duty",
                    map = "Isolated",
                    matchType = "Duo",
                    entryFee = 30.0,
                    prizePool = 1500.0,
                    perKill = 20.0,
                    startTime = tomorrow,
                    totalSlots = 50,
                    joinedSlots = 12,
                    status = "UPCOMING",
                    roomId = "",
                    roomPassword = "",
                    bannerImageResName = "img_battlix_banner_1785044702370",
                    rules = "1. FPP Mode on Isolated.\n2. Both duo partners must register separately or together."
                ),
                TournamentEntity(
                    id = "tour_104",
                    title = "Valorant 5v5 Spike Rush Showdown",
                    gameType = "Valorant",
                    map = "Ascent",
                    matchType = "Squad",
                    entryFee = 100.0,
                    prizePool = 5000.0,
                    perKill = 50.0,
                    startTime = yesterday,
                    totalSlots = 16,
                    joinedSlots = 16,
                    status = "COMPLETED",
                    roomId = "VAL-99120",
                    roomPassword = "pass",
                    bannerImageResName = "img_battlix_banner_1785044702370",
                    rules = "Match completed. Winner: Team Alpha (Prize ₹3000)."
                )
            )
            db.tournamentDao().insertTournaments(tournaments)

            // Auto-join user to match tour_101
            db.participantDao().insertParticipant(
                ParticipantEntity(
                    tournamentId = "tour_101",
                    userId = "user_101",
                    inGameUsername = "BattliX_Slayer",
                    slotNumber = 14
                )
            )

            // Initial Transactions
            val initialTransactions = listOf(
                TransactionEntity(
                    id = "tx_1",
                    userId = "user_101",
                    title = "UPI Wallet Deposit",
                    amount = 150.0,
                    type = "DEPOSIT",
                    status = "SUCCESS",
                    timestamp = now - (2 * 24 * 60 * 60 * 1000),
                    note = "Ref: UPI/77192039120"
                ),
                TransactionEntity(
                    id = "tx_2",
                    userId = "user_101",
                    title = "Joined BGMI Solo #101",
                    amount = 20.0,
                    type = "ENTRY_FEE",
                    status = "SUCCESS",
                    timestamp = now - (3 * 60 * 60 * 1000),
                    note = "Tournament Entry Fee"
                ),
                TransactionEntity(
                    id = "tx_3",
                    userId = "user_101",
                    title = "Welcome Referral Bonus",
                    amount = 10.0,
                    type = "REFERRAL_BONUS",
                    status = "SUCCESS",
                    timestamp = now - (5 * 24 * 60 * 60 * 1000),
                    note = "Signup with code BATTLIX-WELCOME"
                )
            )
            initialTransactions.forEach { db.transactionDao().insertTransaction(it) }

            // Initial Notifications
            val initialNotifications = listOf(
                NotificationEntity(
                    id = "notif_1",
                    userId = "user_101",
                    title = "Room Credentials Unlocked! 🎮",
                    message = "Room ID: 8492041 | Pass: 7721 for BGMI Championship Solo #101. Join now!",
                    timestamp = now - (5 * 60 * 1000),
                    isRead = false,
                    type = "ROOM_CREDS"
                ),
                NotificationEntity(
                    id = "notif_2",
                    userId = "user_101",
                    title = "Deposit Successful ₹150",
                    message = "Added ₹150 to your BattliX deposit balance.",
                    timestamp = now - (2 * 24 * 60 * 60 * 1000),
                    isRead = true,
                    type = "WALLET"
                )
            )
            initialNotifications.forEach { db.notificationDao().insertNotification(it) }
        }
    }
}
