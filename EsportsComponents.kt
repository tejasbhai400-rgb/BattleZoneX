package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TournamentEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EsportsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = RedPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .shadow(8.dp, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp), spotColor = RedPrimary),
        shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFF2A2C38),
            disabledContentColor = Color(0xFF6E7191)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun EsportsCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderDark,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
            color = BlackCard,
            contentColor = TextPrimary,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
            color = BlackCard,
            contentColor = TextPrimary,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun BadgeTag(
    text: String,
    containerColor: Color = RedContainer,
    textColor: Color = RedNeon,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        modifier = Modifier.border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RoomCredsCard(
    tournament: TournamentEntity,
    isJoined: Boolean
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Tick every second to update remaining time
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val isUnlocked = tournament.isRoomUnlocked(currentTime)
    val remainingMs = tournament.remainingTimeForRoomUnlock(currentTime)

    EsportsCard(
        borderColor = if (isUnlocked && isJoined) GreenAccent.copy(alpha = 0.8f) else RedPrimary.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.VpnKey else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) GreenAccent else RedPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ROOM ID & PASSWORD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = when {
                            !isJoined -> "Join match to access room credentials"
                            isUnlocked -> "Unlocked! Join room in-game now"
                            else -> "Unlocks 5 minutes before match start"
                        },
                        fontSize = 12.sp,
                        color = if (isUnlocked && isJoined) GreenAccent else TextSecondary
                    )
                }
            }
            if (isJoined && isUnlocked) {
                BadgeTag(text = "UNLOCKED", containerColor = GreenAccent.copy(alpha = 0.2f), textColor = GreenAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isJoined && isUnlocked) {
            // Unlocked state with copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1018), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Room ID: ${tournament.roomId}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenAccent)
                    Text(text = "Password: ${tournament.roomPassword}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Room Credentials", "Room ID: ${tournament.roomId}\nPassword: ${tournament.roomPassword}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Room ID & Password copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .background(RedPrimary, RoundedCornerShape(8.dp))
                        .size(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                }
            }
        } else {
            // Locked Countdown State
            val totalSeconds = remainingMs / 1000
            val hours = totalSeconds / 3600
            val mins = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60

            val formattedTime = if (hours > 0) {
                String.format("%02dh %02dm %02ds", hours, mins, secs)
            } else {
                String.format("%02dm %02ds", mins, secs)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1018), RoundedCornerShape(8.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = RedNeon, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (remainingMs > 0) "Unlocks in: $formattedTime" else "Waiting for Admin to publish Room ID",
                        fontWeight = FontWeight.Bold,
                        color = RedNeon,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
