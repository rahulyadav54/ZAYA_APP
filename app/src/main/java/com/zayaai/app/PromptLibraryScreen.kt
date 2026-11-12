package com.zayaai.app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

data class PromptCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val prompts: List<PromptItem>
)

data class PromptItem(
    val title: String,
    val prompt: String,
    val description: String
)

private fun getPromptCategories(): List<PromptCategory> = listOf(
    PromptCategory(
        name = "Writing",
        icon = Icons.Rounded.Edit,
        color = Color(0xFFFFD600),
        prompts = listOf(
            PromptItem("Blog Post", "Write a detailed, engaging blog post about [TOPIC]. Include an attention-grabbing introduction, 3-5 key points with examples, and a compelling conclusion with a call to action.", "Generate a full blog post"),
            PromptItem("Email Draft", "Write a professional email to [RECIPIENT] regarding [SUBJECT]. The tone should be [formal/friendly/persuasive]. Include a clear subject line, greeting, body, and sign-off.", "Compose professional emails"),
            PromptItem("Story Starter", "Write the opening 3 paragraphs of a [GENRE] story set in [SETTING]. Introduce a compelling main character facing [CONFLICT]. Use vivid sensory details and create an immediate hook.", "Creative story beginnings"),
            PromptItem("Product Description", "Write a compelling product description for [PRODUCT]. Highlight its key features, benefits, target audience, and unique selling points. Use persuasive language that drives conversions.", "E-commerce product copy"),
            PromptItem("Social Media Post", "Create 5 engaging social media posts about [TOPIC] for [PLATFORM]. Include relevant hashtags, emojis, and a call-to-action. Keep the tone [casual/professional/witty].", "Social media content"),
            PromptItem("Resume Summary", "Write a professional resume summary for a [JOB_TITLE] with [X] years of experience in [INDUSTRY]. Highlight key achievements, skills, and career objectives in 3-4 sentences.", "Professional resume help")
        )
    ),
    PromptCategory(
        name = "Coding",
        icon = Icons.Rounded.Code,
        color = Color(0xFF00E676),
        prompts = listOf(
            PromptItem("Debug Helper", "I have the following code that produces [ERROR]. Please identify the bug, explain why it occurs, and provide the corrected code with comments:\n\n[PASTE CODE HERE]", "Fix code errors quickly"),
            PromptItem("Code Review", "Review the following code for best practices, performance issues, security vulnerabilities, and readability. Provide specific suggestions for improvement:\n\n[PASTE CODE HERE]", "Get expert code review"),
            PromptItem("API Design", "Design a RESTful API for [APPLICATION]. Include endpoints, HTTP methods, request/response schemas, authentication strategy, and error handling. Follow REST best practices.", "Design clean APIs"),
            PromptItem("Algorithm Solution", "Explain and implement an efficient algorithm to solve: [PROBLEM]. Include time/space complexity analysis, step-by-step walkthrough, and optimized code in [LANGUAGE].", "Algorithm explanations"),
            PromptItem("Database Schema", "Design a normalized database schema for [APPLICATION]. Include tables, columns, data types, primary/foreign keys, indexes, and relationships. Explain your design decisions.", "Database design help"),
            PromptItem("Unit Tests", "Write comprehensive unit tests for the following function/class using [TESTING_FRAMEWORK]. Cover edge cases, happy paths, and error scenarios:\n\n[PASTE CODE HERE]", "Generate test cases")
        )
    ),
    PromptCategory(
        name = "Study",
        icon = Icons.Rounded.School,
        color = Color(0xFF6E72FC),
        prompts = listOf(
            PromptItem("Explain Like I'm 5", "Explain [COMPLEX TOPIC] in the simplest possible terms, as if you're teaching a 5-year-old. Use everyday analogies, examples, and avoid jargon. Then gradually increase complexity.", "Simplify complex topics"),
            PromptItem("Study Notes", "Create comprehensive study notes on [TOPIC/CHAPTER]. Include key concepts, definitions, important formulas, examples, mnemonics, and practice questions. Format with clear headings.", "Generate study material"),
            PromptItem("Exam Prep", "Create a practice exam with 15 questions on [SUBJECT/TOPIC]. Mix multiple choice, short answer, and essay questions. Include difficulty levels and an answer key with explanations.", "Prepare for exams"),
            PromptItem("Concept Map", "Create a detailed concept map for [TOPIC] showing the relationships between all major concepts. List each concept and its connections. Explain how they relate to each other.", "Visual concept mapping"),
            PromptItem("Compare & Contrast", "Create a detailed comparison between [CONCEPT A] and [CONCEPT B]. Include similarities, differences, use cases, advantages/disadvantages, and a summary table.", "Compare topics easily"),
            PromptItem("Research Summary", "Summarize the key findings and methodology of research on [TOPIC]. Include the main arguments, evidence, conclusions, and implications. Cite relevant theories.", "Summarize research")
        )
    ),
    PromptCategory(
        name = "Business",
        icon = Icons.Rounded.Business,
        color = Color(0xFFFF9800),
        prompts = listOf(
            PromptItem("Business Plan", "Create a lean business plan outline for [BUSINESS IDEA]. Include value proposition, target market, revenue model, key metrics, competitive advantage, and initial milestones.", "Quick business planning"),
            PromptItem("SWOT Analysis", "Perform a comprehensive SWOT analysis for [COMPANY/PRODUCT/IDEA]. Identify at least 5 items for each category (Strengths, Weaknesses, Opportunities, Threats) with explanations.", "Strategic analysis"),
            PromptItem("Pitch Deck", "Create a 10-slide pitch deck outline for [STARTUP/PRODUCT]. Include: Problem, Solution, Market Size, Business Model, Traction, Team, Competition, Financials, Ask, and Vision.", "Investor pitch prep"),
            PromptItem("Market Research", "Conduct a market research analysis for [PRODUCT/SERVICE] in [MARKET]. Include market size, growth trends, customer segments, competitor analysis, and entry barriers.", "Market analysis"),
            PromptItem("Marketing Strategy", "Develop a comprehensive marketing strategy for [PRODUCT/SERVICE]. Include target audience personas, channels, content strategy, budget allocation, and KPIs.", "Marketing plan creation"),
            PromptItem("Financial Projection", "Create a 12-month financial projection for [BUSINESS]. Include revenue streams, cost structure, break-even analysis, cash flow forecast, and key assumptions.", "Financial forecasting")
        )
    ),
    PromptCategory(
        name = "Creative",
        icon = Icons.Rounded.Palette,
        color = Color(0xFFE040FB),
        prompts = listOf(
            PromptItem("Creative Concept", "Help me brainstorm a vivid visual concept and artistic theme for: [TOPIC]. Include color palette, mood, composition ideas, and creative direction.", "Visual & creative concepts"),
            PromptItem("Brainstorm Ideas", "Generate 20 creative and unique ideas for [PROJECT/TOPIC]. Think outside the box. For each idea, provide a brief description and potential impact. Rank them by feasibility.", "Creative brainstorming"),
            PromptItem("Song Lyrics", "Write original song lyrics about [THEME] in the style of [GENRE]. Include verse, chorus, bridge structure. Use vivid imagery, metaphors, and emotional resonance.", "Songwriting assistance"),
            PromptItem("Video Script", "Write a compelling video script for a [DURATION]-minute video about [TOPIC]. Include hook, intro, main content sections, transitions, call-to-action, and visual/audio cues.", "Video content creation"),
            PromptItem("Character Design", "Create a detailed character profile for [TYPE] character. Include appearance, personality traits, backstory, motivations, strengths, weaknesses, and character arc.", "Character development"),
            PromptItem("World Building", "Design a detailed fictional world/setting for [GENRE]. Include geography, culture, politics, technology level, magic system (if applicable), history, and key locations.", "Build fictional worlds")
        )
    ),
    PromptCategory(
        name = "Productivity",
        icon = Icons.Rounded.Speed,
        color = Color(0xFF00BCD4),
        prompts = listOf(
            PromptItem("Weekly Planner", "Create a detailed weekly plan for someone who wants to [GOAL]. Include daily tasks, time blocks, priority levels, milestones, and balance between work and rest.", "Plan your week"),
            PromptItem("Meeting Agenda", "Create a structured meeting agenda for [MEETING TYPE] with [NUMBER] attendees. Include objectives, time allocations, discussion points, action items template, and follow-up plan.", "Organize meetings"),
            PromptItem("Decision Matrix", "Help me decide between [OPTION A], [OPTION B], and [OPTION C] for [DECISION]. Create a weighted decision matrix with relevant criteria, scoring, and a clear recommendation.", "Make better decisions"),
            PromptItem("Goal Setting", "Help me create SMART goals for [AREA]. For each goal, define specific metrics, realistic timelines, action steps, potential obstacles, and accountability measures.", "Set achievable goals"),
            PromptItem("Habit Tracker", "Design a 30-day habit building plan for [HABIT]. Include daily micro-goals, progress milestones, accountability strategies, recovery plans for missed days, and reward system.", "Build better habits"),
            PromptItem("Problem Solving", "Help me solve this problem: [PROBLEM]. Use the 5-Why analysis, identify root causes, generate 5 potential solutions, evaluate pros/cons of each, and recommend the best approach.", "Structured problem solving")
        )
    )
)

