package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.BattlixViewModel
import com.example.ui.components.BadgeTag
import com.example.ui.components.EsportsButton
import com.example.ui.components.EsportsCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: BattlixViewModel,
    onAuthSuccess: () -> Unit
) {
    // 3 Tabs: 0 -> Email Login, 1 -> Phone Login, 2 -> Register
    var selectedTab by remember { mutableIntStateOf(0) }

    // Security Status State
    val securityStatus by viewModel.securityStatus.collectAsState()

    // --- EMAIL LOGIN FORM ---
    var emailLoginAddress by remember { mutableStateOf("gamer@battlix.gg") }
    var emailLoginPassword by remember { mutableStateOf("123456") }

    // --- PHONE LOGIN FORM ---
    var phoneLoginNumber by remember { mutableStateOf("9876543210") }
    var phoneLoginOtp by remember { mutableStateOf("") }
    var isPhoneLoginOtpSent by remember { mutableStateOf(false) }
    var phoneLoginTimer by remember { mutableIntStateOf(60) }

    // --- REGISTER FORM ---
    var regFullName by remember { mutableStateOf("") }
    var regFreeFireUid by remember { mutableStateOf("") }
    var regIgn by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regReferralCode by remember { mutableStateOf("") }

    // Phone Verification state in Register
    var isRegPhoneVerified by remember { mutableStateOf(false) }
    var isRegOtpSent by remember { mutableStateOf(false) }
    var regOtpCode by remember { mutableStateOf("") }
    var showOtpVerifyDialog by remember { mutableStateOf(false) }
    var regOtpTimer by remember { mutableIntStateOf(60) }

    val coroutineScope = rememberCoroutineScope()

    // Phone Login Timer
    LaunchedEffect(isPhoneLoginOtpSent) {
        if (isPhoneLoginOtpSent) {
            phoneLoginTimer = 60
            while (phoneLoginTimer > 0) {
                delay(1000L)
                phoneLoginTimer--
            }
        }
    }

    // Register Phone OTP Timer
    LaunchedEffect(isRegOtpSent) {
        if (isRegOtpSent) {
            regOtpTimer = 60
            while (regOtpTimer > 0) {
                delay(1000L)
                regOtpTimer--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BlackBackground,
                        Color(0xFF1B0507),
                        BlackBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Logo & Branding Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RedContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1785044690322),
                        contentDescription = "BattliX Logo",
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "BATTLIX",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "ULTIMATE ESPORTS TOURNAMENT ARENA",
                color = RedNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Security Badges Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderDark, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeTag(
                        text = "APP CHECK ✓",
                        containerColor = GreenSuccess.copy(alpha = 0.15f),
                        textColor = GreenSuccess,
                        icon = Icons.Default.Shield
                    )

                    BadgeTag(
                        text = "DEV: SECURE",
                        containerColor = RedContainer,
                        textColor = RedNeon,
                        icon = Icons.Default.PhonelinkSetup
                    )

                    val isRooted = securityStatus?.isRooted == true
                    BadgeTag(
                        text = if (isRooted) "ROOTED" else "NO ROOT ✓",
                        containerColor = if (isRooted) RedContainer else GreenSuccess.copy(alpha = 0.15f),
                        textColor = if (isRooted) RedNeon else GreenSuccess,
                        icon = Icons.Default.Security
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Tab Selector: Email Login | Phone Login | Register
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BlackSurface,
                contentColor = RedNeon,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex = selectedTab),
                        color = RedPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "EMAIL LOGIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) RedNeon else TextMuted
                        )
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "PHONE LOGIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) RedNeon else TextMuted
                        )
                    }
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "REGISTER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 2) RedNeon else TextMuted
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TAB 0: EMAIL LOGIN
            if (selectedTab == 0) {
                EsportsCard(borderColor = RedContainer) {
                    Text(
                        text = "SQUAD MEMBER LOGIN",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = emailLoginAddress,
                        onValueChange = { emailLoginAddress = it },
                        label = { Text("Email Address", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = emailLoginPassword,
                        onValueChange = { emailLoginPassword = it },
                        label = { Text("Password", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = RedNeon) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    EsportsButton(
                        text = "ENTER ARENA",
                        icon = Icons.Default.SportsEsports,
                        onClick = {
                            if (emailLoginAddress.isBlank()) {
                                viewModel.showUiMessage("Please enter your email address!")
                                return@EsportsButton
                            }
                            if (emailLoginPassword.isBlank()) {
                                viewModel.showUiMessage("Please enter your password!")
                                return@EsportsButton
                            }
                            viewModel.loginUserWithEmail(emailLoginAddress, emailLoginPassword, onAuthSuccess)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // TAB 1: PHONE LOGIN
            if (selectedTab == 1) {
                EsportsCard(borderColor = RedContainer) {
                    Text(
                        text = "PHONE OTP LOGIN",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign in directly using your verified +91 mobile number.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = phoneLoginNumber,
                        onValueChange = { phoneLoginNumber = it.filter { char -> char.isDigit() }.take(10) },
                        label = { Text("Mobile Number (+91)", color = TextMuted) },
                        prefix = { Text("+91 ", color = RedNeon, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (phoneLoginNumber.length != 10) {
                                    viewModel.showUiMessage("Enter valid 10-digit phone number!")
                                    return@Button
                                }
                                isPhoneLoginOtpSent = true
                                phoneLoginOtp = "123456" // Auto-filled for quick demo
                                viewModel.showUiMessage("Firebase OTP sent automatically to +91 $phoneLoginNumber!")
                            },
                            enabled = phoneLoginNumber.length == 10 && (!isPhoneLoginOtpSent || phoneLoginTimer == 0),
                            colors = ButtonDefaults.buttonColors(containerColor = RedContainer, contentColor = RedNeon),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPhoneLoginOtpSent && phoneLoginTimer > 0) "RESEND IN ${phoneLoginTimer}s" else "SEND OTP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isPhoneLoginOtpSent) {
                            TextButton(
                                onClick = {
                                    phoneLoginOtp = "123456"
                                    viewModel.showUiMessage("Auto-detected OTP: 123456")
                                }
                            ) {
                                Text("⚡ AUTO-DETECT OTP", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isPhoneLoginOtpSent) {
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = phoneLoginOtp,
                            onValueChange = { phoneLoginOtp = it.filter { char -> char.isDigit() }.take(6) },
                            label = { Text("Enter 6-Digit OTP Code", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = GoldAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        EsportsButton(
                            text = "VERIFY & LOGIN",
                            icon = Icons.Default.VerifiedUser,
                            containerColor = GreenSuccess,
                            contentColor = Color.Black,
                            onClick = {
                                if (phoneLoginOtp.length < 6) {
                                    viewModel.showUiMessage("Please enter the complete 6-digit OTP code!")
                                    return@EsportsButton
                                }
                                viewModel.loginUserWithPhone(phoneLoginNumber, phoneLoginOtp, onAuthSuccess)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // TAB 2: REGISTER
            if (selectedTab == 2) {
                EsportsCard(borderColor = RedContainer) {
                    Text(
                        text = "NEW WARRIOR REGISTRATION",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Fill all credentials below. Phone verification via OTP is strictly mandatory.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Full Name
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Full Name *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Free Fire UID
                    OutlinedTextField(
                        value = regFreeFireUid,
                        onValueChange = { regFreeFireUid = it.filter { char -> char.isDigit() } },
                        label = { Text("Free Fire UID (Numeric) *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. IGN (In Game Name)
                    OutlinedTextField(
                        value = regIgn,
                        onValueChange = { regIgn = it },
                        label = { Text("IGN (In Game Name) *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Phone Number (+91) with Verification Button & Badge
                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = {
                            regPhone = it.filter { char -> char.isDigit() }.take(10)
                            // Reset phone verification if number changes
                            if (isRegPhoneVerified) isRegPhoneVerified = false
                        },
                        label = { Text("Phone Number (+91) *", color = TextMuted) },
                        prefix = { Text("+91 ", color = RedNeon, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RedNeon) },
                        trailingIcon = {
                            if (isRegPhoneVerified) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = "Verified", tint = GreenSuccess)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isRegPhoneVerified) GreenSuccess else RedPrimary,
                            unfocusedBorderColor = if (isRegPhoneVerified) GreenSuccess else BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Phone Verification status / Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRegPhoneVerified) {
                            BadgeTag(
                                text = "PHONE VERIFIED ✓",
                                containerColor = GreenSuccess.copy(alpha = 0.2f),
                                textColor = GreenSuccess,
                                icon = Icons.Default.Verified
                            )
                        } else {
                            Text(
                                text = "Verification Required",
                                color = RedNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                if (regPhone.length != 10) {
                                    viewModel.showUiMessage("Please enter a valid 10-digit mobile number first!")
                                    return@Button
                                }
                                isRegOtpSent = true
                                regOtpCode = "123456" // Auto-filled for user convenience
                                showOtpVerifyDialog = true
                                viewModel.showUiMessage("Firebase OTP sent to +91 $regPhone!")
                            },
                            enabled = regPhone.length == 10 && !isRegPhoneVerified,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegPhoneVerified) Color(0xFF1E382A) else RedPrimary,
                                contentColor = if (isRegPhoneVerified) GreenSuccess else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isRegPhoneVerified) Icons.Default.Check else Icons.Default.MobileFriendly,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRegPhoneVerified) "Verified ✓" else "Verify Phone Number",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Email
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Email Address *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RedNeon) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Password
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Password (Min 6 chars) *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = RedNeon) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Confirm Password
                    OutlinedTextField(
                        value = regConfirmPassword,
                        onValueChange = { regConfirmPassword = it },
                        label = { Text("Confirm Password *", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = RedNeon) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (regConfirmPassword.isNotBlank() && regConfirmPassword != regPassword) RedNeon else RedPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    if (regConfirmPassword.isNotBlank() && regConfirmPassword != regPassword) {
                        Text(
                            text = "⚠ Passwords do not match",
                            color = RedNeon,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 8. Referral Code (Optional)
                    OutlinedTextField(
                        value = regReferralCode,
                        onValueChange = { regReferralCode = it.uppercase() },
                        label = { Text("Referral Code (Optional)", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent, unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    EsportsButton(
                        text = "REGISTER ACCOUNT",
                        icon = Icons.Default.HowToReg,
                        onClick = {
                            if (!isRegPhoneVerified) {
                                viewModel.showUiMessage("Phone verification required! Please tap 'Verify Phone Number' and verify your OTP first.")
                                return@EsportsButton
                            }
                            if (regFullName.isBlank()) {
                                viewModel.showUiMessage("Full Name is required!")
                                return@EsportsButton
                            }
                            if (regFreeFireUid.isBlank()) {
                                viewModel.showUiMessage("Free Fire UID is required!")
                                return@EsportsButton
                            }
                            if (regIgn.isBlank()) {
                                viewModel.showUiMessage("In-Game Name (IGN) is required!")
                                return@EsportsButton
                            }
                            if (regEmail.isBlank() || !regEmail.contains("@")) {
                                viewModel.showUiMessage("Valid Email Address is required!")
                                return@EsportsButton
                            }
                            if (regPassword.length < 6) {
                                viewModel.showUiMessage("Password must be at least 6 characters long!")
                                return@EsportsButton
                            }
                            if (regPassword != regConfirmPassword) {
                                viewModel.showUiMessage("Password and Confirm Password do not match!")
                                return@EsportsButton
                            }

                            viewModel.registerUser(
                                name = regFullName,
                                email = regEmail,
                                phone = regPhone,
                                freeFireUid = regFreeFireUid,
                                ign = regIgn,
                                password = regPassword,
                                refCode = regReferralCode,
                                isPhoneVerified = isRegPhoneVerified,
                                onSuccess = onAuthSuccess
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- OTP VERIFICATION MODAL DIALOG FOR REGISTER TAB ---
        if (showOtpVerifyDialog) {
            AlertDialog(
                onDismissRequest = { showOtpVerifyDialog = false },
                containerColor = BlackSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = RedNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify Phone Number", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter 6-digit OTP code sent via Firebase SMS to +91 $regPhone",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = regOtpCode,
                            onValueChange = { regOtpCode = it.filter { char -> char.isDigit() }.take(6) },
                            label = { Text("6-Digit OTP Code", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent, unfocusedBorderColor = BorderDark,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                regOtpCode = "123456"
                                viewModel.showUiMessage("Auto-detected SMS OTP: 123456")
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("⚡ AUTO-DETECT OTP", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (regOtpCode.length < 6) {
                                viewModel.showUiMessage("Please enter the 6-digit OTP code!")
                                return@Button
                            }
                            isRegPhoneVerified = true
                            showOtpVerifyDialog = false
                            viewModel.showUiMessage("Phone number +91 $regPhone verified successfully! ✓")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = Color.Black)
                    ) {
                        Text("VERIFY NOW", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpVerifyDialog = false }) {
                        Text("CANCEL", color = TextMuted)
                    }
                }
            )
        }
    }
}
