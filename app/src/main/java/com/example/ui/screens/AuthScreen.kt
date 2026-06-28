package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.TechPurple
import com.example.ui.viewmodel.PrisimViewModel

@Composable
fun AuthScreen(viewModel: PrisimViewModel) {
    var authMode by remember { mutableStateOf(0) } // 0: Email, 1: Google, 2: Phone
    var isSignUp by remember { mutableStateOf(false) }
    var forgotPasswordMode by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    // Input States
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prisim Canvas Logo
            PrisimLogo(modifier = Modifier.size(100.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "PRISIM",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Professional Social Collaboration",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (forgotPasswordMode) {
                // Forgot password view
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Reset Password",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter your verified email address and we will send you a reset link.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                showVerificationDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().testTag("reset_password_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send Reset Link", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Back to Login",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { forgotPasswordMode = false }
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Multi-tab Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Tabs selector
                        TabRow(
                            selectedTabIndex = authMode,
                            containerColor = Color.Transparent,
                            divider = {},
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Tab(
                                selected = authMode == 0,
                                onClick = { authMode = 0 },
                                text = { Text("Email", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = authMode == 1,
                                onClick = { authMode = 1 },
                                text = { Text("Google", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = authMode == 2,
                                onClick = { authMode = 2 },
                                text = { Text("Phone", fontWeight = FontWeight.Bold) }
                            )
                        }

                        // Input fields based on mode
                        AnimatedContent(targetState = authMode, label = "InputsTransition") { targetMode ->
                            Column {
                                when (targetMode) {
                                    0 -> { // Email
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it },
                                            label = { Text("Email Address") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                            modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (isSignUp) {
                                            OutlinedTextField(
                                                value = username,
                                                onValueChange = { username = it },
                                                label = { Text("Full Name") },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                                modifier = Modifier.fillMaxWidth().testTag("name_input"),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            label = { Text("Password") },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        if (!isSignUp) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Forgot Password?",
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .clickable { forgotPasswordMode = true }
                                                    .padding(vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Button(
                                            onClick = {
                                                val loginName = if (isSignUp) username else email.substringBefore("@")
                                                viewModel.login(email, loginName.ifEmpty { "Sarah Jenkins" })
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("submit_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = if (isSignUp) "Sign Up" else "Log In",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (isSignUp) "Log In" else "Sign Up",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.clickable { isSignUp = !isSignUp }
                                            )
                                        }
                                    }
                                    1 -> { // Google Login (Simulated Production Google Identity)
                                        Text(
                                            text = "Secure Google Sign-In",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Connect securely with your Google Workspace profile to immediately sync project repositories and verified status.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                viewModel.loginGoogle("Sarah Jenkins", "privateuserx533@gmail.com")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_login_button")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AccountCircle,
                                                    contentDescription = "Google Logo",
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Sign In with Google", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    2 -> { // Phone Login with simulated OTP verification
                                        if (!otpSent) {
                                            OutlinedTextField(
                                                value = phone,
                                                onValueChange = { phone = it },
                                                label = { Text("Phone Number") },
                                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                                placeholder = { Text("+1 (555) 000-0000") },
                                                modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Button(
                                                onClick = {
                                                    if (phone.isNotEmpty()) otpSent = true
                                                },
                                                modifier = Modifier.fillMaxWidth().testTag("send_otp_button"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Send OTP Code", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text(
                                                text = "OTP sent to $phone",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            OutlinedTextField(
                                                value = otpCode,
                                                onValueChange = { otpCode = it },
                                                label = { Text("Verification Code") },
                                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                                placeholder = { Text("6-digit code") },
                                                modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.loginPhone(phone)
                                                },
                                                modifier = Modifier.fillMaxWidth().testTag("verify_otp_button"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Verify & Login", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Resend Code",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { otpCode = "" }
                                                    .padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 13.sp
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

        // Verification notification popup
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { showVerificationDialog = false },
                title = { Text("Email Verification Sent") },
                text = { Text("A secure verification link has been sent to $email. Please check your inbox to activate your professional Prisim account.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showVerificationDialog = false
                            forgotPasswordMode = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun PrisimLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Subtle soft shadow under the squircle for a premium 3D elevation look
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(0f, h * 0.03f),
            size = androidx.compose.ui.geometry.Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.24f)
        )

        // 2. Pure white squircle background
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.24f)
        )

        // Subtle elegant border for high contrast on light backgrounds
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.05f),
            topLeft = Offset(0f, 0f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.24f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.01f)
        )

        // 3. Proportional Coordinates for the Folded P Ribbon
        val x1 = w * 0.32f // Left stem outer edge
        val x2 = w * 0.46f // Left stem inner edge
        val x3 = w * 0.68f // Inner triangle right point
        val x4 = w * 0.72f // Outer rightmost loop tip

        val y1 = h * 0.23f // Top outer edge
        val y2 = h * 0.30f // Inner triangle top-left point
        val y3 = h * 0.30f // Top-right outer loop tip
        val y4 = h * 0.42f // Inner triangle right tip
        val y5 = h * 0.46f // Outer rightmost loop tip
        val y6 = h * 0.54f // Inner triangle bottom-left point
        val y7 = h * 0.64f // Bottom-left outer stem curve start
        val y8 = h * 0.71f // Bottom-most point of stem hook

        // Facet 1: The Left Vertical Stem (Dark Slate/Deep Charcoal gradient)
        val stemPath = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
            lineTo(x2, y6)
            lineTo(x1, y7)
            close()
        }
        drawPath(
            path = stemPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2B2B2D), Color(0xFF141416)),
                start = Offset(x1, y1),
                end = Offset(x2, y6)
            )
        )

        // Facet 2: The Top Fold (Slightly lighter metallic charcoal gradient to simulate light reflection)
        val topFoldPath = Path().apply {
            moveTo(x1, y1)
            lineTo(x4, y3)
            lineTo(x3, y4)
            lineTo(x2, y2)
            close()
        }
        drawPath(
            path = topFoldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3F3F41), Color(0xFF1E1E20)),
                start = Offset(x1, y1),
                end = Offset(x4, y4)
            )
        )

        // Facet 3: The Outer Loop Curve / Lower Diagonal (Rich black gradient with subtle shadow)
        val loopPath = Path().apply {
            moveTo(x3, y4)
            lineTo(x4, y3)
            lineTo(x4, y5)
            lineTo(x2, y6)
            close()
        }
        drawPath(
            path = loopPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1B1B1C), Color(0xFF050505)),
                start = Offset(x4, y3),
                end = Offset(x2, y6)
            )
        )

        // Facet 4: The Bottom Stem Hook / Fold (Rich dark black with diagonal gradient wrapping up to stem base)
        val hookPath = Path().apply {
            moveTo(x1, y7)
            quadraticTo(x1, y8, x2 - (x2 - x1) * 0.2f, y8)
            lineTo(x2, y8)
            lineTo(x2, y6)
            close()
        }
        drawPath(
            path = hookPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0D0D0E), Color(0xFF020202)),
                start = Offset(x1, y7),
                end = Offset(x2, y8)
            )
        )
    }
}
