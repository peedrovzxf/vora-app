package com.peedrovzxf.vora.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val isDarkMode  by viewModel.isDarkMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()

    val accentOptions = listOf(
        "default" to Color(0xFF6650A4) to "Purple",
        "red"     to Color(0xFFE53935) to "Red",
        "blue"    to Color(0xFF1E88E5) to "Blue",
        "green"   to Color(0xFF43A047) to "Green",
        "orange"  to Color(0xFFFB8C00) to "Orange",
        "pink"    to Color(0xFFE91E63) to "Pink",
        "teal"    to Color(0xFF00897B) to "Teal",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text     = "Settings",
            style    = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 20.dp, bottom = 24.dp)
        )

        SectionLabel("Appearance")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text  = "Dark mode",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text  = if (isDarkMode) "On" else "Off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked         = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Accent color")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(22.dp)
                    )
                    Text(
                        text  = "Choose accent",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val chunked = accentOptions.chunked(4)
                chunked.forEach { row ->
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { (keyColor, label) ->
                            val (key, color) = keyColor
                            val isSelected   = accentColor == key

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier            = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAccentColor(key) }
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected)
                                                Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else
                                                Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector        = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint               = Color.White,
                                            modifier           = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text  = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        val remainder = 4 - row.size
                        repeat(remainder) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text     = "Vora · v1.0",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        content()
    }
}
