package com.zayaai.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import java.util.regex.Pattern

// --- Syntax Highlighting Colors ---
private object SyntaxColors {
    val keyword = Color(0xFFC678DD)       // purple — keywords
    val string = Color(0xFF98C379)        // green — strings
    val comment = Color(0xFF5C6370)       // gray — comments
    val function = Color(0xFF61AFEF)      // blue — function names
    val number = Color(0xFFD19A66)        // orange — numbers
    val type = Color(0xFFE5C07B)          // yellow — types/classes
    val operator = Color(0xFF56B6C2)      // cyan — operators
    val annotation = Color(0xFFE06C75)    // red — annotations
    val plain = Color(0xFFABB2BF)         // light gray — plain text
}

// --- Markdown Block Models ---
sealed class MdBlock {
    data class Header(val level: Int, val content: String) : MdBlock()
    data class CodeBlock(val code: String, val language: String = "") : MdBlock()
    data class Table(
        val headers: List<String>,
        val alignments: List<TextAlign>,
        val rows: List<List<String>>
    ) : MdBlock()
    data class Blockquote(val content: String) : MdBlock()
    data class ListItem(
        val bullet: String,
        val content: String,
        val isTaskList: Boolean = false,
        val isChecked: Boolean = false
    ) : MdBlock()
    data class MathBlock(val formula: String) : MdBlock()
    data class Paragraph(val content: String) : MdBlock()
    data object Divider : MdBlock()
}

