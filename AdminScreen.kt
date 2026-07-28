package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DepositEntity
import com.example.data.TournamentEntity
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: BattlixViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val tournaments by viewModel.allTournaments.collectAsState()
    val withdrawals by viewModel.allWithdrawals.collectAsState()
    val deposits by viewModel.allDeposits.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    var isAdminAuthenticated by remember { mutableStateOf(currentUser?.isAdmin == true) }
    var adminPasscodeInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf(false) }

    // Admin Tabs: 0=Dashboard, 1=Matches, 2=Finances, 3=Transactions, 4=Users
    var selectedAdminTab by remember { mutableStateOf(0) }

    // Create / Edit Tournament Dialogs
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTournament by remember { mutableStateOf<TournamentEntity?>(null) }

    // Tournament Form Inputs
    var titleInput by remember { mutableStateOf("") }
    var gameTypeInput by remember { mutableStateOf("BGMI") }
    var mapInput by remember { mutableStateOf("Erangel") }
    var matchTypeInput by remember { mutableStateOf("Solo") }
    var entryFeeInput by remember { mutableStateOf("20") }
    var prizePoolInput by remember { mutableStateOf("1000") }
    var perKillInput by remember { mutableStateOf("15") }
    var slotsInput by remember { mutableStateOf("100") }

    // Room Credentials Dialog
    var showRoomDialog by remember { mutableStateOf(false) }
    var targetTournamentId by remember { mutableStateOf("") }
    var roomIdInput by remember { mutableStateOf("") }
    var roomPassInput by remember { mutableStateOf("") }

    // Match Results Dialog
    var showResultsDialog by remember { mutableStateOf(false) }
    var selectedMatchForResults by remember { mutableStateOf<TournamentEntity?>(null) }
    var winner1stInput by remember { mutableStateOf("") }
    var winner2ndInput by remember { mutableStateOf("") }
    var winner3rdInput by remember { mutableStateOf("") }

    // User Search State
    var userSearchQuery by remember { mutableStateOf("") }

    // Financial Tab Sub-toggle (0=Deposits, 1=Withdrawals)
    var financeSubTab by remember { mutableStateOf(0) }

    // ADMIN LOGIN GUARD
    if (!isAdminAuthenticated && currentUser?.isAdmin != true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EsportsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                borderColor = RedPrimary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = RedPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ADMIN AUTHENTICATION",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter BattliX Admin Passcode to access control panel.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = adminPasscodeInput,
                        onValueChange = {
                            adminPasscodeInput = it
                            authError = false
                        },
                        label = { Text("Admin Passcode") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = authError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedLabelColor = RedPrimary
                        ),
                        singleLine = true
                    )

                    if (authError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Invalid passcode! Default is 'admin123' or 'BATTLEX_ADMIN_2026'",
                            fontSize = 11.sp,
                            color = RedNeon
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    EsportsButton(
                        text = "UNLOCK ADMIN PANEL 🔑",
                        onClick = {
                            if (adminPasscodeInput == "admin123" || adminPasscodeInput == "BATTLEX_ADMIN_2026" || adminPasscodeInput == "admin") {
                                isAdminAuthenticated = true
                            } else {
                                authError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (onBackClick != null) {
                        TextButton(onClick = onBackClick) {
                            Text("RETURN TO HOME", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        return
    }

    // ADMIN MAIN CONTENT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlackSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = RedNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ADMIN CONTROL CENTER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "BattliX Management System",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = {
                titleInput = ""
                gameTypeInput = "BGMI"
                mapInput = "Erangel"
                matchTypeInput = "Solo"
                entryFeeInput = "20"
                prizePoolInput = "1000"
                perKillInput = "15"
                slotsInput = "100"
                showCreateDialog = true
            }) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Create Match", tint = RedNeon, modifier = Modifier.size(28.dp))
            }
        }

        // TABS
        ScrollableTabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = BlackSurface,
            contentColor = RedPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (selectedAdminTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                        color = RedPrimary,
                        height = 3.dp
                    )
                }
            }
        ) {
            Tab(selected = selectedAdminTab == 0, onClick = { selectedAdminTab = 0 }) {
                Text("DASHBOARD", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp), color = if (selectedAdminTab == 0) RedPrimary else TextMuted)
            }
            Tab(selected = selectedAdminTab == 1, onClick = { selectedAdminTab = 1 }) {
                Text("MATCHES (${tournaments.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp), color = if (selectedAdminTab == 1) RedPrimary else TextMuted)
            }
            Tab(selected = selectedAdminTab == 2, onClick = { selectedAdminTab = 2 }) {
                val pendingDep = deposits.count { it.status == "PENDING" }
                val pendingWdraw = withdrawals.count { it.status == "PENDING" }
                val totalPending = pendingDep + pendingWdraw
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Text("FINANCES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedAdminTab == 2) RedPrimary else TextMuted)
                    if (totalPending > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeTag(text = totalPending.toString(), containerColor = RedNeon, textColor = Color.White)
                    }
                }
            }
            Tab(selected = selectedAdminTab == 3, onClick = { selectedAdminTab = 3 }) {
                Text("TRANSACTIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp), color = if (selectedAdminTab == 3) RedPrimary else TextMuted)
            }
            Tab(selected = selectedAdminTab == 4, onClick = { selectedAdminTab = 4 }) {
                Text("USERS (${users.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp), color = if (selectedAdminTab == 4) RedPrimary else TextMuted)
            }
            Tab(selected = selectedAdminTab == 5, onClick = { selectedAdminTab = 5 }) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = if (selectedAdminTab == 5) RedPrimary else TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SECURITY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedAdminTab == 5) RedPrimary else TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedAdminTab) {
                0 -> AdminDashboardView(users, tournaments, deposits, withdrawals)
                1 -> AdminMatchesView(
                    tournaments = tournaments,
                    onEditMatch = { tournament ->
                        editingTournament = tournament
                        titleInput = tournament.title
                        gameTypeInput = tournament.gameType
                        mapInput = tournament.map
                        matchTypeInput = tournament.matchType
                        entryFeeInput = tournament.entryFee.toString()
                        prizePoolInput = tournament.prizePool.toString()
                        perKillInput = tournament.perKill.toString()
                        slotsInput = tournament.totalSlots.toString()
                        showEditDialog = true
                    },
                    onSetRoom = { tournament ->
                        targetTournamentId = tournament.id
                        roomIdInput = tournament.roomId
                        roomPassInput = tournament.roomPassword
                        showRoomDialog = true
                    },
                    onPublishResults = { tournament ->
                        selectedMatchForResults = tournament
                        winner1stInput = ""
                        winner2ndInput = ""
                        winner3rdInput = ""
                        showResultsDialog = true
                    },
                    onDeleteMatch = { tournamentId ->
                        viewModel.deleteTournament(tournamentId)
                    }
                )
                2 -> AdminFinancesView(
                    subTab = financeSubTab,
                    onSubTabChange = { financeSubTab = it },
                    deposits = deposits,
                    withdrawals = withdrawals,
                    onProcessDeposit = { id, approve -> viewModel.processDeposit(id, approve) },
                    onProcessWithdrawal = { id, approve -> viewModel.processWithdrawal(id, approve) }
                )
                3 -> AdminTransactionsView(transactions)
                4 -> AdminUsersView(
                    users = users,
                    searchQuery = userSearchQuery,
                    onSearchChange = { userSearchQuery = it },
                    onBlockUnblock = { userId, block -> viewModel.blockUnblockUser(userId, block) }
                )
                5 -> AdminSecurityCenterView(viewModel)
            }
        }
    }

    // CREATE TOURNAMENT DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = BlackCard,
            title = { Text("CREATE NEW MATCH", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Tournament Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = gameTypeInput, onValueChange = { gameTypeInput = it }, label = { Text("Game (BGMI / Free Fire)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = mapInput, onValueChange = { mapInput = it }, label = { Text("Map Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = matchTypeInput, onValueChange = { matchTypeInput = it }, label = { Text("Match Type (Solo / Squad)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = entryFeeInput, onValueChange = { entryFeeInput = it }, label = { Text("Entry Fee (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prizePoolInput, onValueChange = { prizePoolInput = it }, label = { Text("Prize Pool (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = perKillInput, onValueChange = { perKillInput = it }, label = { Text("Per Kill Bonus (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = slotsInput, onValueChange = { slotsInput = it }, label = { Text("Total Slots") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                EsportsButton(
                    text = "PUBLISH MATCH 🎮",
                    onClick = {
                        val newTour = TournamentEntity(
                            id = "tour_${UUID.randomUUID().toString().take(6)}",
                            title = if (titleInput.isBlank()) "BattliX Showdown" else titleInput,
                            gameType = gameTypeInput,
                            map = mapInput,
                            matchType = matchTypeInput,
                            entryFee = entryFeeInput.toDoubleOrNull() ?: 20.0,
                            prizePool = prizePoolInput.toDoubleOrNull() ?: 1000.0,
                            perKill = perKillInput.toDoubleOrNull() ?: 15.0,
                            startTime = System.currentTimeMillis() + (30 * 60 * 1000),
                            totalSlots = slotsInput.toIntOrNull() ?: 100,
                            joinedSlots = 0,
                            status = "UPCOMING"
                        )
                        viewModel.createTournament(newTour) { showCreateDialog = false }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("CANCEL", color = TextMuted) }
            }
        )
    }

    // EDIT TOURNAMENT DIALOG
    if (showEditDialog && editingTournament != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = BlackCard,
            title = { Text("EDIT TOURNAMENT", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Tournament Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = entryFeeInput, onValueChange = { entryFeeInput = it }, label = { Text("Entry Fee (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prizePoolInput, onValueChange = { prizePoolInput = it }, label = { Text("Prize Pool (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = perKillInput, onValueChange = { perKillInput = it }, label = { Text("Per Kill Bonus (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                EsportsButton(
                    text = "SAVE CHANGES 💾",
                    onClick = {
                        val updated = editingTournament!!.copy(
                            title = titleInput,
                            gameType = gameTypeInput,
                            map = mapInput,
                            matchType = matchTypeInput,
                            entryFee = entryFeeInput.toDoubleOrNull() ?: editingTournament!!.entryFee,
                            prizePool = prizePoolInput.toDoubleOrNull() ?: editingTournament!!.prizePool,
                            perKill = perKillInput.toDoubleOrNull() ?: editingTournament!!.perKill,
                            totalSlots = slotsInput.toIntOrNull() ?: editingTournament!!.totalSlots
                        )
                        viewModel.updateTournament(updated) { showEditDialog = false }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("CANCEL", color = TextMuted) }
            }
        )
    }

    // PUBLISH ROOM ID DIALOG
    if (showRoomDialog) {
        AlertDialog(
            onDismissRequest = { showRoomDialog = false },
            containerColor = BlackCard,
            title = { Text("PUBLISH ROOM CREDS", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    Text(text = "Credentials will auto-release to joined players 5 mins before start time.", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = roomIdInput,
                        onValueChange = { roomIdInput = it },
                        label = { Text("Room ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = roomPassInput,
                        onValueChange = { roomPassInput = it },
                        label = { Text("Room Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                EsportsButton(
                    text = "PUBLISH CREDENTIALS 🔑",
                    onClick = {
                        viewModel.updateRoomCredentials(targetTournamentId, roomIdInput, roomPassInput) {
                            showRoomDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRoomDialog = false }) { Text("CANCEL", color = TextMuted) }
            }
        )
    }

    // MATCH RESULTS & AUTO PRIZE DISTRIBUTION DIALOG
    if (showResultsDialog && selectedMatchForResults != null) {
        AlertDialog(
            onDismissRequest = { showResultsDialog = false },
            containerColor = BlackCard,
            title = { Text("DECLARE MATCH RESULTS 🏆", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Match: ${selectedMatchForResults!!.title}\nPrize Pool: ₹${selectedMatchForResults!!.prizePool}",
                        fontSize = 12.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("1st Place Winner (50% Prize):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = winner1stInput,
                        onValueChange = { winner1stInput = it },
                        placeholder = { Text("Enter Winner User ID or Game Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("2nd Place Winner (30% Prize):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = winner2ndInput,
                        onValueChange = { winner2ndInput = it },
                        placeholder = { Text("Optional 2nd Place User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("3rd Place Winner (20% Prize):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = winner3rdInput,
                        onValueChange = { winner3rdInput = it },
                        placeholder = { Text("Optional 3rd Place User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                EsportsButton(
                    text = "DISTRIBUTE PRIZE MONEY 💰",
                    onClick = {
                        val firstUser = users.find { it.id == winner1stInput || it.name.equals(winner1stInput, true) || it.gameUsername.equals(winner1stInput, true) }?.id ?: users.firstOrNull()?.id ?: ""
                        val secondUser = users.find { it.id == winner2ndInput || it.name.equals(winner2ndInput, true) || it.gameUsername.equals(winner2ndInput, true) }?.id ?: ""
                        val thirdUser = users.find { it.id == winner3rdInput || it.name.equals(winner3rdInput, true) || it.gameUsername.equals(winner3rdInput, true) }?.id ?: ""

                        viewModel.publishMatchResults(
                            tournamentId = selectedMatchForResults!!.id,
                            firstPlaceUserId = firstUser,
                            secondPlaceUserId = secondUser,
                            thirdPlaceUserId = thirdUser,
                            onSuccess = { showResultsDialog = false }
                        )
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showResultsDialog = false }) { Text("CANCEL", color = TextMuted) }
            }
        )
    }
}

// DASHBOARD TAB
@Composable
fun AdminDashboardView(
    users: List<UserEntity>,
    tournaments: List<TournamentEntity>,
    deposits: List<DepositEntity>,
    withdrawals: List<WithdrawalEntity>
) {
    val totalUserBalance = users.sumOf { it.totalBalance }
    val totalDepositsApproved = deposits.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalWithdrawalsApproved = withdrawals.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val pendingDepositsCount = deposits.count { it.status == "PENDING" }
    val pendingWithdrawalsCount = withdrawals.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("OVERVIEW ANALYTICS", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextSecondary, letterSpacing = 1.sp)
        }

        // METRICS GRID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("TOTAL USERS", "${users.size}", Icons.Default.Group, GoldAccent, modifier = Modifier.weight(1f))
                    MetricCard("USER BALANCES", "₹${String.format("%.0f", totalUserBalance)}", Icons.Default.AccountBalanceWallet, GreenAccent, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("APPROVED DEPOSITS", "₹${String.format("%.0f", totalDepositsApproved)}", Icons.Default.ArrowDownward, GreenAccent, modifier = Modifier.weight(1f))
                    MetricCard("APPROVED WITHDRAWS", "₹${String.format("%.0f", totalWithdrawalsApproved)}", Icons.Default.ArrowUpward, RedNeon, modifier = Modifier.weight(1f))
                }
            }
        }

        // PENDING ACTIONS CARD
        if (pendingDepositsCount > 0 || pendingWithdrawalsCount > 0) {
            item {
                EsportsCard(borderColor = RedNeon) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTION REQUIRED 🚨", fontWeight = FontWeight.Bold, color = RedNeon, fontSize = 14.sp)
                            Text("$pendingDepositsCount pending deposit(s) & $pendingWithdrawalsCount pending withdrawal(s)", fontSize = 12.sp, color = TextMuted)
                        }
                        BadgeTag(text = "PENDING", containerColor = RedContainer, textColor = RedNeon)
                    }
                }
            }
        }

        // TOURNAMENTS STATS
        item {
            EsportsCard {
                Column {
                    Text("TOURNAMENTS SUMMARY 🎮", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Matches", fontSize = 11.sp, color = TextMuted)
                            Text("${tournaments.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Upcoming", fontSize = 11.sp, color = TextMuted)
                            Text("${tournaments.count { it.status == "UPCOMING" }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Column {
                            Text("Completed", fontSize = 11.sp, color = TextMuted)
                            Text("${tournaments.count { it.status == "COMPLETED" }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    EsportsCard(modifier = modifier, borderColor = color.copy(alpha = 0.4f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// MATCHES MANAGEMENT TAB
@Composable
fun AdminMatchesView(
    tournaments: List<TournamentEntity>,
    onEditMatch: (TournamentEntity) -> Unit,
    onSetRoom: (TournamentEntity) -> Unit,
    onPublishResults: (TournamentEntity) -> Unit,
    onDeleteMatch: (String) -> Unit
) {
    if (tournaments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No tournaments found. Click + to create one.", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tournaments) { tournament ->
                EsportsCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (tournament.roomId.isNotEmpty()) GreenAccent.copy(alpha = 0.5f) else BorderDark
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tournament.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    BadgeTag(text = tournament.gameType, containerColor = RedContainer, textColor = RedNeon)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    BadgeTag(text = tournament.status, containerColor = BlackSurfaceVariant, textColor = if (tournament.status == "COMPLETED") GreenAccent else GoldAccent)
                                }
                            }

                            Row {
                                IconButton(onClick = { onEditMatch(tournament) }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                                }
                                IconButton(onClick = { onDeleteMatch(tournament.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedNeon)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tournament.roomId.isNotEmpty()) "Room ID: ${tournament.roomId} | Pass: ${tournament.roomPassword}" else "Room ID: Not Set Yet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tournament.roomId.isNotEmpty()) GreenAccent else TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EsportsButton(
                                text = "SET ROOM ID 🔑",
                                onClick = { onSetRoom(tournament) },
                                modifier = Modifier.weight(1f),
                                containerColor = BlackSurfaceVariant,
                                contentColor = RedNeon
                            )

                            if (tournament.status != "COMPLETED") {
                                EsportsButton(
                                    text = "RESULTS 🏆",
                                    onClick = { onPublishResults(tournament) },
                                    modifier = Modifier.weight(1f),
                                    containerColor = RedPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// FINANCES TAB (DEPOSITS & WITHDRAWALS)
@Composable
fun AdminFinancesView(
    subTab: Int,
    onSubTabChange: (Int) -> Unit,
    deposits: List<DepositEntity>,
    withdrawals: List<WithdrawalEntity>,
    onProcessDeposit: (String, Boolean) -> Unit,
    onProcessWithdrawal: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab, containerColor = BlackSurface, contentColor = RedPrimary) {
            Tab(selected = subTab == 0, onClick = { onSubTabChange(0) }) {
                Text("DEPOSITS (${deposits.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (subTab == 0) RedPrimary else TextMuted)
            }
            Tab(selected = subTab == 1, onClick = { onSubTabChange(1) }) {
                Text("WITHDRAWALS (${withdrawals.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (subTab == 1) RedPrimary else TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (subTab == 0) {
                // DEPOSITS
                if (deposits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No deposit requests found.", color = TextMuted)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(deposits) { dep ->
                            EsportsCard(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(dep.userName, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("UTR: ${dep.utrNumber}", fontSize = 12.sp, color = TextMuted)
                                        Text("Amount: ₹${String.format("%.0f", dep.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                                    }

                                    if (dep.status == "PENDING") {
                                        Row {
                                            IconButton(onClick = { onProcessDeposit(dep.id, true) }) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = GreenAccent)
                                            }
                                            IconButton(onClick = { onProcessDeposit(dep.id, false) }) {
                                                Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = RedNeon)
                                            }
                                        }
                                    } else {
                                        BadgeTag(
                                            text = dep.status,
                                            containerColor = if (dep.status == "APPROVED") GreenAccent.copy(alpha = 0.2f) else RedContainer,
                                            textColor = if (dep.status == "APPROVED") GreenAccent else RedNeon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // WITHDRAWALS
                if (withdrawals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No withdrawal requests found.", color = TextMuted)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(withdrawals) { wdraw ->
                            EsportsCard(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(wdraw.userName, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("UPI: ${wdraw.upiId}", fontSize = 12.sp, color = GreenAccent)
                                        Text("Amount: ₹${String.format("%.0f", wdraw.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                    }

                                    if (wdraw.status == "PENDING") {
                                        Row {
                                            IconButton(onClick = { onProcessWithdrawal(wdraw.id, true) }) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = GreenAccent)
                                            }
                                            IconButton(onClick = { onProcessWithdrawal(wdraw.id, false) }) {
                                                Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = RedNeon)
                                            }
                                        }
                                    } else {
                                        BadgeTag(
                                            text = wdraw.status,
                                            containerColor = if (wdraw.status == "APPROVED") GreenAccent.copy(alpha = 0.2f) else RedContainer,
                                            textColor = if (wdraw.status == "APPROVED") GreenAccent else RedNeon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TRANSACTIONS TAB
@Composable
fun AdminTransactionsView(transactions: List<com.example.data.TransactionEntity>) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No transactions recorded.", color = TextMuted)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(transactions) { tx ->
                EsportsCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(tx.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Note: ${tx.note.ifBlank { "N/A" }}", fontSize = 11.sp, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val isPlus = tx.type == "DEPOSIT" || tx.type == "WINNING" || tx.type == "REFERRAL_BONUS"
                            Text(
                                text = "${if (isPlus) "+" else "-"}₹${String.format("%.0f", tx.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = if (isPlus) GreenAccent else RedNeon,
                                fontSize = 15.sp
                            )
                            BadgeTag(text = tx.type, containerColor = BlackSurfaceVariant, textColor = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// USERS MANAGEMENT TAB
@Composable
fun AdminUsersView(
    users: List<UserEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBlockUnblock: (String, Boolean) -> Unit
) {
    val filteredUsers = users.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true) ||
        it.gameUsername.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search users by name, email, or game ID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RedPrimary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = BorderDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No users found matching query.", color = TextMuted)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredUsers) { user ->
                        EsportsCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (user.isBlocked) RedNeon.copy(alpha = 0.8f) else BorderDark
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        if (user.isBlocked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            BadgeTag(text = "BLOCKED", containerColor = RedContainer, textColor = RedNeon)
                                        }
                                    }
                                    Text("IGN: ${user.gameUsername} | Email: ${user.email}", fontSize = 11.sp, color = TextMuted)
                                    Text("Balance: ₹${String.format("%.0f", user.totalBalance)} | Kills: ${user.totalKills}", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onBlockUnblock(user.id, !user.isBlocked) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.isBlocked) GreenAccent else RedNeon,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (user.isBlocked) "UNBLOCK" else "BLOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSecurityCenterView(viewModel: BattlixViewModel) {
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val bannedDevices by viewModel.allBannedDevices.collectAsState()
    val securityStatus by viewModel.securityStatus.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    var banUserIdInput by remember { mutableStateOf("") }
    var banReasonInput by remember { mutableStateOf("Policy violation / Fraud attempt") }
    var auditUserIdInput by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECURITY MONITOR OVERVIEW CARD
        item {
            EsportsCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (securityStatus.riskLevel == "SAFE") BorderDark else RedNeon
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = RedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ENTERPRISE FRAUD GUARD", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                        }
                        BadgeTag(
                            text = securityStatus.riskLevel,
                            containerColor = if (securityStatus.riskLevel == "SAFE") GreenSuccess.copy(0.2f) else RedNeon.copy(0.2f),
                            textColor = if (securityStatus.riskLevel == "SAFE") GreenSuccess else RedNeon
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Root Status: ${if (securityStatus.isRooted) "ROOTED ⚠️" else "PASS ✅"}", fontSize = 12.sp, color = if (securityStatus.isRooted) RedNeon else TextMuted)
                            Text("Emulator: ${if (securityStatus.isEmulator) "DETECTED ⚠️" else "PASS ✅"}", fontSize = 12.sp, color = if (securityStatus.isEmulator) GoldAccent else TextMuted)
                        }
                        Column {
                            Text("Debugger: ${if (securityStatus.isDebuggerAttached) "ATTACHED ⚠️" else "PASS ✅"}", fontSize = 12.sp, color = TextMuted)
                            Text("App Integrity: ${if (securityStatus.isTampered) "TAMPERED ❌" else "PASS ✅"}", fontSize = 12.sp, color = if (securityStatus.isTampered) RedNeon else GreenSuccess)
                        }
                    }
                }
            }
        }

        // WALLET DOUBLE-ENTRY LEDGER AUDITOR
        item {
            EsportsCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("DOUBLE-ENTRY LEDGER AUDITOR ⚖️", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Validate user wallet balance against full transaction history to detect balance tampering.", fontSize = 11.sp, color = TextMuted)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = auditUserIdInput,
                            onValueChange = { auditUserIdInput = it },
                            placeholder = { Text("User ID (e.g. user_101)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        Button(
                            onClick = {
                                val target = auditUserIdInput.ifBlank { users.firstOrNull()?.id ?: "" }
                                if (target.isNotBlank()) {
                                    viewModel.auditUserWalletLedger(target)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("AUDIT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // QUICK USER & DEVICE BAN CONTROL
        item {
            EsportsCard(modifier = Modifier.fillMaxWidth(), borderColor = RedNeon.copy(alpha = 0.5f)) {
                Column {
                    Text("BAN USER & LOCK DEVICE ⛔", fontWeight = FontWeight.Bold, color = RedNeon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = banUserIdInput,
                        onValueChange = { banUserIdInput = it },
                        placeholder = { Text("Target User ID (e.g., user_101)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        placeholder = { Text("Reason for suspension") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (banUserIdInput.isNotBlank()) {
                                viewModel.banUserAndDevice(banUserIdInput, banReasonInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedNeon, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BAN USER & LOCK DEVICE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // BANNED DEVICES LIST
        if (bannedDevices.isNotEmpty()) {
            item {
                Text("BANNED DEVICES HARDWARE LOCK (${bannedDevices.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }

            items(bannedDevices) { device ->
                EsportsCard(modifier = Modifier.fillMaxWidth(), borderColor = RedNeon) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(device.deviceFingerprint, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Reason: ${device.reason}", fontSize = 11.sp, color = RedNeon)
                            Text("Banned on: ${dateFormat.format(Date(device.bannedAt))}", fontSize = 10.sp, color = TextMuted)
                        }

                        Button(
                            onClick = { viewModel.unbanUserAndDevice("", device.deviceFingerprint) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("UNBAN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // SECURITY AUDIT LOGS TRAIL
        item {
            Text("SECURITY AUDIT LOGS TRAIL (${auditLogs.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        if (auditLogs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No security audit logs recorded yet.", color = TextMuted)
                }
            }
        } else {
            items(auditLogs) { log ->
                val badgeColor = when (log.severity) {
                    "CRITICAL" -> RedNeon
                    "WARNING" -> GoldAccent
                    else -> BluePrimary
                }

                EsportsCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgeTag(text = log.severity, containerColor = badgeColor.copy(0.2f), textColor = badgeColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(log.action, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Text(dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(log.details, fontSize = 12.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("User: ${log.userName} (${log.userId})", fontSize = 10.sp, color = TextMuted)
                            Text("FP: ${log.deviceFingerprint}", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
