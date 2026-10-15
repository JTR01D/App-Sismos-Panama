package com.sismos.panama.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sismos.panama.data.models.PanamaRegionInfo
import com.sismos.panama.data.models.PanamaRegionsCatalog
import com.sismos.panama.ui.theme.PrimaryBlue

@Composable
fun PanamaRegionsBanner(
    selectedRegion: String?,
    onSelectRegion: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ZONAS SÍSMICAS DE PANAMÁ",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
            if (selectedRegion != null) {
                Text(
                    text = "Restablecer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onSelectRegion(null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            items(PanamaRegionsCatalog.regions) { reg ->
                val isSelected = selectedRegion == reg.name
                RegionGlassChip(
                    region = reg,
                    isSelected = isSelected,
                    onClick = {
                        onSelectRegion(if (isSelected) null else reg.name)
                    }
                )
            }
        }
    }
}

@Composable
fun RegionGlassChip(
    region: PanamaRegionInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected) {
        Brush.verticalGradient(
            listOf(
                Color(0x553186FF),
                Color(0x221E3A8A)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0x1F253A5A),
                Color(0x120B1320)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.verticalGradient(listOf(Color(0xFF60A5FA), PrimaryBlue))
    } else {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Column {
            Text(
                text = region.shortName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF93C5FD) else Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${region.percentage}% actividad nacional",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) Color(0xFFBAE6FD) else Color(0xFF94A3B8)
            )
        }
    }
}