/**
 * A premier, ChatGPT-grade Markdown renderer for AI ZAYA.
 * Supports:
 * - Headings (H1 - H6) with clean typographic hierarchy
 * - GitHub Flavored Markdown Tables with horizontal scrolling and zebra rows
 * - Fenced code blocks with language pills, syntax highlighting, and copy button
 * - Blockquotes with accent border and elegant background
 * - Bullet lists, numbered lists, and task checkboxes
 * - LaTeX / Math formula display blocks & inline math
 * - Inline formatting: bold, italic, strikethrough, inline code pills, clickable links
 * - Resilient streaming response parsing (handles partial Markdown safely)
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    lineHeight: TextUnit = 24.sp
) {
    val isDark = LocalIsDarkTheme.current
    val uriHandler = LocalUriHandler.current
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Header -> {
                    val (fontSize, fontWeight, topPad, botPad) = when (block.level) {
                        1 -> listOf(21.sp, FontWeight.Bold, 12.dp, 4.dp)
                        2 -> listOf(18.sp, FontWeight.Bold, 10.dp, 4.dp)
                        3 -> listOf(16.sp, FontWeight.SemiBold, 8.dp, 2.dp)
                        else -> listOf(15.sp, FontWeight.SemiBold, 6.dp, 2.dp)
                    }

                    val headerColor = if (isDark) {
                        when (block.level) {
                            1 -> Color(0xFFFFFFFF)
                            2 -> Color(0xFFF1F5F9)
                            else -> Color(0xFFE2E8F0)
                        }
                    } else {
                        when (block.level) {
                            1 -> Color(0xFF0F172A)
                            2 -> Color(0xFF1E293B)
                            else -> Color(0xFF334155)
                        }
                    }

                    Spacer(modifier = Modifier.height(topPad as androidx.compose.ui.unit.Dp))
                    val annotatedHeader = remember(block.content, headerColor, isDark) {
                        parseInlineMarkdown(block.content, headerColor, isDark)
                    }
                    ClickableText(
                        text = annotatedHeader,
                        style = TextStyle(
                            fontSize = fontSize as TextUnit,
                            fontWeight = fontWeight as FontWeight,
                            color = headerColor,
                            lineHeight = ((fontSize as TextUnit).value * 1.35f).sp
                        ),
                        onClick = { offset ->
                            annotatedHeader.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    try { uriHandler.openUri(annotation.item) } catch (_: Exception) {}
                                }
                        }
                    )
                    Spacer(modifier = Modifier.height(botPad as androidx.compose.ui.unit.Dp))
                }

                is MdBlock.Table -> {
                    MarkdownTable(
                        headers = block.headers,
                        alignments = block.alignments,
                        rows = block.rows,
                        isDark = isDark
                    )
                }

                is MdBlock.CodeBlock -> {
                    SyntaxHighlightedCodeBlock(
                        code = block.code,
                        language = block.language,
                        isDark = isDark
                    )
                }

                is MdBlock.Blockquote -> {
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                        color = if (isDark) Color(0xFF16182B) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF7C3AED))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            val quoteColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            val annotatedQuote = remember(block.content, quoteColor, isDark) {
                                parseInlineMarkdown(block.content, quoteColor, isDark)
                            }
                            ClickableText(
                                text = annotatedQuote,
                                style = TextStyle(
                                    fontSize = 14.5.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = quoteColor,
                                    lineHeight = 22.sp
                                ),
                                onClick = { offset ->
                                    annotatedQuote.getStringAnnotations("URL", offset, offset)
                                        .firstOrNull()?.let { annotation ->
                                            try { uriHandler.openUri(annotation.item) } catch (_: Exception) {}
                                        }
                                }
                            )
                        }
                    }
                }

                is MdBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = if (isDark) Color(0xFF2E314D) else Color(0xFFE2E8F0),
                        thickness = 1.dp
                    )
                }

                is MdBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (block.isTaskList) {
                            Icon(
                                imageVector = if (block.isChecked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                contentDescription = if (block.isChecked) "Completed" else "Incomplete",
                                tint = if (block.isChecked) Color(0xFF10B981) else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(19.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Text(
                                text = block.bullet,
                                color = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.width(if (block.bullet.length > 2) 26.dp else 18.dp)
                            )
                        }

                        val itemColor = if (block.isTaskList && block.isChecked) {
                            if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        } else color

                        val annotatedItem = remember(block.content, itemColor, isDark) {
                            parseInlineMarkdown(block.content, itemColor, isDark)
                        }
                        ClickableText(
                            text = annotatedItem,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                color = itemColor,
                                lineHeight = lineHeight
                            ),
                            onClick = { offset ->
                                annotatedItem.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        try { uriHandler.openUri(annotation.item) } catch (_: Exception) {}
                                    }
                            }
                        )
                    }
                }

                is MdBlock.MathBlock -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF131525) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = block.formula,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }
                    }
                }

                is MdBlock.Paragraph -> {
                    if (block.content.isNotBlank()) {
                        val annotatedPara = remember(block.content, color, isDark) {
                            parseInlineMarkdown(block.content, color, isDark)
                        }
                        ClickableText(
                            text = annotatedPara,
                            modifier = Modifier.fillMaxWidth(),
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                color = color,
                                lineHeight = lineHeight
                            ),
                            onClick = { offset ->
                                annotatedPara.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        try { uriHandler.openUri(annotation.item) } catch (_: Exception) {}
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- Structured Table Composable ---

@Composable
private fun MarkdownTable(
    headers: List<String>,
    alignments: List<TextAlign>,
    rows: List<List<String>>,
    isDark: Boolean
) {
    val scrollState = rememberScrollState()
    val borderColor = if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)
    val headerBg = if (isDark) Color(0xFF1C1E34) else Color(0xFFF1F5F9)
    val zebraBg = if (isDark) Color(0xFF141629) else Color(0xFFF8FAFC)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) Color(0xFF131524) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headers.forEachIndexed { colIndex, header ->
                        val align = alignments.getOrElse(colIndex) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 280.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = when (align) {
                                TextAlign.Center -> Alignment.Center
                                TextAlign.End -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = parseInlineMarkdown(header, textPrimary, isDark),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp
                                ),
                                textAlign = align
                            )
                        }
                        if (colIndex < headers.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(38.dp)
                                    .background(borderColor)
                            )
                        }
                    }
                }

                HorizontalDivider(color = borderColor, thickness = 1.dp)

                // Data Rows with Zebra Striping
                rows.forEachIndexed { rowIndex, row ->
                    val isZebra = rowIndex % 2 == 1
                    val rowBg = if (isZebra) zebraBg else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headers.indices.forEach { colIndex ->
                            val cellText = row.getOrElse(colIndex) { "" }
                            val align = alignments.getOrElse(colIndex) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 120.dp, max = 280.dp)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = when (align) {
                                    TextAlign.Center -> Alignment.Center
                                    TextAlign.End -> Alignment.CenterEnd
                                    else -> Alignment.CenterStart
                                }
                            ) {
                                Text(
                                    text = parseInlineMarkdown(cellText, textSecondary, isDark),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp
                                    ),
                                    textAlign = align
                                )
                            }
                            if (colIndex < headers.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(36.dp)
                                        .background(borderColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                    if (rowIndex < rows.size - 1) {
                        HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// --- Syntax Highlighted Code Block with Copy & Expand ---

@Composable
private fun SyntaxHighlightedCodeBlock(
    code: String,
    language: String,
    isDark: Boolean
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val lineCount = remember(code) { code.lines().size }
    val isLong = lineCount > 25
    var isExpanded by remember { mutableStateOf(!isLong) }
    val displayCode = if (isExpanded || !isLong) code else code.lines().take(18).joinToString("\n") + "\n..."

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141624),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFF2D304D)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Header bar with language label + copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2138))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    color = Color(0xFFA5B4FC),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    fontSize = 11.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLong) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Copy button with feedback
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                                isCopied = true
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF10B981) else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied" else "Copy",
                            color = if (isCopied) Color(0xFF10B981) else Color(0xFF94A3B8),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            // Code content with syntax highlighting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = highlightSyntax(displayCode, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        fontSize = 13.sp
                    )
                )
            }

            // Expand hint
            if (isLong && !isExpanded) {
                Text(
                    text = "  ▾ Show ${lineCount - 18} more lines",
                    color = Color(0xFF818CF8),
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// --- Syntax Highlighting Engine ---

private fun highlightSyntax(code: String, language: String): AnnotatedString {
    val lang = language.lowercase().trim()
    return buildAnnotatedString {
        code.lines().forEachIndexed { index, line ->
            if (index > 0) append("\n")
            highlightLine(line, lang)
        }
    }
}

private fun AnnotatedString.Builder.highlightLine(line: String, lang: String) {
    val trimmed = line.trimStart()

    // Comment detection
    if (isComment(trimmed, lang)) {
        withStyle(SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)) {
            append(line)
        }
        return
    }

    var i = 0
    val len = line.length

    while (i < len) {
        when {
            // Strings: "..." or '...'
            line[i] == '"' || line[i] == '\'' -> {
                val quote = line[i]
                val start = i
                i++
                while (i < len && line[i] != quote) {
                    if (line[i] == '\\' && i + 1 < len) i++ // skip escaped char
                    i++
                }
                if (i < len) i++ // closing quote
                withStyle(SpanStyle(color = SyntaxColors.string)) {
                    append(line.substring(start, i))
                }
            }

            // Triple-quote strings (Python, Kotlin)
            i + 2 < len && line.substring(i, i + 3) == "\"\"\"" -> {
                withStyle(SpanStyle(color = SyntaxColors.string)) {
                    append(line.substring(i))
                }
                i = len
            }

            // Inline comment // or #
            (i + 1 < len && line.substring(i, i + 2) == "//") ||
            (line[i] == '#' && lang in listOf("python", "py", "ruby", "rb", "bash", "sh", "yaml", "yml")) -> {
                withStyle(SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)) {
                    append(line.substring(i))
                }
                i = len
            }

            // Annotations: @Something
            line[i] == '@' && i + 1 < len && line[i + 1].isLetter() -> {
                val start = i
                i++
                while (i < len && (line[i].isLetterOrDigit() || line[i] == '_')) i++
                withStyle(SpanStyle(color = SyntaxColors.annotation)) {
                    append(line.substring(start, i))
                }
            }

            // Numbers
            line[i].isDigit() && (i == 0 || !line[i - 1].isLetterOrDigit()) -> {
                val start = i
                while (i < len && (line[i].isDigit() || line[i] == '.' || line[i] == 'f' || line[i] == 'L' || line[i] == 'x')) i++
                withStyle(SpanStyle(color = SyntaxColors.number)) {
                    append(line.substring(start, i))
                }
            }

            // Identifiers / Keywords
            line[i].isLetter() || line[i] == '_' -> {
                val start = i
                while (i < len && (line[i].isLetterOrDigit() || line[i] == '_')) i++
                val word = line.substring(start, i)

                val wordColor = when {
                    isKeyword(word, lang) -> SyntaxColors.keyword
                    isBuiltinType(word, lang) -> SyntaxColors.type
                    isBuiltinFunction(word, lang) -> SyntaxColors.function
                    i < len && line[i] == '(' -> SyntaxColors.function
                    word.isNotEmpty() && word.first().isUpperCase() -> SyntaxColors.type
                    else -> SyntaxColors.plain
                }

                withStyle(SpanStyle(color = wordColor)) {
                    append(word)
                }
            }

            // Operators
            line[i] in "+-*/%=<>!&|^~?:" -> {
                withStyle(SpanStyle(color = SyntaxColors.operator)) {
                    append(line[i])
                }
                i++
            }

            else -> {
                append(line[i])
                i++
            }
        }
    }
}

