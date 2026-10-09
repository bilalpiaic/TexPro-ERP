package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material window-size breakpoints. Compact covers phone widths such as
 * Samsung A56 (~360–411 dp) where 3–4 equal columns clip currency.
 */
enum class WidthClass { Compact, Medium, Expanded }

val WidthClass.isCompact: Boolean
    get() = this == WidthClass.Compact

@Composable
fun rememberWidthClass(): WidthClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return remember(widthDp) {
        when {
            widthDp < 600 -> WidthClass.Compact
            widthDp < 840 -> WidthClass.Medium
            else -> WidthClass.Expanded
        }
    }
}

fun WidthClass.columns(compact: Int, medium: Int = compact + 1, expanded: Int = medium): Int = when (this) {
    WidthClass.Compact -> compact
    WidthClass.Medium -> medium
    WidthClass.Expanded -> expanded
}

/**
 * Equal-width rows that wrap to the next line. Last-row leftovers keep the
 * same cell width as full rows (trailing spacers) so amounts do not stretch.
 */
@Composable
fun AdaptiveGrid(
    itemCount: Int,
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    itemContent: @Composable (index: Int, modifier: Modifier) -> Unit
) {
    val cols = columns.coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        var i = 0
        while (i < itemCount) {
            val rowCount = minOf(cols, itemCount - i)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                for (j in 0 until rowCount) {
                    itemContent(i + j, Modifier.weight(1f))
                }
                if (rowCount < cols) {
                    repeat(cols - rowCount) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            i += rowCount
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

@Composable
fun FormPairRow(
    compact: Boolean = rememberWidthClass().isCompact,
    modifier: Modifier = Modifier,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit
) {
    if (compact) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            first(Modifier.fillMaxWidth())
            second(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            first(Modifier.weight(1f))
            second(Modifier.weight(1f))
        }
    }
}

@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = FontWeight.Bold,
    maxLines: Int = 1,
    textAlign: TextAlign? = TextAlign.End
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontWeight = fontWeight ?: style.fontWeight,
            fontFeatureSettings = "tnum"
        ),
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        softWrap = maxLines > 1
    )
}

@Composable
fun MetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DebitCreditPair(
    debit: String,
    credit: String,
    debitColor: Color,
    creditColor: Color,
    compact: Boolean = rememberWidthClass().isCompact,
    modifier: Modifier = Modifier
) {
    val amountMin = if (compact) 76.dp else 100.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AmountText(
            text = debit,
            modifier = Modifier.widthIn(min = amountMin),
            style = MaterialTheme.typography.bodySmall,
            color = debitColor
        )
        AmountText(
            text = credit,
            modifier = Modifier.widthIn(min = amountMin),
            style = MaterialTheme.typography.bodySmall,
            color = creditColor
        )
    }
}

@Composable
fun LabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false,
    isHeadline: Boolean = false
) {
    val labelStyle = when {
        isHeadline -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        isBold -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.bodyMedium
    }
    val valueStyle = when {
        isHeadline -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        isBold -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 0.dp),
            style = labelStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AmountText(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = 2
        )
    }
}
