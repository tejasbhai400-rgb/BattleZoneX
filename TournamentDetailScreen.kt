package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ParticipantEntity
import com.example.data.TournamentEntity
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.components.RoomCredsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    viewModel: BattlixViewModel,
    onBackClick: () -> Unit
) {
    val tournaments by viewModel.allTournaments.collectAsState()
    val tournament = remember(tournaments, tournamentId) {
        tournaments.find { it.id == tournamentId }
    }

    val user by viewModel.currentUser.collectAsState()
    val joinedTournaments by viewModel.userJoinedTournaments.collectAsState()
    val participants by viewModel.getParticipantsForTournamentFlow(tournamentId).collectAsState(initial = emptyList())

    val isJoined = remember(joinedTournaments, tournamentId) {
        joinedTournaments.any { it.tournamentId == tournamentId }
    }

    var showJoinDialog by remember { mutableStateOf(false) }
    var inGameUsernameInput by remember { mutableStateOf(user?.gameUsername ?: "") }

    if (tournament == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RedPrimary)
        }
        return
    }

    val imgRes = when (tournament.gameType.lowercase()) {
        "bgmi" -> R.drawable.img_bgmi_card_1785044714566
        "free fire" -> R.drawable.img_freefire_card_1785044725320
        else -> R.drawable.img_battlix_banner_1785044702370
    }

    val dateFormat = remember { SimpleDateFormat("dd MMMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(tournament.startTime) { dateFormat.format(Date(tournament.startTime)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // HERO IMAGE & BACK BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    BlackBackground
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .background(BlackSurface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeTag(text = tournament.gameType, containerColor = RedPrimary, textColor = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeTag(text = tournament.matchType, containerColor = BlackSurfaceVariant, textColor = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeTag(text = "MAP: ${tournament.map}", containerColor = BlackSurfaceVariant, textColor = TextSecondary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tournament.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = RedNeon, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = formattedDate, fontSize = 13.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PRIZE POOL BREAKDOWN
                EsportsCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PRIZE DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Prize Pool", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${String.format("%.0f", tournament.prizePool)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Column {
                            Text("Per Kill Bonus", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${String.format("%.0f", tournament.perKill)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Entry Fee", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${String.format("%.0f", tournament.entryFee)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RedNeon)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ROOM CREDENTIALS CARD
                RoomCredsCard(tournament = tournament, isJoined = isJoined)

                Spacer(modifier = Modifier.height(20.dp))

                // MATCH RULES
                EsportsCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "MATCH RULES & GUIDELINES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tournament.rules,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // JOINED PLAYERS ROSTER
                Text(
                    text = "JOINED PLAYERS (${participants.size}/${tournament.totalSlots})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (participants.isEmpty()) {
                    Text("No players joined yet. Be the first to join!", color = TextMuted, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        participants.forEach { p ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = BlackSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BadgeTag(text = "#${p.slotNumber}", containerColor = RedContainer, textColor = RedNeon)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = p.inGameUsername, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    }
                                    Text(text = "READY", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM ACTION BAR
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = BlackSurface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isJoined) {
                    EsportsButton(
                        text = "ALREADY JOINED ✅",
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = GreenAccent,
                        contentColor = Color.Black
                    )
                } else if (tournament.status == "COMPLETED") {
                    EsportsButton(
                        text = "MATCH COMPLETED",
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EsportsButton(
                        text = "JOIN FOR ₹${String.format("%.0f", tournament.entryFee)}",
                        onClick = { showJoinDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.SportsEsports
                    )
                }
            }
        }
    }

    // JOIN TOURNAMENT DIALOG
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = BlackCard,
            title = {
                Text(
                    text = "CONFIRM REGISTRATION",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tournament: ${tournament.title}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Entry Fee: ₹${String.format("%.0f", tournament.entryFee)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedNeon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inGameUsernameInput,
                        onValueChange = { inGameUsernameInput = it },
                        label = { Text("Your In-Game ID / Name") },
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
                    text = "CONFIRM & PAY",
                    onClick = {
                        viewModel.joinTournament(tournament.id, inGameUsernameInput) {
                            showJoinDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            }
        )
    }
}