private fun isComment(trimmed: String, lang: String): Boolean {
    return trimmed.startsWith("//") ||
            trimmed.startsWith("/*") ||
            trimmed.startsWith("*") ||
            (trimmed.startsWith("#") && lang in listOf("python", "py", "ruby", "rb", "bash", "sh", "yaml", "yml", "r", "dockerfile")) ||
            (trimmed.startsWith("--") && lang in listOf("sql", "lua", "haskell"))
}

private fun isKeyword(word: String, lang: String): Boolean {
    val commonKeywords = setOf(
        "if", "else", "for", "while", "do", "return", "break", "continue",
        "switch", "case", "default", "try", "catch", "finally", "throw",
        "new", "this", "super", "null", "true", "false", "import", "package",
        "as", "in", "is", "when", "by", "val", "var", "fun", "class", "object",
        "interface", "abstract", "override", "public", "private", "protected",
        "const", "let", "def", "lambda", "yield", "async", "await", "select",
        "from", "where", "insert", "update", "delete", "join", "group", "order"
    )
    return word in commonKeywords
}

private fun isBuiltinType(word: String, lang: String): Boolean {
    val types = setOf(
        "Int", "Long", "Float", "Double", "Boolean", "String", "Char", "Byte",
        "Short", "Unit", "Any", "List", "Map", "Set", "Array", "int", "float",
        "double", "bool", "boolean", "char", "void", "object", "number", "string"
    )
    return word in types
}

