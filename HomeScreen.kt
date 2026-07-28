package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
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

@Composable
fun HomeScreen(
    viewModel: BattlixViewModel,
    onNavigateToTournaments: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val tournaments by viewModel.allTournaments.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()

    val upcomingTournaments = remember(tournaments) {
        tournaments.filter { it.status != "COMPLETED" }
    }

    val selectedCategory by viewModel.selectedGameFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(RedContainer)
                        .border(1.5.dp, RedPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = RedNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = user?.name ?: "Pro Gamer",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "ID: ${user?.gameUsername ?: "BattliX"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Wallet Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BlackSurface,
                    modifier = Modifier
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToWallet() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${String.format("%.0f", user?.totalBalance ?: 0.0)}",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notifications Bell
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                    if (unreadNotifs > 0) {
                        Badge(
                            containerColor = RedNeon,
                            contentColor = Color.White,
                            modifier = Modifier.padding(top = 6.dp, end = 6.dp)
                        ) {
                            Text(unreadNotifs.toString(), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // ANNOUNCEMENT TICKER
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            color = RedContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, RedNeon.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = RedNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🔥 BGMI Solo Tournament Room ID Unlocked! Minimum withdrawal ₹10 active.",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HERO FEATURED BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, RedPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_battlix_banner_1785044702370),
                contentDescription = "Hero Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                BadgeTag(text = "FEATURED TOURNAMENT", containerColor = RedPrimary, textColor = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BGMI Championship Mega League",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Prize Pool: ₹1,000 • Per Kill: ₹15",
                    fontSize = 12.sp,
                    color = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GAME CATEGORY SELECTOR
        Text(
            text = "SELECT GAME",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val games = listOf("ALL", "BGMI", "Free Fire", "Call of Duty", "Valorant")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(games) { game ->
                val isSelected = selectedCategory == game
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectedGameFilter.value = game
                        onNavigateToTournaments(game)
                    },
                    label = { Text(game, fontWeight = FontWeight.Bold) },
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

        Spacer(modifier = Modifier.height(24.dp))

        // REFERRAL CASH PROMO BANNER
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToReferral() },
            shape = RoundedCornerShape(12.dp),
            color = BlackSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "REFER & EARN ₹10 REAL CASH",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Share your code ${user?.referralCode ?: "BATTLIX"} with friends",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // UPCOMING MATCHES LIST
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UPCOMING MATCHES",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            TextTextButton(
                text = "VIEW ALL",
                onClick = { onNavigateToTournaments("ALL") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (upcomingTournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No upcoming matches found.", color = TextMuted)
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                upcomingTournaments.take(3).forEach { tournament ->
                    HomeTournamentCard(
                        tournament = tournament,
                        onClick = { onNavigateToDetail(tournament.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTournamentCard(
    tournament: TournamentEntity,
    onClick: () -> Unit
) {
    EsportsCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (tournament.isRoomUnlocked()) GreenAccent.copy(alpha = 0.8f) else BorderDark,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Game Thumbnail
                val imgRes = when (tournament.gameType.lowercase()) {
                    "bgmi" -> R.drawable.img_bgmi_card_1785044714566
                    "free fire" -> R.drawable.img_freefire_card_1785044725320
                    else -> R.drawable.img_battlix_banner_1785044702370
                }

                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgeTag(text = tournament.gameType, containerColor = RedContainer, textColor = RedNeon)
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeTag(text = tournament.matchType, containerColor = BlackSurfaceVariant, textColor = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tournament.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Prize: ₹${String.format("%.0f", tournament.prizePool)}",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Per Kill: ₹${String.format("%.0f", tournament.perKill)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ENTRY",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%.0f", tournament.entryFee)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RedNeon
                )
            }
        }
    }
}

@Composable
fun TextTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = RedNeon,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.clickable { onClick() }
    )
}
