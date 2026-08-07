package org.rocs.osda.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.rocs.osda.mobile.ui.theme.OsdaTokens


@Composable
fun ScreenHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
    )
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun StatCard(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OsdaTokens.cardRadius))
            .padding(vertical = 14.dp, horizontal = 6.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

@Composable
fun StatusPill(text: String, fg: Color, bg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(OsdaTokens.pillRadius))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = fg, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(OsdaTokens.pillRadius))
            .let { if (!selected) it.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OsdaTokens.pillRadius)) else it }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.5f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), RoundedCornerShape(OsdaTokens.buttonRadius))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OsdaCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(OsdaTokens.cardRadius))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(OsdaTokens.cardRadius))
            .padding(14.dp),
        content = content
    )
}

@Composable
fun InitialsBadge(initials: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(OsdaTokens.primaryMuted.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}

object StatusColors {
    fun forRecord(status: String): Pair<Color, Color> = when (status.uppercase()) {
        "RESOLVED" -> OsdaTokens.green to OsdaTokens.greenBg
        "APPEALED" -> OsdaTokens.amber to OsdaTokens.amberBg
        else -> OsdaTokens.amber to OsdaTokens.amberBg // PENDING and anything else reads as "active"
    }

    fun forAppeal(status: String): Pair<Color, Color> = when (status.uppercase()) {
        "APPROVED" -> OsdaTokens.green to OsdaTokens.greenBg
        "DENIED" -> OsdaTokens.red to OsdaTokens.redBg
        else -> OsdaTokens.amber to OsdaTokens.amberBg // PENDING, UNDER_REVIEW
    }
}