private fun isBuiltinFunction(word: String, lang: String): Boolean {
    val functions = setOf(
        "println", "print", "listOf", "mapOf", "setOf", "remember", "mutableStateOf",
        "LaunchedEffect", "len", "range", "console", "log", "fetch", "JSON"
    )
    return word in functions
}

// --- Block-Level Markdown Parser ---

private val tableDividerRegex = Pattern.compile("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)+\\|?\\s*$")

private fun parseMarkdownBlocks(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = text.lines()
    var i = 0
    val total = lines.size

    while (i < total) {
        val line = lines[i]
        val trimmed = line.trimStart()

        when {
            // Empty line
            trimmed.isBlank() -> {
                i++
            }

            // Code block: ```
            trimmed.startsWith("```") -> {
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < total && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MdBlock.CodeBlock(codeLines.joinToString("\n"), lang))
                if (i < total) i++ // skip closing ```
            }

            // LaTeX display math: $$ formula $$
            trimmed.startsWith("$$") -> {
                val mathContent = trimmed.removePrefix("$$").trim()
                if (mathContent.endsWith("$$") && mathContent.length > 2) {
                    blocks.add(MdBlock.MathBlock(mathContent.removeSuffix("$$").trim()))
                    i++
                } else {
                    val mathLines = mutableListOf<String>()
                    if (mathContent.isNotBlank()) mathLines.add(mathContent)
                    i++
                    while (i < total && !lines[i].trim().endsWith("$$")) {
                        mathLines.add(lines[i])
                        i++
                    }
                    if (i < total) {
                        val closeLine = lines[i].trim().removeSuffix("$$").trim()
                        if (closeLine.isNotBlank()) mathLines.add(closeLine)
                        i++
                    }
                    blocks.add(MdBlock.MathBlock(mathLines.joinToString("\n")))
                }
            }

            // Divider: --- or *** or ___
            trimmed.matches(Regex("^[-*_]{3,}$")) -> {
                blocks.add(MdBlock.Divider)
                i++
            }

            // Table detection
            "|" in line && i + 1 < total && tableDividerRegex.matcher(lines[i + 1].trim()).matches() -> {
                val headerCells = line.trim().trim('|').split('|').map { it.trim() }
                val sepCells = lines[i + 1].trim().trim('|').split('|').map { it.trim() }

                val alignments = sepCells.map { cell ->
                    when {
                        cell.startsWith(":") && cell.endsWith(":") -> TextAlign.Center
                        cell.endsWith(":") -> TextAlign.End
                        else -> TextAlign.Start
                    }
                }

                i += 2
                val rows = mutableListOf<List<String>>()
                while (i < total) {
                    val rowLine = lines[i].trim()
                    if (rowLine.isBlank() || "|" !in rowLine || rowLine.startsWith("#") || rowLine.startsWith("```")) {
                        break
                    }
                    val rowCells = rowLine.trim('|').split('|').map { it.trim() }.toMutableList()
                    if (rowCells.size < headerCells.size) {
                        while (rowCells.size < headerCells.size) rowCells.add("")
                    } else if (rowCells.size > headerCells.size) {
                        rowCells.subList(headerCells.size, rowCells.size).clear()
                    }
                    rows.add(rowCells)
                    i++
                }
                blocks.add(MdBlock.Table(headerCells, alignments, rows))
            }

            // Blockquote: > or >>>
            trimmed.startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < total && lines[i].trimStart().startsWith(">")) {
                    quoteLines.add(lines[i].trimStart().replaceFirst(Regex("^>+\\s?"), ""))
                    i++
                }
                blocks.add(MdBlock.Blockquote(quoteLines.joinToString("\n")))
            }

            // Headers: # ## ###
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(6)
                val content = trimmed.drop(level).trimStart()
                blocks.add(MdBlock.Header(level, content))
                i++
            }

            // Task list: - [ ] or - [x]
            trimmed.matches(Regex("^[-*•]\\s+\\[[ xX]\\]\\s+.*")) -> {
                val isChecked = trimmed.contains(Regex("\\[[xX]\\]"))
                val content = trimmed.replaceFirst(Regex("^[-*•]\\s+\\[[ xX]\\]\\s+"), "")
                blocks.add(MdBlock.ListItem(bullet = "", content = content, isTaskList = true, isChecked = isChecked))
                i++
            }

            // Bullet list: - or * or • or +
            trimmed.matches(Regex("^[-*•+]\\s+.*")) -> {
                val content = trimmed.replaceFirst(Regex("^[-*•+]\\s+"), "")
                blocks.add(MdBlock.ListItem(bullet = "•", content = content))
                i++
            }

            // Numbered list: 1. 2. etc.
            trimmed.matches(Regex("^\\d+[.):]\\s+.*")) -> {
                val num = trimmed.takeWhile { it.isDigit() }
                val content = trimmed.replaceFirst(Regex("^\\d+[.):]\\s+"), "")
                blocks.add(MdBlock.ListItem(bullet = "$num.", content = content))
                i++
            }

            // Regular paragraph
            else -> {
                val paraLines = mutableListOf(line)
                i++
                while (i < total) {
                    val nextLine = lines[i].trimStart()
                    if (nextLine.isBlank() ||
                        nextLine.startsWith("#") ||
                        nextLine.startsWith("```") ||
                        nextLine.startsWith(">") ||
                        nextLine.startsWith("$$") ||
                        nextLine.matches(Regex("^[-*•+]\\s+.*")) ||
                        nextLine.matches(Regex("^\\d+[.):]\\s+.*")) ||
                        nextLine.matches(Regex("^[-*_]{3,}$")) ||
                        ("|" in nextLine && i + 1 < total && tableDividerRegex.matcher(lines[i + 1].trim()).matches())
                    ) break
                    paraLines.add(lines[i])
                    i++
                }
                blocks.add(MdBlock.Paragraph(paraLines.joinToString(" ")))
            }
        }
    }

    return blocks
}

