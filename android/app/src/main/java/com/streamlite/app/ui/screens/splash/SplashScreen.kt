package com.streamlite.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.streamlite.app.ui.navigation.NavRoutes
import com.streamlite.app.ui.theme.PremiumBlue
import com.streamlite.app.ui.theme.PremiumRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, tween(1000))
        delay(2000)
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1A1A1A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Red and Blue Gradient Lighting Effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f)
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PremiumRed.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 50.dp, y = 50.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PremiumBlue.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = PremiumRed)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PremiumRed, PremiumBlue)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "STREAM LITE",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 8.sp
                )
            )
            
            Text(
                text = "Premium Media Experience",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
            )
        }
    }
}
