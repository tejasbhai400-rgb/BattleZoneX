package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: BattlixViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user?.id) {
        if (user != null) {
            viewModel.refreshTransactionsFromFirestore()
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Deposit, 1 = Withdraw, 2 = Transactions

    // Referral input field in wallet
    var manualRefCodeInput by remember { mutableStateOf("") }

    // Deposit fields
    var depositAmountInput by remember { mutableStateOf("100") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI Auto") }

    // Withdraw fields
    var withdrawAmountInput by remember { mutableStateOf("50") }
    var upiIdInput by remember { mutableStateOf("gamer@upi") }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    // 24 Hour frequency remaining check
    val currentTime = remember { System.currentTimeMillis() }
    val timeSinceLast = currentTime - (user?.lastWithdrawalTime ?: 0L)
    val twentyFourHoursMs = 24 * 60 * 60 * 1000L
    val isWithdrawalLocked = (user?.lastWithdrawalTime ?: 0L) > 0 && timeSinceLast < twentyFourHoursMs
    val remainingMs = twentyFourHoursMs - timeSinceLast

    val referralCode = user?.referralCode.takeIf { !it.isNullOrEmpty() } ?: "BTLX7A9K"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "BATTLIX WALLET",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        if (user == null) {
            // RETRY CARD IF USER PROFILE DELAYED OR NULL
            EsportsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = RedPrimary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Profile data loading...", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    EsportsButton(
                        text = "RETRY LOADING",
                        onClick = { viewModel.retryLoadUser() },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        } else {
            // BALANCE OVERVIEW CARD
            EsportsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = GoldAccent.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "TOTAL BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format("%.2f", user?.totalBalance ?: 0.0)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Deposit Balance", fontSize = 11.sp, color = TextMuted)
                        Text("₹${String.format("%.2f", user?.depositBalance ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Winning Balance (Withdrawable)", fontSize = 11.sp, color = TextMuted)
                        Text("₹${String.format("%.2f", user?.winningBalance ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DEPOSIT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Outbox, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WITHDRAW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // INSTANT REFERRAL CODE CARD ON WALLET PAGE
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                color = BlackSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("YOUR REFERRAL CODE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(referralCode, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("BattliX Referral Code", referralCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Referral code copied.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Earn ₹5 cash when your friend makes their 1st deposit of ₹10 or more!", fontSize = 11.sp, color = TextSecondary)

                    // Show referral code status or input
                    val currentUserEntity = user
                    if (currentUserEntity != null) {
                        if (currentUserEntity.referredBy.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgeTag(text = "REFERRED BY: ${currentUserEntity.referredBy}", containerColor = BlackCard, textColor = GreenAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentUserEntity.hasClaimedReferralDepositBonus) "₹5 Bonus Claimed! 🎉" else "Deposit ₹10+ for ₹5 Bonus",
                                    fontSize = 10.sp,
                                    color = if (currentUserEntity.hasClaimedReferralDepositBonus) GreenAccent else GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualRefCodeInput,
                                    onValueChange = { manualRefCodeInput = it.uppercase() },
                                    placeholder = { Text("Have a referral code?", fontSize = 11.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = GoldAccent,
                                        unfocusedBorderColor = BorderDark,
                                        focusedLabelColor = GoldAccent,
                                        unfocusedLabelColor = TextSecondary
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (manualRefCodeInput.isNotBlank()) {
                                            viewModel.applyReferralCode(manualRefCodeInput) {
                                                manualRefCodeInput = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(46.dp)
                                ) {
                                    Text("APPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RECENT TRANSACTIONS SECTION (SYNCED WITH FIRESTORE)
            EsportsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = GoldAccent.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECENT TRANSACTIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeTag(text = "Firestore", containerColor = RedContainer, textColor = RedNeon)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.refreshTransactionsFromFirestore() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (transactions.size > 3) {
                            Text(
                                text = "SEE ALL (${transactions.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedPrimary,
                                modifier = Modifier
                                    .clickable { selectedTab = 2 }
                                    .padding(start = 4.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (transactions.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = BlackSurface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No recent transactions found",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Deposits, withdrawals & tournament joins automatically sync from Firestore",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.take(3).forEach { tx ->
                            TransactionItemRow(tx = tx, dateFormat = dateFormat)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TAB ROW
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BlackSurface,
            contentColor = RedPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RedPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("DEPOSIT", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), color = if (selectedTab == 0) RedPrimary else TextMuted)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("WITHDRAW", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), color = if (selectedTab == 1) RedPrimary else TextMuted)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("HISTORY", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), color = if (selectedTab == 2) RedPrimary else TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
        ) {
            when (selectedTab) {
                0 -> {
                    // DEPOSIT TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Select Deposit Amount", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val presets = listOf("10", "20", "50", "100", "200", "500")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presets) { preset ->
                                val isSelected = depositAmountInput == preset
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) RedPrimary else BlackSurface,
                                    modifier = Modifier
                                        .border(1.dp, if (isSelected) RedPrimary else BorderDark, RoundedCornerShape(8.dp))
                                        .clickable { depositAmountInput = preset }
                                ) {
                                    Text(
                                        text = "₹$preset",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = depositAmountInput,
                            onValueChange = { depositAmountInput = it },
                            label = { Text("Custom Amount (Min ₹1)") },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = RedPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RedPrimary,
                                unfocusedBorderColor = BorderDark,
                                focusedLabelColor = RedPrimary,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        EsportsButton(
                            text = "ADD CASH ₹$depositAmountInput",
                            onClick = {
                                val amt = depositAmountInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.depositMoney(amt, selectedPaymentMethod) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Add
                        )
                    }
                }

                1 -> {
                    // WITHDRAW TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        // RULES BANNER
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = RedContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedNeon.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = RedNeon, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WITHDRAWAL RULES", fontWeight = FontWeight.Bold, color = RedNeon, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("1. Minimum withdrawal is ₹10.\n2. Only 1 withdrawal allowed every 24 hours.", fontSize = 12.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isWithdrawalLocked) {
                            val hours = remainingMs / (60 * 60 * 1000)
                            val mins = (remainingMs % (60 * 60 * 1000)) / (60 * 1000)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = BlackCard,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("24h Cooldown Active", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 13.sp)
                                        Text("Next withdrawal unlocks in ${hours}h ${mins}m", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = { upiIdInput = it },
                            label = { Text("UPI ID (e.g., gamer@upi)") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = GreenAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedLabelColor = GreenAccent,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = withdrawAmountInput,
                            onValueChange = { withdrawAmountInput = it },
                            label = { Text("Amount to Withdraw (Min ₹10)") },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GreenAccent) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedLabelColor = GreenAccent,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        EsportsButton(
                            text = "WITHDRAW ₹$withdrawAmountInput TO UPI",
                            onClick = {
                                val amt = withdrawAmountInput.toDoubleOrNull() ?: 0.0
                                viewModel.withdrawMoney(amt, upiIdInput) {}
                            },
                            enabled = !isWithdrawalLocked,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = GreenAccent,
                            contentColor = Color.Black
                        )
                    }
                }

                2 -> {
                    // HISTORY TAB
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No transaction history.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your deposits, withdrawals & game rewards will appear here.", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.refreshTransactionsFromFirestore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SYNC FIRESTORE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ALL TRANSACTIONS (${transactions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                                    TextButton(onClick = { viewModel.refreshTransactionsFromFirestore() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("REFRESH", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            items(transactions) { tx ->
                                TransactionItemRow(tx = tx, dateFormat = dateFormat)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: TransactionEntity,
    dateFormat: SimpleDateFormat
) {
    val isPositive = tx.type in listOf("DEPOSIT", "WINNING", "REFERRAL_BONUS")

    val (icon, iconBg, iconColor) = when (tx.type) {
        "DEPOSIT" -> Triple(Icons.Default.ArrowDownward, GreenAccent.copy(alpha = 0.15f), GreenAccent)
        "WITHDRAWAL" -> Triple(Icons.Default.ArrowUpward, RedNeon.copy(alpha = 0.15f), RedNeon)
        "ENTRY_FEE" -> Triple(Icons.Default.SportsEsports, GoldAccent.copy(alpha = 0.15f), GoldAccent)
        "WINNING" -> Triple(Icons.Default.EmojiEvents, GreenAccent.copy(alpha = 0.15f), GreenAccent)
        "REFERRAL_BONUS" -> Triple(Icons.Default.CardGiftcard, GoldAccent.copy(alpha = 0.15f), GoldAccent)
        else -> Triple(Icons.Default.ReceiptLong, RedPrimary.copy(alpha = 0.15f), RedPrimary)
    }

    val statusUpper = tx.status.uppercase()
    val statusText = when (statusUpper) {
        "SUCCESS", "COMPLETED" -> "Completed"
        "PENDING", "PROCESSING" -> "Pending"
        "FAILED", "REJECTED", "CANCELLED" -> "Failed"
        else -> tx.status.ifBlank { "Completed" }
    }
    val statusBg = when (statusUpper) {
        "SUCCESS", "COMPLETED" -> GreenAccent.copy(alpha = 0.15f)
        "PENDING", "PROCESSING" -> GoldAccent.copy(alpha = 0.15f)
        "FAILED", "REJECTED", "CANCELLED" -> RedNeon.copy(alpha = 0.15f)
        else -> RedPrimary.copy(alpha = 0.15f)
    }
    val statusColor = when (statusUpper) {
        "SUCCESS", "COMPLETED" -> GreenAccent
        "PENDING", "PROCESSING" -> GoldAccent
        "FAILED", "REJECTED", "CANCELLED" -> RedNeon
        else -> RedPrimary
    }
    val statusIcon = when (statusUpper) {
        "SUCCESS", "COMPLETED" -> Icons.Default.CheckCircle
        "PENDING", "PROCESSING" -> Icons.Default.Schedule
        "FAILED", "REJECTED", "CANCELLED" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = BlackSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateFormat.format(Date(tx.timestamp)),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        if (tx.note.isNotBlank()) {
                            Text(" • ", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = tx.note,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else "-"}₹${String.format("%.2f", tx.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (isPositive) GreenAccent else RedNeon
                )
                Spacer(modifier = Modifier.height(3.dp))
                BadgeTag(
                    text = statusText,
                    containerColor = statusBg,
                    textColor = statusColor,
                    icon = statusIcon
                )
            }
        }
    }
}

