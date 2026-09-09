package com.zayaai.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan

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

/**
 * A composable that renders markdown-formatted text with proper styling.
 * Supports: **bold**, *italic*, `inline code`, ### headers, bullet lists,
 * numbered lists, ---, code blocks with syntax highlighting, copy & expand.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    lineHeight: androidx.compose.ui.unit.TextUnit = 22.sp
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Header -> {
                    val (style, headerColor) = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold) to NeonCyan
                        2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) to NeonCyan.copy(alpha = 0.9f)
                        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold) to color
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = parseInlineMarkdown(block.content, headerColor),
                        style = style,
                        color = headerColor,
                        lineHeight = 28.sp
                    )
                }

                is MdBlock.CodeBlock -> {
                    SyntaxHighlightedCodeBlock(
                        code = block.code,
                        language = block.language
                    )
                }

                is MdBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                }

                is MdBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.bullet,
                            color = NeonCyan,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.content, color),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            lineHeight = lineHeight
                        )
                    }
                }

                is MdBlock.Paragraph -> {
                    if (block.content.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(block.content, color),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            lineHeight = lineHeight
                        )
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
    language: String
) {
    val context = LocalContext.current
    val lineCount = code.lines().size
    val isLong = lineCount > 20
    var isExpanded by remember { mutableStateOf(!isLong) }
    val displayCode = if (isExpanded || !isLong) code else code.lines().take(15).joinToString("\n") + "\n..."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        // Header bar with language label + copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D3D))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.uppercase(),
                color = Color(0xFF858599),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                fontSize = 10.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/Collapse button for long code
                if (isLong) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color(0xFF858599),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Copy button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF858599),
                        modifier = Modifier.size(14.dp)
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
                .padding(12.dp)
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

        // Show expand hint
        if (isLong && !isExpanded) {
            Text(
                text = "  ? ${lineCount - 15} more lines — tap to expand",
                color = Color(0xFF5C6370),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
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

    // Tokenize and highlight
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

            // Brackets
            line[i] in "(){}[]" -> {
                withStyle(SpanStyle(color = SyntaxColors.plain)) {
                    append(line[i])
                }
                i++
            }

            // Everything else
            else -> {
                withStyle(SpanStyle(color = SyntaxColors.plain)) {
                    append(line[i])
                }
                i++
            }
        }
    }
}

private fun isComment(line: String, lang: String): Boolean {
    return when {
        line.startsWith("//") -> true
        line.startsWith("/*") -> true
        line.startsWith("*") && lang in listOf("java", "kotlin", "kt", "js", "javascript", "ts", "typescript", "c", "cpp", "swift", "dart") -> true
        line.startsWith("#") && lang in listOf("python", "py", "ruby", "rb", "bash", "sh", "yaml", "yml", "r") -> true
        line.startsWith("--") && lang in listOf("sql", "lua", "haskell") -> true
        line.startsWith("<!--") -> true
        else -> false
    }
}

private fun isKeyword(word: String, lang: String): Boolean {
    val keywords = when (lang) {
        "kotlin", "kt" -> setOf(
            "fun", "val", "var", "class", "object", "interface", "abstract", "override",
            "return", "if", "else", "when", "for", "while", "do", "break", "continue",
            "import", "package", "private", "public", "protected", "internal", "open",
            "data", "sealed", "enum", "companion", "suspend", "inline", "reified",
            "by", "lazy", "lateinit", "const", "try", "catch", "finally", "throw",
            "is", "as", "in", "out", "this", "super", "null", "true", "false",
            "typealias", "constructor", "init", "get", "set", "operator", "infix",
            "annotation", "crossinline", "noinline", "tailrec", "vararg", "where"
        )
        "java" -> setOf(
            "public", "private", "protected", "class", "interface", "abstract", "final",
            "static", "void", "return", "if", "else", "for", "while", "do", "switch",
            "case", "break", "continue", "new", "try", "catch", "finally", "throw",
            "throws", "import", "package", "extends", "implements", "this", "super",
            "null", "true", "false", "instanceof", "synchronized", "volatile", "transient",
            "native", "enum", "assert", "default", "strictfp"
        )
        "python", "py" -> setOf(
            "def", "class", "return", "if", "elif", "else", "for", "while", "break",
            "continue", "import", "from", "as", "try", "except", "finally", "raise",
            "with", "pass", "lambda", "yield", "global", "nonlocal", "assert", "del",
            "True", "False", "None", "and", "or", "not", "in", "is", "async", "await"
        )
        "javascript", "js", "typescript", "ts" -> setOf(
            "function", "const", "let", "var", "return", "if", "else", "for", "while",
            "do", "switch", "case", "break", "continue", "new", "try", "catch", "finally",
            "throw", "import", "export", "from", "default", "class", "extends", "this",
            "super", "null", "undefined", "true", "false", "typeof", "instanceof",
            "async", "await", "yield", "of", "in", "delete", "void", "interface", "type",
            "enum", "implements", "abstract", "private", "public", "protected", "static",
            "readonly", "declare", "module", "namespace"
        )
        "swift" -> setOf(
            "func", "var", "let", "class", "struct", "enum", "protocol", "extension",
            "return", "if", "else", "for", "while", "switch", "case", "break", "continue",
            "import", "public", "private", "internal", "open", "override", "static",
            "self", "super", "nil", "true", "false", "guard", "defer", "throw", "try",
            "catch", "typealias", "associatedtype", "where", "init", "deinit", "subscript",
            "inout", "mutating", "async", "await"
        )
        "dart" -> setOf(
            "void", "var", "final", "const", "class", "abstract", "extends", "implements",
            "return", "if", "else", "for", "while", "do", "switch", "case", "break",
            "continue", "import", "export", "this", "super", "null", "true", "false",
            "new", "try", "catch", "finally", "throw", "async", "await", "yield",
            "static", "dynamic", "typedef", "enum", "mixin", "with", "late", "required"
        )
        "c", "cpp", "c++" -> setOf(
            "int", "float", "double", "char", "void", "long", "short", "unsigned",
            "signed", "const", "static", "extern", "register", "volatile", "auto",
            "return", "if", "else", "for", "while", "do", "switch", "case", "break",
            "continue", "struct", "union", "enum", "typedef", "sizeof", "include",
            "define", "ifdef", "ifndef", "endif", "class", "public", "private",
            "protected", "virtual", "override", "template", "namespace", "using",
            "new", "delete", "try", "catch", "throw", "true", "false", "nullptr",
            "this", "inline", "constexpr", "noexcept"
        )
        "sql" -> setOf(
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
            "ALTER", "TABLE", "INDEX", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
            "ON", "AND", "OR", "NOT", "IN", "EXISTS", "BETWEEN", "LIKE", "ORDER",
            "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "AS", "DISTINCT", "UNION",
            "NULL", "TRUE", "FALSE", "SET", "VALUES", "INTO", "PRIMARY", "KEY",
            "select", "from", "where", "insert", "update", "delete", "create", "drop",
            "alter", "table", "index", "join", "left", "right", "inner", "outer",
            "on", "and", "or", "not", "in", "exists", "between", "like", "order",
            "by", "group", "having", "limit", "offset", "as", "distinct", "union",
            "null", "true", "false", "set", "values", "into", "primary", "key"
        )
        "go" -> setOf(
            "func", "var", "const", "type", "struct", "interface", "return", "if",
            "else", "for", "range", "switch", "case", "break", "continue", "import",
            "package", "go", "defer", "chan", "select", "map", "make", "new", "append",
            "len", "cap", "nil", "true", "false", "iota"
        )
        "rust" -> setOf(
            "fn", "let", "mut", "const", "struct", "enum", "impl", "trait", "pub",
            "return", "if", "else", "for", "while", "loop", "match", "break", "continue",
            "use", "mod", "crate", "self", "super", "as", "in", "ref", "move",
            "async", "await", "unsafe", "where", "type", "dyn", "static", "extern",
            "true", "false", "Some", "None", "Ok", "Err"
        )
        else -> setOf(
            "function", "fun", "def", "class", "return", "if", "else", "for", "while",
            "var", "val", "let", "const", "import", "from", "new", "true", "false",
            "null", "nil", "None", "this", "self", "super", "try", "catch", "throw",
            "public", "private", "static", "void", "int", "string", "bool", "float",
            "abstract", "interface", "extends", "implements", "override", "async", "await",
            "break", "continue", "do", "switch", "case", "default", "enum", "struct",
            "package", "export", "module", "type"
        )
    }
    return word in keywords
}

private fun isBuiltinType(word: String, lang: String): Boolean {
    val types = when (lang) {
        "kotlin", "kt" -> setOf(
            "Int", "Long", "Float", "Double", "Boolean", "String", "Char", "Byte", "Short",
            "Unit", "Nothing", "Any", "Array", "List", "Map", "Set", "MutableList",
            "MutableMap", "MutableSet", "Pair", "Triple", "Sequence", "Flow",
            "StateFlow", "MutableStateFlow", "LiveData", "MutableLiveData"
        )
        "java" -> setOf(
            "String", "Integer", "Long", "Float", "Double", "Boolean", "Character",
            "Byte", "Short", "Object", "List", "Map", "Set", "ArrayList", "HashMap",
            "LinkedList", "HashSet", "TreeMap", "Optional", "Stream"
        )
        "python", "py" -> setOf(
            "int", "float", "str", "bool", "list", "dict", "set", "tuple", "bytes",
            "range", "type", "object", "Exception", "TypeError", "ValueError"
        )
        "javascript", "js", "typescript", "ts" -> setOf(
            "String", "Number", "Boolean", "Object", "Array", "Map", "Set", "Promise",
            "Date", "RegExp", "Error", "Symbol", "BigInt", "Function", "Proxy",
            "any", "string", "number", "boolean", "void", "never", "unknown"
        )
        else -> setOf(
            "String", "Int", "Integer", "Float", "Double", "Boolean", "Long", "Char",
            "List", "Map", "Set", "Array", "Object", "void"
        )
    }
    return word in types
}

private fun isBuiltinFunction(word: String, lang: String): Boolean {
    val functions = when (lang) {
        "kotlin", "kt" -> setOf(
            "println", "print", "listOf", "mapOf", "setOf", "mutableListOf",
            "mutableMapOf", "mutableSetOf", "arrayOf", "buildList", "buildMap",
            "require", "check", "error", "TODO", "repeat", "run", "let", "also",
            "apply", "with", "takeIf", "takeUnless", "remember", "mutableStateOf",
            "LaunchedEffect", "rememberCoroutineScope"
        )
        "python", "py" -> setOf(
            "print", "len", "range", "enumerate", "zip", "map", "filter", "sorted",
            "isinstance", "type", "input", "open", "super", "getattr", "setattr",
            "hasattr", "iter", "next", "reversed", "abs", "min", "max", "sum"
        )
        "javascript", "js", "typescript", "ts" -> setOf(
            "console", "log", "warn", "error", "parseInt", "parseFloat",
            "setTimeout", "setInterval", "fetch", "require", "JSON",
            "Math", "Object", "Array", "Promise", "document", "window"
        )
        else -> setOf(
            "print", "println", "log", "console", "sizeof", "typeof", "instanceof"
        )
    }
    return word in functions
}

// --- Markdown Block Types ---

private sealed class MdBlock {
    data class Header(val level: Int, val content: String) : MdBlock()
    data class CodeBlock(val code: String, val language: String = "") : MdBlock()
    data class ListItem(val bullet: String, val content: String) : MdBlock()
    data class Paragraph(val content: String) : MdBlock()
    data object Divider : MdBlock()
}

// --- Block-level Parser ---

private fun parseMarkdownBlocks(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trimStart()

        when {
            // Code block: ```
            trimmed.startsWith("```") -> {
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MdBlock.CodeBlock(codeLines.joinToString("\n"), lang))
                if (i < lines.size) i++ // skip closing ```
            }

            // Divider: --- or ***
            trimmed.matches(Regex("^[-*_]{3,}$")) -> {
                blocks.add(MdBlock.Divider)
                i++
            }

            // Headers: # ## ###
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(3)
                val content = trimmed.drop(level).trimStart()
                blocks.add(MdBlock.Header(level, content))
                i++
            }

            // Bullet list: - or * or •
            trimmed.matches(Regex("^[-*•]\\s+.*")) -> {
                val content = trimmed.replaceFirst(Regex("^[-*•]\\s+"), "")
                blocks.add(MdBlock.ListItem("•", content))
                i++
            }

            // Numbered list: 1. 2. etc
            trimmed.matches(Regex("^\\d+[.):]\\s+.*")) -> {
                val num = trimmed.takeWhile { it.isDigit() }
                val content = trimmed.replaceFirst(Regex("^\\d+[.):]\\s+"), "")
                blocks.add(MdBlock.ListItem("$num.", content))
                i++
            }

            // Empty line
            trimmed.isBlank() -> {
                i++
            }

            // Regular paragraph
            else -> {
                val paraLines = mutableListOf(line)
                i++
                while (i < lines.size) {
                    val nextLine = lines[i].trimStart()
                    if (nextLine.isBlank() ||
                        nextLine.startsWith("#") ||
                        nextLine.startsWith("```") ||
                        nextLine.matches(Regex("^[-*•]\\s+.*")) ||
                        nextLine.matches(Regex("^\\d+[.):]\\s+.*")) ||
                        nextLine.matches(Regex("^[-*_]{3,}$"))
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

// --- Inline Markdown Parser (bold, italic, code, strikethrough) ---

private fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            when {
                // Bold + Italic: ***text***
                i + 2 < len && text.substring(i, i + 3) == "***" -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append("*")
                        i++
                    }
                }

                // Bold: **text**
                i + 1 < len && text.substring(i, i + 2) == "**" -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("*")
                        i++
                    }
                }

                // Italic: *text*
                text[i] == '*' && i + 1 < len && text[i + 1] != ' ' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append("*")
                        i++
                    }
                }

                // Inline code: `text`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE06C75),
                                background = Color(0xFF2D2D3D),
                                fontSize = 13.sp
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

                // Strikethrough: ~~text~~
                i + 1 < len && text.substring(i, i + 2) == "~~" -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
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


