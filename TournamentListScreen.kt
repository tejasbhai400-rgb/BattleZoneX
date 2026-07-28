package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TournamentEntity
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    viewModel: BattlixViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val tournaments by viewModel.filteredTournaments.collectAsState()
    val joinedTournaments by viewModel.userJoinedTournaments.collectAsState()
    val selectedGame by viewModel.selectedGameFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    val games = listOf("ALL", "BGMI", "Free Fire", "Call of Duty", "Valorant")
    val statuses = listOf("ALL", "UPCOMING", "ONGOING", "COMPLETED")

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
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = RedPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "TOURNAMENTS",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        // STATUS TAB SELECTOR
        ScrollableTabRow(
            selectedTabIndex = statuses.indexOf(selectedStatus).coerceAtLeast(0),
            containerColor = BlackSurface,
            contentColor = RedPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val index = statuses.indexOf(selectedStatus).coerceAtLeast(0)
                if (index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = RedPrimary,
                        height = 3.dp
                    )
                }
            }
        ) {
            statuses.forEach { status ->
                val isSelected = selectedStatus == status
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectedStatusFilter.value = status },
                    text = {
                        Text(
                            text = status,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) RedPrimary else TextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GAME CHIPS
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(games) { game ->
                val isSelected = selectedGame == game
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedGameFilter.value = game },
                    label = { Text(game, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = BlackSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BorderDark,
                        selectedBorderColor = RedPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TOURNAMENT LIST
        if (tournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideogameAssetOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No tournaments found for this category.", color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tournaments) { tournament ->
                    val isJoined = joinedTournaments.any { it.tournamentId == tournament.id }
                    TournamentCard(
                        tournament = tournament,
                        isJoined = isJoined,
                        onClick = { onNavigateToDetail(tournament.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TournamentCard(
    tournament: TournamentEntity,
    isJoined: Boolean = false,
    onClick: () -> Unit
) {
    val imgRes = when (tournament.gameType.lowercase()) {
        "bgmi" -> R.drawable.img_bgmi_card_1785044714566
        "free fire" -> R.drawable.img_freefire_card_1785044725320
        else -> R.drawable.img_battlix_banner_1785044702370
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(tournament.startTime) { dateFormat.format(Date(tournament.startTime)) }

    val slotProgress = remember(tournament.joinedSlots, tournament.totalSlots) {
        if (tournament.totalSlots > 0) tournament.joinedSlots.toFloat() / tournament.totalSlots.toFloat() else 0f
    }

    EsportsCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isJoined) GreenAccent.copy(alpha = 0.8f) else if (tournament.isRoomUnlocked()) RedNeon.copy(alpha = 0.8f) else BorderDark,
        onClick = onClick
    ) {
        // Thumbnail & Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = imgRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        BadgeTag(text = tournament.gameType, containerColor = RedContainer, textColor = RedNeon)
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeTag(text = tournament.matchType, containerColor = BlackSurfaceVariant, textColor = TextSecondary)
                    }

                    if (isJoined) {
                        BadgeTag(text = "JOINED ✅", containerColor = GreenAccent.copy(alpha = 0.2f), textColor = GreenAccent)
                    } else if (tournament.isRoomUnlocked()) {
                        BadgeTag(text = "ROOM LIVE 🔑", containerColor = RedNeon.copy(alpha = 0.2f), textColor = RedNeon)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tournament.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prize Pool Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0E15), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PRIZE POOL", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text("₹${String.format("%.0f", tournament.prizePool)}", fontSize = 15.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PER KILL", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text("₹${String.format("%.0f", tournament.perKill)}", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ENTRY FEE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text("₹${String.format("%.0f", tournament.entryFee)}", fontSize = 15.sp, color = RedNeon, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slot progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spots Filled",
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = "${tournament.joinedSlots}/${tournament.totalSlots}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RedNeon
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { slotProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = RedPrimary,
            trackColor = BlackSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        val btnText = when {
            tournament.status == "COMPLETED" -> "MATCH ENDED"
            isJoined -> "ALREADY JOINED ✅"
            tournament.isRoomUnlocked() -> "ROOM LIVE 🔑"
            else -> "JOIN TOURNAMENT"
        }

        val btnBg = when {
            isJoined -> GreenAccent
            tournament.isRoomUnlocked() -> RedPrimary
            else -> RedPrimary
        }

        val btnFg = when {
            isJoined -> Color.Black
            else -> Color.White
        }

        EsportsButton(
            text = btnText,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = btnBg,
            contentColor = btnFg
        )
    }
}