// --- Inline Markdown Parser ---

private fun parseInlineMarkdown(text: String, defaultColor: Color, isDark: Boolean): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            when {
                // Markdown link: [Title](https://...)
                text[i] == '[' -> {
                    val closeBracket = text.indexOf(']', i + 1)
                    val openParen = if (closeBracket != -1) text.indexOf('(', closeBracket) else -1
                    val closeParen = if (openParen == closeBracket + 1) text.indexOf(')', openParen) else -1

                    if (closeBracket != -1 && openParen == closeBracket + 1 && closeParen != -1) {
                        val title = text.substring(i + 1, closeBracket)
                        val url = text.substring(openParen + 1, closeParen)
                        val linkColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)

                        val start = length
                        pushStringAnnotation(tag = "URL", annotation = url)
                        withStyle(
                            SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(title)
                        }
                        pop()
                        i = closeParen + 1
                    } else {
                        append("[")
                        i++
                    }
                }

                // Bold + Italic: ***text*** or ___text___
                i + 2 < len && (text.substring(i, i + 3) == "***" || text.substring(i, i + 3) == "___") -> {
                    val marker = text.substring(i, i + 3)
                    val end = text.indexOf(marker, i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = defaultColor)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(marker[0])
                        i++
                    }
                }

                // Bold: **text** or __text__
                i + 1 < len && (text.substring(i, i + 2) == "**" || text.substring(i, i + 2) == "__") -> {
                    val marker = text.substring(i, i + 2)
                    val end = text.indexOf(marker, i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(marker[0])
                        i++
                    }
                }

                // Inline code: `code`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        val codeBg = if (isDark) Color(0xFF23253E) else Color(0xFFEDE9FE)
                        val codeColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = codeColor,
                                background = codeBg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append("`")
                        i++
                    }
                }

                // Inline math: $formula$ (ignoring currency like $2,500)
                text[i] == '$' && i + 1 < len && !text[i + 1].isDigit() && text[i + 1] != ' ' -> {
                    val end = text.indexOf('$', i + 1)
                    if (end != -1 && end > i + 1) {
                        val mathColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                                color = mathColor,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append("$")
                        i++
                    }
                }

                // Italic: *text* or _text_
                (text[i] == '*' || text[i] == '_') && i + 1 < len && text[i + 1] != ' ' -> {
                    val marker = text[i]
                    val end = text.indexOf(marker, i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(marker)
                        i++
                    }
                }

                // Strikethrough: ~~text~~
                i + 1 < len && text.substring(i, i + 2) == "~~" -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = defaultColor.copy(alpha = 0.7f))) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("~")
                        i++
                    }
                }

                // Regular character
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
