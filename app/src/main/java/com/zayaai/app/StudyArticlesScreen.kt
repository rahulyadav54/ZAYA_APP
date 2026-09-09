package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun StudyArticlesScreen(viewModel: StudyArticlesViewModel, paddingValues: PaddingValues) {
    val articles by viewModel.articles.collectAsState()
    var selectedArticle by remember { mutableStateOf<StudyArticleEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var hasSeeded by remember { mutableStateOf(false) }

    // Seed default articles if database is empty (one-time)
    LaunchedEffect(articles) {
        if (articles.isEmpty() && !hasSeeded) {
            hasSeeded = true
            defaultStudyArticles.forEach { article ->
                viewModel.addArticle(article.title, article.content, article.category)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (articles.isEmpty() && !hasSeeded) {
            // Initial loading state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (articles.isEmpty() && hasSeeded) {
            // Loading after seed (articles are being inserted)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading articles...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Study Articles",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${articles.size} articles available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                items(articles) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { selectedArticle = article },
                        onDelete = { viewModel.deleteArticle(article) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB to add article
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Article", tint = Color.White)
            }
        }

        // Dialogs
        if (selectedArticle != null) {
            ArticleDetailDialog(article = selectedArticle!!) {
                selectedArticle = null
            }
        }

        if (showAddDialog) {
            AddArticleDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, content, category ->
                    viewModel.addArticle(title, content, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ArticleCard(article: StudyArticleEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Article, contentDescription = null, tint = NeonPurple)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Text(
                    text = article.content.take(80) + if (article.content.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252).copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ArticleDetailDialog(article: StudyArticleEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(article.title, color = NeonCyan, fontWeight = FontWeight.Bold)
                Text(article.category, style = MaterialTheme.typography.labelSmall, color = NeonPurple)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = article.content,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonCyan)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    val categories = listOf("General", "Science", "Mathematics", "Technology", "History", "Language", "Study Tips")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Article", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSave(title, content, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

// Default study articles to seed the database
private data class DefaultArticle(val title: String, val content: String, val category: String)

private val defaultStudyArticles = listOf(
    DefaultArticle(
        title = "Effective Study Techniques",
        content = """The most effective study techniques backed by research include:

1. **Active Recall** - Test yourself on the material instead of passively re-reading. Use flashcards, practice problems, or close your notes and try to recall key concepts.

2. **Spaced Repetition** - Review material at increasing intervals. Study today, review tomorrow, then in 3 days, then a week, etc.

3. **The Feynman Technique** - Explain the concept in simple terms as if teaching someone else. If you get stuck, go back and study that part.

4. **Pomodoro Technique** - Study for 25 minutes, then take a 5-minute break. After 4 sessions, take a longer 15-30 minute break.

5. **Interleaving** - Mix different topics or types of problems in a single study session rather than focusing on just one.

6. **Elaborative Interrogation** - Ask "why" and "how" questions about the material to deepen understanding.

These methods are proven to be more effective than highlighting, re-reading, or cramming.""",
        category = "Study Tips"
    ),
    DefaultArticle(
        title = "Introduction to Machine Learning",
        content = """Machine Learning (ML) is a subset of Artificial Intelligence that enables systems to learn from data and improve without being explicitly programmed.

**Types of Machine Learning:**

- Supervised Learning - The algorithm learns from labeled training data (e.g., spam detection, image classification)
- Unsupervised Learning - The algorithm finds patterns in unlabeled data (e.g., clustering, dimensionality reduction)
- Reinforcement Learning - The algorithm learns through trial and error with rewards/penalties (e.g., game playing, robotics)

**Key Concepts:**
- Training Data: The dataset used to train the model
- Features: Input variables used for predictions
- Labels: The output/target variable
- Model: The mathematical representation learned from data
- Overfitting: When a model performs well on training data but poorly on new data

**Popular Algorithms:**
- Linear Regression, Decision Trees, Random Forests
- Neural Networks, Support Vector Machines
- K-Means Clustering, Principal Component Analysis""",
        category = "Technology"
    ),
    DefaultArticle(
        title = "The Scientific Method",
        content = """The scientific method is a systematic approach to understanding the natural world through observation and experimentation.

**Steps of the Scientific Method:**

1. **Observation** - Notice something interesting or puzzling in the world around you.

2. **Question** - Formulate a specific question about your observation.

3. **Research** - Gather existing information about your topic.

4. **Hypothesis** - Make an educated guess that answers your question. It should be testable and falsifiable.

5. **Experiment** - Design and conduct a controlled experiment to test your hypothesis. Use control and experimental groups.

6. **Analyze Data** - Collect and analyze your results using statistics and data visualization.

7. **Conclusion** - Determine whether your hypothesis was supported or refuted by the evidence.

8. **Communicate** - Share your findings with others through reports, papers, or presentations.

Remember: Science is iterative. Results often lead to new questions and further investigation.""",
        category = "Science"
    ),
    DefaultArticle(
        title = "Fundamentals of Mathematics",
        content = """Core mathematical concepts every student should master:

**Algebra Basics:**
- Variables represent unknown values (x, y, z)
- Equations: expressions set equal to each other (2x + 3 = 7)
- Solving: isolate the variable using inverse operations

**Key Formulas:**
- Quadratic Formula: x = (-b +/- sqrt(b^2-4ac)) / 2a
- Pythagorean Theorem: a^2 + b^2 = c^2
- Area of Circle: A = pi * r^2
- Slope: m = (y2 - y1) / (x2 - x1)

**Calculus Foundations:**
- Derivatives measure rate of change (dy/dx)
- Integrals measure accumulated area under a curve
- The Fundamental Theorem connects derivatives and integrals

**Statistics Essentials:**
- Mean (average), Median (middle value), Mode (most frequent)
- Standard Deviation measures spread of data
- Normal Distribution (bell curve) describes many natural phenomena

**Tips for Math Success:**
- Practice regularly - math is a skill, not just knowledge
- Show your work step by step
- Understand WHY formulas work, not just how to use them""",
        category = "Mathematics"
    ),
    DefaultArticle(
        title = "World History: Key Events",
        content = """Major turning points in world history:

**Ancient World (3000 BCE - 500 CE):**
- Rise of civilizations in Mesopotamia, Egypt, India, China
- Greek democracy and philosophy (5th century BCE)
- Roman Empire's rise and fall (27 BCE - 476 CE)

**Medieval Period (500 - 1500):**
- Rise of Islam (7th century)
- The Crusades (1096-1291)
- Black Death devastates Europe (1347-1351)
- Gutenberg's printing press (1440)

**Early Modern (1500 - 1800):**
- Age of Exploration - Columbus, Magellan, Da Gama
- Renaissance - rebirth of art, science, and learning
- Scientific Revolution - Galileo, Newton, Copernicus
- American (1776) and French (1789) Revolutions

**Modern Era (1800 - Present):**
- Industrial Revolution transforms economies
- World War I (1914-1918) and World War II (1939-1945)
- Cold War and Space Race
- Digital Revolution and the Internet age

Understanding history helps us learn from the past and better navigate the future.""",
        category = "History"
    ),
    DefaultArticle(
        title = "English Grammar Essentials",
        content = """Master these grammar fundamentals for clear writing:

**Parts of Speech:**
- Nouns - people, places, things, ideas
- Verbs - action or state of being
- Adjectives - describe nouns
- Adverbs - describe verbs, adjectives, other adverbs
- Prepositions - show relationships (in, on, at, by)
- Conjunctions - connect words/clauses (and, but, or)

**Sentence Structure:**
- Simple: One independent clause (The cat sat.)
- Compound: Two independent clauses joined by conjunction (The cat sat, and the dog slept.)
- Complex: Independent + dependent clause (When it rained, the cat sat inside.)

**Common Mistakes to Avoid:**
- Their/There/They're
- Your/You're
- Its/It's
- Effect/Affect
- Then/Than

**Writing Tips:**
1. Use active voice: "The student wrote the essay" (not "The essay was written by the student")
2. Be concise - remove unnecessary words
3. Vary sentence length for better flow
4. Read your work aloud to catch errors
5. Proofread carefully before submitting""",
        category = "Language"
    ),
    DefaultArticle(
        title = "Programming Basics for Beginners",
        content = """Getting started with programming:

**What is Programming?**
Programming is giving instructions to a computer to perform specific tasks. These instructions are written in programming languages like Python, Java, Kotlin, or JavaScript.

**Core Concepts:**

1. **Variables** - Store data values
   Example: name = "Zaya", age = 20

2. **Data Types** - Different kinds of data
   - String: text ("hello")
   - Integer: whole numbers (42)
   - Float: decimal numbers (3.14)
   - Boolean: true/false

3. **Conditionals** - Make decisions
   if (score >= 90) -> "A grade"
   else if (score >= 80) -> "B grade"

4. **Loops** - Repeat actions
   for each student in class -> print grade

5. **Functions** - Reusable blocks of code
   function calculateArea(length, width) -> return length * width

6. **Arrays/Lists** - Collections of data
   colors = ["red", "blue", "green"]

**Best Practices:**
- Write clean, readable code with comments
- Break problems into smaller sub-problems
- Test your code frequently
- Learn to debug (find and fix errors)
- Practice daily with small projects""",
        category = "Technology"
    ),
    DefaultArticle(
        title = "Time Management for Students",
        content = """Master your time to boost academic performance:

**The Eisenhower Matrix:**
Organize tasks by urgency and importance:
- Urgent + Important -> Do immediately
- Important + Not Urgent -> Schedule for later
- Urgent + Not Important -> Delegate if possible
- Not Urgent + Not Important -> Eliminate

**Practical Strategies:**

1. **Plan Your Week** - Every Sunday, map out your week. Assign study blocks for each subject.

2. **Use the 2-Minute Rule** - If a task takes less than 2 minutes, do it now instead of adding it to your list.

3. **Batch Similar Tasks** - Group similar activities together (all reading in one block, all writing in another).

4. **Set SMART Goals:**
   - Specific: "Study Chapter 5 of Biology"
   - Measurable: "Complete 20 practice problems"
   - Achievable: realistic for your skill level
   - Relevant: aligned with your course goals
   - Time-bound: "by Thursday evening"

5. **Avoid Multitasking** - Focus on one task at a time. Task-switching reduces efficiency by up to 40%.

6. **Take Regular Breaks** - Use the Pomodoro Technique (25 min work, 5 min break).

7. **Protect Your Sleep** - 7-9 hours of sleep improves memory consolidation and focus.

Remember: Being busy isn't the same as being productive!""",
        category = "Study Tips"
    )
)

