package com.example.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.SoftGreyText
import com.example.ui.theme.TechPurple

@Composable
fun MediaRenderer(
    mediaName: String,
    modifier: Modifier = Modifier,
    isAvatar: Boolean = false
) {
    if (isAvatar) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = when (mediaName) {
                            "avatar_alex" -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
                            "avatar_sophia" -> listOf(Color(0xFF10B981), Color(0xFF3B82F6))
                            "avatar_marcus" -> listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            else -> listOf(Color(0xFF64748B), Color(0xFF475569))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val letter = when (mediaName) {
                "avatar_alex" -> "A"
                "avatar_sophia" -> "S"
                "avatar_marcus" -> "M"
                else -> "SJ"
            }
            Text(
                text = letter,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    } else {
        // Render rich, colorful abstract card depending on post/moment ID
        Box(
            modifier = modifier
                .background(
                    Brush.linearGradient(
                        colors = when (mediaName) {
                            "post_design_1" -> listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            "post_design_2" -> listOf(Color(0xFF1E1B4B), Color(0xFF311042))
                            "post_code_1" -> listOf(Color(0xFF022C22), Color(0xFF064E3B))
                            "moment_design_sprint" -> listOf(Color(0xFF430F54), Color(0xFF641E82))
                            "moment_code_review" -> listOf(Color(0xFF0F4C5C), Color(0xFF1F5F7A))
                            "moment_scrum_board" -> listOf(Color(0xFF5C3D2E), Color(0xFF865439))
                            "video_interaction_demo" -> listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                            "video_compose_animation" -> listOf(Color(0xFF000000), Color(0xFF1C0A35))
                            else -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Overlay geometric grid or icons
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw technical grid lines
                for (i in 1..8) {
                    val x = (w / 8) * i
                    val y = (h / 8) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw premium abstract vector circles
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SleekCyan.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.25f, h * 0.35f),
                        radius = w * 0.40f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TechPurple.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.75f, h * 0.65f),
                        radius = w * 0.45f
                    )
                )
            }

            // Central icon/title based on post
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                val (icon, title, subtitle) = when (mediaName) {
                    "post_design_1" -> Triple(Icons.Default.Palette, "Figma Design Sprint", "v2.4 - Mobile Systems")
                    "post_design_2" -> Triple(Icons.Default.IntegrationInstructions, "Dynamic Prototypes", "Micro-interactions")
                    "post_code_1" -> Triple(Icons.Default.Code, "Clean Architecture", "MVVM + Coroutine Flow")
                    "video_interaction_demo" -> Triple(Icons.Default.Speed, "Prototype Motion", "60 FPS Anim")
                    "video_compose_animation" -> Triple(Icons.Default.Code, "Compose Custom Canvas", "Particle simulation")
                    else -> Triple(Icons.Default.Palette, "Creative Highlight", "Collaborative Board")
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SleekCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = subtitle,
                    color = SoftGreyText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
