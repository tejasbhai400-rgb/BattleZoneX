package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: BattlixViewModel,
    onBackClick: () -> Unit
) {
    val notifications by viewModel.userNotifications.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.markNotificationsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .statusBarsPadding()
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NOTIFICATIONS",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No notifications yet.", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif ->
                    val (badgeColor, textColor) = when (notif.type) {
                        "ROOM_CREDS" -> Pair(GreenAccent.copy(alpha = 0.2f), GreenAccent)
                        "WALLET" -> Pair(GoldAccent.copy(alpha = 0.2f), GoldAccent)
                        else -> Pair(RedContainer, RedNeon)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = BlackSurface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgeTag(text = notif.type, containerColor = badgeColor, textColor = textColor)
                                Text(text = dateFormat.format(Date(notif.timestamp)), fontSize = 11.sp, color = TextMuted)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = notif.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(text = notif.message, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
