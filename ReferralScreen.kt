package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BattlixViewModel
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*

@Composable
fun ReferralScreen(
    viewModel: BattlixViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    val refCode = user?.referralCode.takeIf { !it.isNullOrEmpty() } ?: "BTLX7A9K"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // TOP HEADER
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "REFER & EARN CASH",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (user == null) {
            // RETRY CARD IF USER IS NULL
            EsportsCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = RedPrimary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Loading referral code...", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    EsportsButton(
                        text = "RETRY LOADING",
                        onClick = { viewModel.retryLoadUser() },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        } else {
            // HERO GIFT CARD
            EsportsCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldAccent.copy(alpha = 0.6f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "EARN ₹5 FOR EVERY FRIEND",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccent,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "When your friend signs up with your code and completes their first deposit of ₹10+, you BOTH get ₹5 cash!",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // REFERRAL CODE DISPLAY BOX
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1018), RoundedCornerShape(10.dp))
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(10.dp)),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YOUR UNIQUE CODE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(refCode, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 2.sp)
                            }

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("BattliX Referral Code", refCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Referral code copied.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COPY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    EsportsButton(
                        text = "SHARE REFERRAL CODE",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join me on BattliX Free Fire MAX Esports! Use my referral code $refCode and complete your first deposit of ₹10+ to get ₹5 bonus cash!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Referral Code"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = GoldAccent,
                        contentColor = Color.Black,
                        icon = Icons.Default.Share
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HOW IT WORKS
        Text(text = "HOW IT WORKS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)

        Spacer(modifier = Modifier.height(12.dp))

        val steps = listOf(
            "1. Share your code $refCode with your friends.",
            "2. Your friend enters your referral code during signup.",
            "3. Friend completes their first deposit of ₹10 or more.",
            "4. You and your friend both receive ₹5 cash bonus!"
        )

        steps.forEach { step ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = BlackSurface
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = step, fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}