@Composable
fun PromptLibraryScreen(paddingValues: PaddingValues, onUsePrompt: (String) -> Unit) {
    val categories = remember { getPromptCategories() }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    val clipboardManager = LocalClipboardManager.current
    var copiedPromptTitle by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                "Prompt Library",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Ready-to-use prompts for every need",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            category.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) Color.White else category.color
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.color.copy(alpha = 0.8f),
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = category.color.copy(alpha = 0.3f),
                        selectedBorderColor = category.color,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompts List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(selectedCategory.prompts) { promptItem ->
                PromptCard(
                    promptItem = promptItem,
                    categoryColor = selectedCategory.color,
                    isCopied = copiedPromptTitle == promptItem.title,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(promptItem.prompt))
                        copiedPromptTitle = promptItem.title
                    },
                    onUse = { onUsePrompt(promptItem.prompt) }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun PromptCard(
    promptItem: PromptItem,
    categoryColor: Color,
    isCopied: Boolean,
    onCopy: () -> Unit,
    onUse: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (expanded) categoryColor.copy(alpha = 0.5f) else categoryColor.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        promptItem.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        promptItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, categoryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        promptItem.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isCopied) Color(0xFF00E676) else categoryColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isCopied) "Copied!" else "Copy",
                            color = if (isCopied) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onUse,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
                    ) {
                        Icon(
                            Icons.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use in Chat", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


