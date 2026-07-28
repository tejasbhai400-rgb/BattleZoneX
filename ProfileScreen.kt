package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: BattlixViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editGameId by remember { mutableStateOf(user?.gameUsername ?: "") }
    var editPhone by remember { mutableStateOf(user?.phone ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // HEADER
        Text(
            text = "PLAYER PROFILE",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PROFILE HEADER CARD
        EsportsCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RedContainer)
                        .border(2.dp, RedPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = RedNeon,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.name ?: "Pro Gamer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (user?.isAdmin == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            BadgeTag(text = "ADMIN", containerColor = RedPrimary, textColor = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BadgeTag(
                            text = "FF UID: ${user?.freeFireUid ?: "1234567890"}",
                            containerColor = RedContainer,
                            textColor = RedNeon,
                            icon = Icons.Default.Badge
                        )

                        if (user?.isPhoneVerified == true) {
                            BadgeTag(
                                text = "VERIFIED ✓",
                                containerColor = GreenSuccess.copy(alpha = 0.2f),
                                textColor = GreenSuccess,
                                icon = Icons.Default.Verified
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "IGN: ${user?.gameUsername ?: "BattliX_Pro"}",
                        fontSize = 12.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${user?.phone} • ${user?.email ?: "gamer@battlix.gg"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                IconButton(onClick = {
                    editName = user?.name ?: ""
                    editGameId = user?.gameUsername ?: ""
                    editPhone = user?.phone ?: ""
                    showEditDialog = true
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = RedNeon)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CAREER STATS GRID
        Text(text = "CAREER STATS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Matches Played",
                value = "${user?.matchesPlayed ?: 0}",
                icon = Icons.Default.SportsEsports,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Kills",
                value = "${user?.totalKills ?: 0}",
                icon = Icons.Default.GpsFixed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Winnings",
                value = "₹${String.format("%.0f", user?.totalWinnings ?: 0.0)}",
                icon = Icons.Default.EmojiEvents,
                color = GoldAccent,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Ref Earnings",
                value = "₹10/ref",
                icon = Icons.Default.CardGiftcard,
                color = GreenAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECURITY & CONNECTED DEVICE CARD
        EsportsCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SECURITY & DEVICE LOCK", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                    BadgeTag(text = "PROTECTED", containerColor = GreenSuccess.copy(alpha = 0.2f), textColor = GreenSuccess)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Phone Verification", fontSize = 12.sp, color = TextMuted)
                    Text(if (user?.isPhoneVerified == true) "VERIFIED ✅" else "UNVERIFIED ⚠️", fontSize = 12.sp, color = if (user?.isPhoneVerified == true) GreenSuccess else GoldAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Device Fingerprint", fontSize = 12.sp, color = TextMuted)
                    Text(user?.deviceFingerprint ?: "DEV-8F92A1B0-SEC", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Firebase UID", fontSize = 12.sp, color = TextMuted)
                    Text((user?.firebaseUid ?: "fb_uid_101").take(16) + "...", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK MENU
        Text(text = "QUICK SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(title = "Wallet & Transactions", icon = Icons.Default.AccountBalanceWallet, onClick = onNavigateToWallet)
        ProfileMenuItem(title = "In-App Notifications", icon = Icons.Default.Notifications, onClick = onNavigateToNotifications)
        ProfileMenuItem(title = "Referral Program", icon = Icons.Default.CardGiftcard, onClick = onNavigateToReferral)

        if (user?.isAdmin == true) {
            ProfileMenuItem(title = "Admin Panel (Manage Matches & Withdrawals)", icon = Icons.Default.AdminPanelSettings, textColor = RedNeon, onClick = onNavigateToAdmin)
        }

        ProfileMenuItem(title = "Log Out", icon = Icons.Default.Logout, textColor = RedNeon, onClick = onLogout)
    }

    // EDIT PROFILE DIALOG
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = BlackCard,
            title = { Text("EDIT PROFILE", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editGameId,
                        onValueChange = { editGameId = it },
                        label = { Text("In-Game Username") },
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
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
                }
            },
            confirmButton = {
                EsportsButton(
                    text = "SAVE CHANGES",
                    onClick = {
                        viewModel.updateProfile(editName, editGameId, editPhone)
                        showEditDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = BlackSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = BlackSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
