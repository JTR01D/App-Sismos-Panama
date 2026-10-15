package com.sismos.panama.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.Screen
import com.sismos.panama.ui.theme.PrimaryBlue

enum class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color
) {
    HOME("Sismos", Icons.Filled.Home, Icons.Outlined.Home, PrimaryBlue),
    MAP("Explorador", Icons.Filled.Explore, Icons.Outlined.Explore, Color(0xFF00E5FF)),
    DASHBOARD("Dashboard", Icons.Filled.Equalizer, Icons.Outlined.Equalizer, Color(0xFF38BDF8)),
    FAULTS("Fallas", Icons.Filled.Layers, Icons.Outlined.Layers, Color(0xFFA78BFA))
}

@Composable
fun PanamaBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val barBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xEE111827),
            Color(0xFA0B0F19)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(barBrush)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x4038BDF8), Color(0x15FFFFFF))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem.values().forEach { item ->
                    val isSelected = when (item) {
                        BottomNavItem.HOME -> currentScreen is Screen.Home
                        BottomNavItem.MAP -> currentScreen is Screen.Map
                        BottomNavItem.DASHBOARD -> currentScreen is Screen.Dashboard
                        BottomNavItem.FAULTS -> currentScreen is Screen.Faults
                    }

                    val activeBgColor by animateColorAsState(
                        targetValue = if (isSelected) item.accentColor.copy(alpha = 0.16f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "navBg"
                    )

                    val activeContentColor by animateColorAsState(
                        targetValue = if (isSelected) item.accentColor else Color(0xFF94A3B8),
                        animationSpec = tween(durationMillis = 200),
                        label = "navContent"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(activeBgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = item.accentColor)
                            ) {
                                when (item) {
                                    BottomNavItem.HOME -> onNavigate(Screen.Home)
                                    BottomNavItem.MAP -> onNavigate(Screen.Map())
                                    BottomNavItem.DASHBOARD -> onNavigate(Screen.Dashboard)
                                    BottomNavItem.FAULTS -> onNavigate(Screen.Faults)
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = activeContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = activeContentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
