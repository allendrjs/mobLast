package org.rocs.osda.mobile.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders a (small) subset of markdown -- the subset the chatbot's system
 * prompt actually produces -- as real Compose UI instead of dumping raw
 * "**", "|", "---" characters into a plain Text like before. Deliberately
 * not a general-purpose markdown engine: just enough to cover bold/italic/
 * inline code, headings, bullet lists, and pipe tables, which is everything
 * the model has been observed to emit.
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = inlineAnnotatedString(block.text),
                    color = color,
                    style = style.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (style.fontSize.value * headingScale(block.level)).sp
                    )
                )

                is MarkdownBlock.Paragraph -> Text(
                    text = inlineAnnotatedString(block.text),
                    color = color,
                    style = style
                )

                is MarkdownBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEach { item ->
                        Row {
                            Text("•  ", color = color, style = style)
                            Text(inlineAnnotatedString(item), color = color, style = style)
                        }
                    }
                }

                is MarkdownBlock.Table -> MarkdownTable(block.rows, color, style)
            }
        }
    }
}

private fun headingScale(level: Int): Float = when (level) {
    1 -> 1.25f
    2 -> 1.15f
    else -> 1.05f
}

@Composable
private fun MarkdownTable(rows: List<List<String>>, color: Color, style: TextStyle) {
    val columnCount = rows.maxOf { it.size }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val isHeader = rowIndex == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isHeader) color.copy(alpha = 0.08f) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                for (columnIndex in 0 until columnCount) {
                    Text(
                        text = inlineAnnotatedString(row.getOrNull(columnIndex).orEmpty()),
                        color = color,
                        style = style.copy(
                            fontSize = (style.fontSize.value * 0.92f).sp,
                            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                }
            }
            if (rowIndex < rows.lastIndex) {
                HorizontalDivider(color = color.copy(alpha = 0.12f))
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
    data class Table(val rows: List<List<String>>) : MarkdownBlock()
}

private val bulletRegex = Regex("""^\s*([-*+]|\d+\.)\s+(.*)$""")

private fun isTableRow(line: String) = line.trim().startsWith("|")

private fun isSeparatorRow(line: String) =
    line.trim().trim('|').split("|").all { it.trim().matches(Regex("""^:?-+:?$""")) }

private fun parseTableRow(line: String): List<String> =
    line.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        when {
            line.isBlank() -> i++

            isTableRow(line) -> {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && isTableRow(lines[i])) {
                    tableLines.add(lines[i])
                    i++
                }
                val rows = tableLines.filterNot(::isSeparatorRow).map(::parseTableRow)
                if (rows.isNotEmpty()) blocks.add(MarkdownBlock.Table(rows))
            }

            line.trimStart().startsWith("#") -> {
                val trimmed = line.trimStart()
                val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 6)
                blocks.add(MarkdownBlock.Heading(level, trimmed.dropWhile { it == '#' }.trim()))
                i++
            }

            bulletRegex.matches(line) -> {
                val items = mutableListOf<String>()
                while (i < lines.size && bulletRegex.matches(lines[i])) {
                    items.add(bulletRegex.find(lines[i])!!.groupValues[2].trim())
                    i++
                }
                blocks.add(MarkdownBlock.BulletList(items))
            }

            else -> {
                val paragraphLines = mutableListOf<String>()
                while (i < lines.size && lines[i].isNotBlank() &&
                    !isTableRow(lines[i]) &&
                    !lines[i].trimStart().startsWith("#") &&
                    !bulletRegex.matches(lines[i])
                ) {
                    paragraphLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ").trim()))
            }
        }
    }

    return blocks
}

private val inlineRegex = Regex("""\*\*(.+?)\*\*|`(.+?)`|\*(.+?)\*""")

private fun inlineAnnotatedString(text: String): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    for (match in inlineRegex.findAll(text)) {
        if (match.range.first > lastIndex) append(text.substring(lastIndex, match.range.first))
        val bold = match.groups[1]?.value
        val code = match.groups[2]?.value
        val italic = match.groups[3]?.value
        when {
            bold != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
            code != null -> withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Black.copy(alpha = 0.06f))
            ) { append(code) }

            italic != null -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italic) }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}
