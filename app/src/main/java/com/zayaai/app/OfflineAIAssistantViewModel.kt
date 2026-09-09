package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

data class OfflineMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class OfflineAIAssistantViewModel : ViewModel() {

    private val _messages = mutableStateListOf<OfflineMessage>()
    val messages: List<OfflineMessage> = _messages

    var isTyping by mutableStateOf(false)
        private set

    init {
        _messages.add(
            OfflineMessage(
                text = "Hi! I'm your **Offline AI Study Assistant**.\n\n" +
                    "I work completely **without internet**! Here's what I can help with:\n\n" +
                    "* **Math** - formulas, algebra, geometry, calculus\n" +
                    "* **Science** - physics, chemistry, biology\n" +
                    "* **Programming** - concepts, syntax, algorithms\n" +
                    "* **English** - grammar, vocabulary, writing tips\n" +
                    "* **Study Tips** - techniques, memory, time management\n" +
                    "* **General Knowledge** - history, geography, current affairs\n\n" +
                    "Just type your question!",
                isUser = false
            )
        )
    }

    suspend fun sendMessage(text: String) {
        if (text.isBlank()) return
        _messages.add(OfflineMessage(text, true))
        isTyping = true
        // Small delay for natural feel
        delay(400 + (Math.random() * 400).toLong())
        val reply = generateOfflineResponse(text)
        _messages.add(OfflineMessage(reply, false))
        isTyping = false
    }

    fun clearChat() {
        _messages.clear()
        _messages.add(
            OfflineMessage(
                text = "Chat cleared! Ask me anything about your studies.",
                isUser = false
            )
        )
    }

    // ==============================
    //  OFFLINE KNOWLEDGE ENGINE
    // ==============================

    private fun generateOfflineResponse(input: String): String {
        val q = input.lowercase().trim()

        // -- Greetings --
        if (q.matches(Regex("^(hi|hello|hey|sup|yo|hola|namaste|good morning|good evening|good afternoon).*"))) {
            return "Hello! I'm here to help you study. What subject do you need help with today?\n\n" +
                "Try asking about:\n- Math formulas\n- Science concepts\n- Programming help\n- Study tips\n- Grammar rules"
        }

        if (q.contains("how are you") || q.contains("how r u")) {
            return "I'm doing great, thanks for asking! Ready to help you ace your studies. What topic do you need help with?"
        }

        if (q.contains("thank") || q.contains("thanks")) {
            return "You're welcome! Feel free to ask me anything else. Happy studying!"
        }

        if (q.contains("who are you") || q.contains("what are you") || q.contains("your name")) {
            return "I'm the **Offline AI Study Assistant** by **ZAYA CODE HUB**.\n\n" +
                "I work completely **without internet** and I'm designed to help students with:\n" +
                "- Math & Science\n- Programming\n- English & Grammar\n- Study techniques\n- General knowledge\n\n" +
                "I have a built-in knowledge base - no API calls needed!"
        }

        // -- MATH --
        if (q.containsAny("pythagor", "pythagoras")) {
                return "## Pythagorean Theorem\n\n" +
                "**Formula:** `a^2 + b^2 = c^2`\n\n" +
                "Where:\n" +
                "- `a` and `b` are the two shorter sides (legs)\n" +
                "- `c` is the longest side (hypotenuse)\n\n" +
                "**Example:** If a = 3 and b = 4:\n" +
                "- c^2 = 3^2 + 4^2 = 9 + 16 = 25\n" +
                "- c = sqrt(25) = **5**\n\n" +
                "**Common Pythagorean Triples:**\n" +
                "- (3, 4, 5)\n- (5, 12, 13)\n- (8, 15, 17)\n- (7, 24, 25)"
        }

        if (q.containsAny("quadratic formula", "quadratic equation")) {
            return "## Quadratic Formula\n\n" +
                "For equation **ax^2 + bx + c = 0**:\n\n" +
                "**x = (-b +/- sqrt(b^2 - 4ac)) / 2a**\n\n" +
                "**Roots Analysis (D = b^2 - 4ac):**\n" +
                "- D > 0: Two real distinct roots\n" +
                "- D = 0: Two equal real roots\n" +
                "- D < 0: Two complex roots\n\n" +
                "**Example:** 2x^2 + 5x - 3 = 0\n" +
                "- a=2, b=5, c=-3\n" +
                "- D = 25 - 4(2)(-3) = 25 + 24 = 49\n" +
                "- x = (-5 +/- 7) / 4\n" +
                "- x1 = 0.5, x2 = -3"
        }

        if (q.containsAny("area of circle", "circle area", "area circle")) {
            return "## Area of a Circle\n\n" +
                "**Formula:** `A = pi * r^2`\n\n" +
                "Where:\n- A = Area\n- pi approx 3.14159\n- r = radius\n\n" +
                "**Other circle formulas:**\n" +
                "- Circumference: `C = 2 * pi * r`\n" +
                "- Diameter: `d = 2 * r`\n" +
                "- Area from diameter: `A = pi * (d/2)^2`\n\n" +
                "**Example:** r = 7 cm\n" +
                "- A = pi * 7^2 = pi * 49 approx **153.94 cm^2**"
        }

        if (q.containsAny("trigonometry", "sin cos tan", "trig identit", "trigonometric")) {
            return "## Trigonometry Basics\n\n" +
                "**SOH CAH TOA:**\n" +
                "- **sin(theta)** = Opposite / Hypotenuse\n" +
                "- **cos(theta)** = Adjacent / Hypotenuse\n" +
                "- **tan(theta)** = Opposite / Adjacent\n\n" +
                "**Key Identities:**\n" +
                "- sin^2(theta) + cos^2(theta) = 1\n" +
                "- tan(theta) = sin(theta) / cos(theta)\n" +
                "- sin(2*theta) = 2 * sin(theta) * cos(theta)\n" +
                "- cos(2*theta) = cos^2(theta) - sin^2(theta)\n\n" +
                "**Common Values:**\n" +
                "| Angle | sin | cos | tan |\n" +
                "|-------|-----|-----|-----|\n" +
                "| 0 deg | 0 | 1 | 0 |\n" +
                "| 30 deg | 1/2 | sqrt(3)/2 | 1/sqrt(3) |\n" +
                "| 45 deg | sqrt(2)/2 | sqrt(2)/2 | 1 |\n" +
                "| 60 deg | sqrt(3)/2 | 1/2 | sqrt(3) |\n" +
                "| 90 deg | 1 | 0 | infinity |"
        }

        if (q.containsAny("derivative", "differentiation", "calculus derivative")) {
            return "## Differentiation Rules\n\n" +
                "**Basic Rules:**\n" +
                "- Power Rule: d/dx(x^n) = nx^(n-1)\n" +
                "- Constant: d/dx(c) = 0\n" +
                "- Sum: d/dx(f+g) = f' + g'\n" +
                "- Product: d/dx(fg) = f'g + fg'\n" +
                "- Quotient: d/dx(f/g) = (f'g - fg') / g^2\n" +
                "- Chain Rule: d/dx(f(g(x))) = f'(g(x)) * g'(x)\n\n" +
                "**Common Derivatives:**\n" +
                "- d/dx(sin x) = cos x\n" +
                "- d/dx(cos x) = -sin x\n" +
                "- d/dx(e^x) = e^x\n" +
                "- d/dx(ln x) = 1/x\n" +
                "- d/dx(tan x) = sec^2 x"
        }

        if (q.containsAny("integration", "integral", "antiderivative")) {
            return "## Integration Rules\n\n" +
                "**Basic Rules:**\n" +
                "- int x^n dx = x^(n+1)/(n+1) + C (n != -1)\n" +
                "- int 1/x dx = ln|x| + C\n" +
                "- int e^x dx = e^x + C\n" +
                "- int sin x dx = -cos x + C\n" +
                "- int cos x dx = sin x + C\n" +
                "- int sec^2 x dx = tan x + C\n\n" +
                "**Techniques:**\n" +
                "1. **Substitution** - change variable\n" +
                "2. **Integration by Parts** - int u dv = uv - int v du\n" +
                "3. **Partial Fractions** - break rational functions\n" +
                "4. **Trigonometric Substitution**"
        }

        if (q.containsAny("logarithm", "log rules", "log formula")) {
            return "## Logarithm Rules\n\n" +
                "**Definition:** log_a(x) = y means a^y = x\n\n" +
                "**Key Rules:**\n" +
                "- log(ab) = log a + log b\n" +
                "- log(a/b) = log a - log b\n" +
                "- log(a^n) = n * log a\n" +
                "- log_a(a) = 1\n" +
                "- log_a(1) = 0\n" +
                "- Change of base: log_a(b) = log(b) / log(a)\n\n" +
                "**Natural Log (ln):**\n" +
                "- ln(e) = 1\n" +
                "- ln(e^x) = x\n" +
                "- e^(ln x) = x"
        }

        if (q.containsAny("algebra", "linear equation", "solve equation")) {
            return "## Algebra Essentials\n\n" +
                "**Linear Equations (ax + b = 0):**\n" +
                "- Isolate x: x = -b/a\n\n" +
                "**Systems of Equations:**\n" +
                "- Substitution method\n" +
                "- Elimination method\n" +
                "- Matrix method (Cramer's rule)\n\n" +
                "**Key Identities:**\n" +
                "- (a + b)^2 = a^2 + 2ab + b^2\n" +
                "- (a - b)^2 = a^2 - 2ab + b^2\n" +
                "- a^2 - b^2 = (a+b)(a-b)\n" +
                "- (a + b)^3 = a^3 + 3a^2b + 3ab^2 + b^3\n\n" +
                "**Inequalities:**\n" +
                "- If a > b and c > 0, then ac > bc\n" +
                "- If a > b and c < 0, then ac < bc (sign flips!)"
        }

        if (q.containsAny("probability", "permutation", "combination")) {
            return "## Probability & Combinatorics\n\n" +
                "**Probability:**\n" +
                "- P(A) = Favorable outcomes / Total outcomes\n" +
                "- P(A or B) = P(A) + P(B) - P(A and B)\n" +
                "- P(A and B) = P(A) * P(B|A)\n\n" +
                "**Permutations (order matters):**\n" +
                "- nPr = n! / (n-r)!\n\n" +
                "**Combinations (order doesn't matter):**\n" +
                "- nCr = n! / (r! * (n-r)!)\n\n" +
                "**Bayes' Theorem:**\n" +
                "- P(A|B) = P(B|A) * P(A) / P(B)"
        }

        if (q.containsAny("matrix", "matrices", "determinant")) {
            return "## Matrices\n\n" +
                "**Operations:**\n" +
                "- Addition: Add corresponding elements\n" +
                "- Scalar multiplication: Multiply each element\n" +
                "- Matrix multiplication: Rows x Columns\n\n" +
                "**2x2 Determinant:**\n" +
                "- |a b; c d| = ad - bc\n\n" +
                "**Inverse (2x2):**\n" +
                "- A^-1 = (1/det) * |d -b; -c a|\n\n" +
                "**Properties:**\n" +
                "- (AB)^T = B^T * A^T\n" +
                "- (AB)^-1 = B^-1 * A^-1\n" +
                "- det(AB) = det(A) * det(B)"
        }

        // -- PHYSICS --
        if (q.containsAny("newton", "newton's law", "laws of motion")) {
            return "## Newton's Laws of Motion\n\n" +
                "**1st Law (Inertia):**\n" +
                "An object stays at rest or in uniform motion unless acted upon by a net external force.\n\n" +
                "**2nd Law (F = ma):**\n" +
                "Force = Mass * Acceleration\n" +
                "- F is in Newtons (N)\n" +
                "- m is in kilograms (kg)\n" +
                "- a is in m/s^2\n\n" +
                "**3rd Law (Action-Reaction):**\n" +
                "For every action, there is an equal and opposite reaction.\n\n" +
                "**Example:** A 5 kg object accelerates at 3 m/s^2\n" +
                "F = 5 * 3 = **15 N**"
        }

        if (q.containsAny("ohm's law", "ohms law", "voltage current resistance")) {
            return "## Ohm's Law\n\n" +
                "**Formula:** `V = I * R`\n\n" +
                "Where:\n" +
                "- V = Voltage (Volts)\n" +
                "- I = Current (Amperes)\n" +
                "- R = Resistance (Ohms)\n\n" +
                "**Derived formulas:**\n" +
                "- I = V/R\n" +
                "- R = V/I\n" +
                "- Power: P = VI = I^2R = V^2/R\n\n" +
                "**Series:** R_total = R1 + R2 + R3\n" +
                "**Parallel:** 1/R_total = 1/R1 + 1/R2 + 1/R3"
        }

        if (q.containsAny("gravity", "gravitational", "free fall", "acceleration due to gravity")) {
            return "## Gravity\n\n" +
                "**Gravitational acceleration:** g approx 9.8 m/s^2\n\n" +
                "**Newton's Law of Gravitation:**\n" +
                "F = G * m1 * m2 / r^2\n" +
                "- G = 6.674 * 10^-11 N*m^2/kg^2\n\n" +
                "**Free Fall Equations:**\n" +
                "- v = u + gt\n" +
                "- h = ut + 0.5gt^2\n" +
                "- v^2 = u^2 + 2gh\n\n" +
                "**Key Facts:**\n" +
                "- All objects fall at the same rate (ignoring air resistance)\n" +
                "- Weight = mass * g\n" +
                "- g on Moon approx 1.6 m/s^2"
        }

        if (q.containsAny("thermodynamic", "laws of thermodynamics", "entropy")) {
            return "## Laws of Thermodynamics\n\n" +
                "**0th Law:** If A is in thermal equilibrium with B, and B with C, then A is with C.\n\n" +
                "**1st Law (Energy Conservation):**\n" +
                "Delta U = Q - W\n" +
                "- Delta U = change in internal energy\n" +
                "- Q = heat added\n" +
                "- W = work done by system\n\n" +
                "**2nd Law:**\n" +
                "Entropy of an isolated system always increases.\n" +
                "Heat flows from hot -> cold naturally.\n\n" +
                "**3rd Law:**\n" +
                "As temperature -> 0 K, entropy -> 0."
        }

        if (q.containsAny("kinematic", "motion equation", "suvat", "velocity acceleration")) {
            return "## Kinematics (Motion Equations)\n\n" +
                "**SUVAT Equations:**\n" +
                "* u = initial velocity\n" +
                "* v = final velocity\n" +
                "* a = acceleration\n" +
                "* t = time\n" +
                "* s = displacement\n\n" +
                "- v = u + at\n" +
                "- s = ut + 0.5at^2\n" +
                "- v^2 = u^2 + 2as\n" +
                "- s = 0.5(u + v)t\n\n" +
                "**Projectile Motion:**\n" +
                "- Horizontal: x = v0*cos(theta) * t\n- Vertical: y = v0*sin(theta) * t - 0.5gt^2\n- Range: R = v0^2*sin(2*theta) / g"
        }

        // -- CHEMISTRY --
        if (q.containsAny("periodic table", "elements", "atomic number")) {
            return "## Periodic Table Basics\n\n" +
                "**First 20 Elements:**\n" +
                "H, He, Li, Be, B, C, N, O, F, Ne,\n" +
                "Na, Mg, Al, Si, P, S, Cl, Ar, K, Ca\n\n" +
                "**Groups:**\n" +
                "* Group 1: Alkali Metals (Li, Na, K...)\n" +
                "* Group 2: Alkaline Earth Metals (Be, Mg, Ca...)\n" +
                "* Group 17: Halogens (F, Cl, Br, I...)\n" +
                "* Group 18: Noble Gases (He, Ne, Ar...)\n\n" +
                "**Trends:**\n" +
                "- Atomic radius: increases down, decreases right\n- Electronegativity: decreases down, increases right\n- Ionization energy: decreases down, increases right"
        }

        if (q.containsAny("chemical bond", "ionic bond", "covalent bond", "bonding")) {
            return "## Chemical Bonding\n\n" +
                "**Ionic Bond:**\n" +
                "* Transfer of electrons (metal + non-metal)\n" +
                "* Example: NaCl (Na+ + Cl-)\n" +
                "* High melting points, conduct electricity in solution\n\n" +
                "**Covalent Bond:**\n" +
                "* Sharing of electrons (non-metal + non-metal)\n" +
                "* Example: H2O, CO2\n" +
                "* Lower melting points\n\n" +
                "**Metallic Bond:**\n" +
                "* Sea of delocalized electrons\n" +
                "* Good conductors, malleable, ductile\n\n" +
                "**Hydrogen Bond:**\n" +
                "* Weak intermolecular force\n" +
                "* Important in water and DNA"
        }

        if (q.containsAny("acid", "base", "ph", "acidic", "alkaline")) {
            return "## Acids & Bases\n\n" +
                "**pH Scale:** 0-14\n" +
                "* pH < 7: Acidic\n" +
                "* pH = 7: Neutral\n" +
                "* pH > 7: Basic/Alkaline\n\n" +
                "**Strong Acids:** HCl, H2SO4, HNO3\n" +
                "**Strong Bases:** NaOH, KOH, Ca(OH)2\n\n" +
                "**Definitions:**\n" +
                "- Arrhenius: Acid produces H+, Base produces OH-\n- Bronsted-Lowry: Acid donates H+, Base accepts H+\n- Lewis: Acid accepts e- pair, Base donates e- pair\n\n**pH formula:** pH = -log[H+]"
        }

        if (q.containsAny("mole", "avogadro", "molar mass", "stoichiometry")) {
            return "## The Mole Concept\n\n" +
                "**Avogadro's Number:** 6.022 * 10^23\n\n" +
                "**1 mole** = 6.022 * 10^23 particles\n\n" +
                "**Key Formulas:**\n" +
                "* Moles = Mass / Molar Mass\n" +
                "* Moles = Number of particles / 6.022 * 10^23\n" +
                "* Moles (gas at STP) = Volume / 22.4 L\n\n" +
                "**Molarity:** M = moles of solute / liters of solution\n\n" +
                "**Stoichiometry Steps:**\n" +
                "1. Balance the equation\n" +
                "2. Convert given to moles\n" +
                "3. Use mole ratio\n" +
                "4. Convert to desired unit"
        }

        // -- BIOLOGY --
        if (q.containsAny("cell", "mitochondria", "organelle", "cell structure")) {
            return "## Cell Structure\n\n" +
                "**Plant vs Animal Cells:**\n\n" +
                "| Feature | Plant | Animal |\n" +
                "|---------|-------|--------|\n" +
                "| Cell Wall | Yes | No |\n" +
                "| Chloroplast | Yes | No |\n" +
                "|| Vacuole | Large | Small/multiple |\n" +
                "| Shape | Fixed | Irregular |\n\n" +
                "**Key Organelles:**\n" +
                "* **Nucleus** - DNA, controls cell\n" +
                "* **Mitochondria** - Powerhouse (ATP)\n" +
                "* **Ribosome** - Protein synthesis\n" +
                "* **ER** - Rough (protein) / Smooth (lipid)\n" +
                "* **Golgi** - Package & ship proteins\n" +
                "* **Lysosome** - Digestion"
        }

        if (q.containsAny("dna", "rna", "genetic", "gene", "chromosome")) {
            return "## DNA & Genetics\n\n" +
                "**DNA Structure:**\n" +
                "* Double helix\n" +
                "* Base pairs: A-T, G-C\n" +
                "* Sugar: Deoxyribose\n\n" +
                "**RNA:**\n" +
                "* Single strand\n" +
                "* Base pairs: A-U, G-C\n" +
                "* Types: mRNA, tRNA, rRNA\n\n" +
                "**Central Dogma:**\n" +
                "DNA -> (Transcription) -> mRNA -> (Translation) -> Protein\n\n" +
                "**Mendelian Genetics:**\n" +
                "* Dominant (AA, Aa) vs Recessive (aa)\n" +
                "* Genotype: genetic makeup\n" +
                "* Phenotype: physical expression"
        }

        if (q.containsAny("photosynthe", "chlorophyll")) {
            return "## Photosynthesis\n\n" +
                "**Equation:**\n" +
                "6CO2 + 6H2O + Light -> C6H12O6 + 6O2\n\n" +
                "**Stages:**\n" +
                "1. **Light Reactions** (Thylakoid)\n" +
                "   * Water splits -> O2 released\n" +
                "   * ATP and NADPH produced\n\n" +
                "2. **Calvin Cycle** (Stroma)\n" +
                "   * CO2 fixed into glucose\n" +
                "   * Uses ATP and NADPH\n\n" +
                "**Factors affecting rate:**\n" +
                "* Light intensity\n" +
                "* CO2 concentration\n" +
                "* Temperature\n" +
                "* Water availability"
        }

        // -- PROGRAMMING --
        if (q.containsAny("python", "python basics", "learn python")) {
            return "## Python Basics\n\n" +
                "**Variables:**\n```python\nname = \"ZAYA\"\nage = 20\npi = 3.14\n```\n\n" +
                "**Conditionals:**\n```python\nif age >= 18:\n    print(\"Adult\")\nelse:\n    print(\"Minor\")\n```\n\n" +
                "**Loops:**\n```python\nfor i in range(5):\n    print(i)\n\nwhile x > 0:\n    x -= 1\n```\n\n" +
                "**Functions:**\n```python\ndef greet(name):\n    return f\"Hello, {name}!\"\n```\n\n" +
                "**Lists:**\n```python\nfruits = [\"apple\", \"banana\"]\nfruits.append(\"cherry\")\n```"
        }

        if (q.containsAny("java", "java basics", "learn java")) {
            return "## Java Basics\n\n" +
                "**Hello World:**\n```java\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello!\");\n    }\n}\n```\n\n" +
                "**Data Types:** int, double, boolean, String, char\n\n" +
                "**OOP Concepts:**\n" +
                "* **Class** - Blueprint for objects\n" +
                "* **Object** - Instance of a class\n" +
                "* **Inheritance** - extends keyword\n" +
                "* **Polymorphism** - method overriding\n" +
                "* **Encapsulation** - private fields + getters/setters\n" +
                "* **Abstraction** - abstract classes & interfaces"
        }

        if (q.containsAny("kotlin", "kotlin basics", "learn kotlin")) {
            return "## Kotlin Basics\n\n" +
                "**Variables:**\n```kotlin\nval name = \"ZAYA\" // immutable\nvar age = 20       // mutable\n```\n\n" +
                "**Functions:**\n```kotlin\nfun greet(name: String): String {\n    return \"Hello, \$name!\"\n}\n// Short form:\nfun add(a: Int, b: Int) = a + b\n```\n\n" +
                "**Null Safety:**\n```kotlin\nvar x: String? = null\nprintln(x?.length ?: 0)\n```\n\n" +
                "**Data Class:**\n```kotlin\ndata class User(val name: String, val age: Int)\n```\n\n" +
                "**When Expression:**\n```kotlin\nwhen(x) {\n    1 -> \"One\"\n    2 -> \"Two\"\n    else -> \"Other\"\n}\n```"
        }

        if (q.containsAny("data structure", "array", "linked list", "stack", "queue")) {
            return "## Data Structures\n\n" +
                "**Array** - Fixed size, O(1) access\n" +
                "**Linked List** - Dynamic, O(1) insert at head\n" +
                "**Stack** - LIFO (Last In First Out)\n" +
                "* Push, Pop, Peek - all O(1)\n\n" +
                "**Queue** - FIFO (First In First Out)\n" +
                "* Enqueue, Dequeue - O(1)\n\n" +
                "**Hash Map** - Key-value pairs, O(1) average\n\n" +
                "**Tree** - Hierarchical, BST search O(log n)\n\n" +
                "**Graph** - Nodes + Edges, BFS/DFS traversal\n\n" +
                "**Complexity Comparison:**\n" +
                "| DS | Access | Search | Insert | Delete |\n" +
                "|----|--------|--------|--------|--------|\n" +
                "| Array | O(1) | O(n) | O(n) | O(n) |\n" +
                "| LinkedList | O(n) | O(n) | O(1) | O(1) |\n" +
                "| Hash Map | O(1) | O(1) | O(1) | O(1) |"
        }

        if (q.containsAny("sorting", "bubble sort", "merge sort", "quick sort", "algorithm")) {
            return "## Sorting Algorithms\n\n" +
                "| Algorithm | Best | Average | Worst | Space |\n" +
                "|-----------|------|---------|-------|-------|\n" +
                "| Bubble Sort | O(n) | O(n^2) | O(n^2) | O(1) |\n" +
                "| Selection Sort | O(n^2) | O(n^2) | O(n^2) | O(1) |\n" +
                "| Insertion Sort | O(n) | O(n^2) | O(n^2) | O(1) |\n" +
                "| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) |\n" +
                "| Quick Sort | O(n log n) | O(n log n) | O(n^2) | O(log n) |\n" +
                "| Heap Sort | O(n log n) | O(n log n) | O(n log n) | O(1) |\n\n" +
                "**Best for:**\n" +
                "* Nearly sorted -> Insertion Sort\n" +
                "* General purpose -> Merge Sort / Quick Sort\n" +
                "* Memory limited -> Heap Sort"
        }

        if (q.containsAny("big o", "time complexity", "space complexity")) {
            return "## Big O Notation\n\n" +
                "**Common Complexities (fastest to slowest):**\n" +
                "1. O(1) - Constant\n" +
                "2. O(log n) - Logarithmic\n" +
                "3. O(n) - Linear\n" +
                "4. O(n log n) - Linearithmic\n" +
                "5. O(n^2) - Quadratic\n" +
                "6. O(2^n) - Exponential\n" +
                "7. O(n!) - Factorial\n\n" +
                "**Examples:**\n" +
                "* O(1): Array access, HashMap lookup\n" +
                "* O(log n): Binary Search\n" +
                "* O(n): Linear Search\n" +
                "* O(n log n): Merge Sort\n" +
                "* O(n^2): Bubble Sort, Nested loops"
        }

        if (q.containsAny("html", "css", "web", "website")) {
            return "## HTML & CSS Basics\n\n" +
                "**HTML Structure:**\n```html\n<!DOCTYPE html>\n<html>\n<head>\n    <title>My Page</title>\n</head>\n<body>\n    <h1>Hello World</h1>\n    <p>This is a paragraph.</p>\n</body>\n</html>\n```\n\n" +
                "**Common Tags:** h1-h6, p, a, img, div, span, ul, ol, li, table, form, input, button\n\n" +
                "**CSS Basics:**\n```css\nbody {\n    font-family: Arial;\n    background-color: #f0f0f0;\n}\nh1 {\n    color: blue;\n    text-align: center;\n}\n```\n\n" +
                "**CSS Box Model:** Content -> Padding -> Border -> Margin"
        }

        // -- ENGLISH & GRAMMAR --
        if (q.containsAny("tense", "past tense", "present tense", "future tense")) {
            return "## English Tenses\n\n" +
                "**Present:**\n" +
                "* Simple: I write\n" +
                "* Continuous: I am writing\n" +
                "* Perfect: I have written\n" +
                "* Perfect Continuous: I have been writing\n\n" +
                "**Past:**\n" +
                "* Simple: I wrote\n" +
                "* Continuous: I was writing\n" +
                "* Perfect: I had written\n" +
                "* Perfect Continuous: I had been writing\n\n" +
                "**Future:**\n" +
                "* Simple: I will write\n" +
                "* Continuous: I will be writing\n" +
                "* Perfect: I will have written\n" +
                "* Perfect Continuous: I will have been writing"
        }

        if (q.containsAny("parts of speech", "noun", "verb", "adjective", "adverb")) {
            return "## Parts of Speech\n\n" +
                "**8 Parts of Speech:**\n\n" +
                "1. **Noun** - Person, place, thing (dog, city, love)\n" +
                "2. **Pronoun** - Replaces noun (he, she, they)\n" +
                "3. **Verb** - Action/state (run, is, think)\n" +
                "4. **Adjective** - Describes noun (big, red, happy)\n" +
                "5. **Adverb** - Describes verb (quickly, very, well)\n" +
                "6. **Preposition** - Shows relationship (in, on, at, by)\n" +
                "7. **Conjunction** - Connects (and, but, or, because)\n" +
                "8. **Interjection** - Exclamation (wow!, oh!, hey!)"
        }

        if (q.containsAny("active voice", "passive voice", "voice")) {
            return "## Active vs Passive Voice\n\n" +
                "**Active:** Subject performs the action\n" +
                "* The cat **caught** the mouse.\n\n" +
                "**Passive:** Subject receives the action\n" +
                "* The mouse **was caught** by the cat.\n\n" +
                "**Conversion Rules:**\n" +
                "* Object -> Subject\n" +
                "* Verb -> be + past participle\n" +
                "* Subject -> by + agent (optional)\n\n" +
                "**Examples by Tense:**\n" +
                "* Present: writes -> is written\n" +
                "* Past: wrote -> was written\n" +
                "* Future: will write -> will be written\n" +
                "* Perfect: has written -> has been written"
        }

        if (q.containsAny("essay", "essay writing", "how to write essay")) {
            return "## Essay Writing Guide\n\n" +
                "**Structure:**\n\n" +
                "**1. Introduction (10-15%)**\n" +
                "* Hook - grab attention\n" +
                "* Background - context\n" +
                "* Thesis - main argument\n\n" +
                "**2. Body Paragraphs (70-80%)**\n" +
                "Each paragraph:\n" +
                "* Topic sentence\n" +
                "* Evidence/examples\n" +
                "* Analysis\n" +
                "* Transition\n\n" +
                "**3. Conclusion (10-15%)**\n" +
                "* Restate thesis\n" +
                "* Summarize key points\n" +
                "* Final thought/call to action\n\n" +
                "**Tips:**\n" +
                "* Use transition words (however, moreover, therefore)\n" +
                "* Vary sentence length\n" +
                "* Proofread for grammar"
        }

        // -- STUDY TIPS --
        if (q.containsAny("study tip", "how to study", "study technique", "study method", "effective study")) {
            return "## Effective Study Techniques\n\n" +
                "**1. Pomodoro Technique** (Tomato emoji)\n" +
                "* 25 min study -> 5 min break\n" +
                "* After 4 rounds -> 15-30 min long break\n\n" +
                "**2. Active Recall** (Brain emoji)\n" +
                "* Close your book and try to recall\n" +
                "* Use flashcards\n" +
                "* Teach someone else\n\n" +
                "**3. Spaced Repetition** (Calendar emoji)\n" +
                "* Review after 1 day, 3 days, 7 days, 14 days\n\n" +
                "**4. Feynman Technique** (Lightbulb emoji)\n" +
                "* Explain concept in simple words\n" +
                "* If stuck, go back and learn\n\n" +
                "**5. Mind Mapping** (Map emoji)\n" +
                "* Central topic -> branches -> sub-branches\n\n" +
                "**6. SQ3R Method** (Book emoji)\n" +
                "* Survey, Question, Read, Recite, Review"
        }

        if (q.containsAny("memoriz", "memory", "remember", "forget")) {
            return "## Memory Techniques\n\n" +
                "**1. Mnemonics**\n" +
                "* Create acronyms (ROY G BIV for rainbow colors)\n" +
                "* Create sentences (My Very Eager Mother... for planets)\n\n" +
                "**2. Chunking**\n" +
                "* Break large info into small groups\n" +
                "* Phone numbers: 555-123-4567\n\n" +
                "**3. Memory Palace**\n" +
                "* Associate items with locations in a familiar place\n\n" +
                "**4. Visualization**\n" +
                "* Create vivid mental images\n\n" +
                "**5. Spaced Repetition**\n" +
                "* Review at increasing intervals\n\n" +
                "**6. Sleep Well** (Sleeping emoji)\n" +
                "* Memory consolidation happens during sleep\n" +
                "* 7-8 hours is ideal for students"
        }

        if (q.containsAny("exam", "test prep", "exam preparation", "before exam")) {
            return "## Exam Preparation Tips\n\n" +
                "**1 Week Before:**\n" +
                "* Create a revision schedule\n" +
                "* Focus on weak topics\n" +
                "* Practice past papers\n\n" +
                "**1 Day Before:**\n" +
                "* Light review only\n" +
                "* Prepare materials (pen, ID, calculator)\n" +
                "* Sleep early (7-8 hours)\n\n" +
                "**Exam Day:**\n" +
                "* Eat a good breakfast\n" +
                "* Arrive 15 min early\n" +
                "* Read all questions first\n" +
                "* Start with easy questions\n" +
                "* Manage time per question\n\n" +
                "**During Exam:**\n" +
                "* Don't panic if stuck - move on and return\n" +
                "* Show all work (partial credit!)\n" +
                "* Review answers if time allows"
        }

        if (q.containsAny("time management", "schedule", "productivity")) {
            return "## Time Management for Students\n\n" +
                "**Eisenhower Matrix:**\n" +
                "| | Urgent | Not Urgent |\n" +
                "|-----|--------|------------|\n" +
                "| **Important** | DO NOW | Schedule |\n" +
                "| **Not Important** | Delegate | Eliminate |\n\n" +
                "**Tips:**\n" +
                "* Use a planner or to-do app\n" +
                "* Set specific study goals per session\n" +
                "* Eliminate distractions (phone on silent)\n" +
                "* Study hardest subjects when most alert\n" +
                "* Take regular breaks\n" +
                "* Don't multitask - focus on one thing\n" +
                "* Daily Template:\n" +
                "* Morning: Most difficult subjects\n" +
                "* Afternoon: Practice problems\n" +
                "* Evening: Review and revision\n" +
                "* Night: Light reading, prepare for tomorrow"
        }

        // -- GENERAL KNOWLEDGE --
        if (q.containsAny("solar system", "planet", "mercury venus earth")) {
            return "## Solar System\n\n" +
                "**Planets (from Sun):**\n" +
                "1. Mercury - Smallest, closest to Sun\n" +
                "2. Venus - Hottest (greenhouse effect)\n" +
                "3. Earth - Our home (Earth emoji)\n" +
                "4. Mars - Red planet\n" +
                "5. Jupiter - Largest planet\n" +
                "6. Saturn - Famous rings\n" +
                "7. Uranus - Tilted axis\n" +
                "8. Neptune - Farthest, windiest\n\n" +
                "**Mnemonic:** My Very Eager Mother Just Served Us Nachos\n\n" +
                "**Fun Facts:**\n" +
                "* Sun is 99.86% of solar system mass\n" +
                "* Jupiter has 95 known moons\n" +
                "* A day on Venus > a year on Venus"
        }

        if (q.containsAny("world war", "ww1", "ww2", "first world war", "second world war")) {
            return "## World Wars Summary\n\n" +
                "**World War I (1914-1918):**\n" +
                "* Trigger: Assassination of Archduke Franz Ferdinand\n" +
                "* Alliances: Triple Entente vs Central Powers\n" +
                "* Result: Treaty of Versailles\n" +
                "* ~17 million deaths\n\n" +
                "**World War II (1939-1945):**\n" +
                "* Trigger: Germany invades Poland\n" +
                "* Alliances: Allies vs Axis Powers\n" +
                "* Key events: Holocaust, D-Day, Atomic bombs\n" +
                "* Result: UN formed, Cold War began\n" +
                "* ~70-85 million deaths\n\n" +
                "**Key Leaders:**\n" +
                "* Churchill (UK), Roosevelt/Truman (US)\n" +
                "* Hitler (Germany), Mussolini (Italy), Tojo (Japan)\n" +
                "* Stalin (USSR)"
        }

        // -- BROAD SUBJECT MATCHING --
        if (q.containsAny("math", "mathematics", "maths")) {
            return "## Mathematics Topics\n\nI can help with:\n\n" +
                "* **Algebra** - equations, inequalities, polynomials\n" +
                "* **Geometry** - areas, volumes, angles\n" +
                "* **Trigonometry** - sin, cos, tan, identities\n" +
                "* **Calculus** - derivatives, integrals, limits\n" +
                "* **Statistics** - mean, median, mode, probability\n" +
                "* **Matrices** - operations, determinants, inverse\n" +
                "* **Logarithms** - rules, equations\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"Explain quadratic formula\"\n" +
                "* \"Trigonometry identities\"\n" +
                "* \"Derivative rules\""
        }

        if (q.containsAny("physics")) {
            return "## Physics Topics\n\nI can help with:\n\n" +
                "* **Mechanics** - Newton's laws, kinematics, forces\n" +
                "* **Thermodynamics** - heat, entropy, laws\n" +
                "* **Electricity** - Ohm's law, circuits, magnetism\n" +
                "* **Optics** - reflection, refraction, lenses\n" +
                "* **Waves** - sound, light, electromagnetic spectrum\n" +
                "* **Modern Physics** - relativity, quantum basics\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"Newton's laws of motion\"\n" +
                "* \"Ohm's law\"\n" +
                "* \"Kinematics equations\""
        }

        if (q.containsAny("chemistry")) {
            return "## Chemistry Topics\n\nI can help with:\n\n" +
                "* **Periodic Table** - elements, groups, trends\n" +
                "* **Bonding** - ionic, covalent, metallic\n" +
                "* **Acids & Bases** - pH, reactions\n" +
                "* **Mole Concept** - Avogadro, stoichiometry\n" +
                "* **Organic Chemistry** - hydrocarbons, functional groups\n" +
                "* **Reactions** - types, balancing equations\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"Periodic table basics\"\n" +
                "* \"Chemical bonding\"\n" +
                "* \"Mole concept\""
        }

        if (q.containsAny("biology")) {
            return "## Biology Topics\n\nI can help with:\n\n" +
                "* **Cell Biology** - structure, organelles\n" +
                "* **Genetics** - DNA, RNA, inheritance\n" +
                "* **Evolution** - natural selection, adaptation\n" +
                "* **Ecology** - ecosystems, food chains\n" +
                "* **Human Body** - organ systems\n" +
                "* **Photosynthesis & Respiration**\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"Cell structure\"\n" +
                "* \"DNA and genetics\"\n" +
                "* \"Photosynthesis\""
        }

        if (q.containsAny("programming", "coding", "code")) {
            return "## Programming Topics\n\nI can help with:\n\n" +
                "* **Python** - syntax, functions, data types\n" +
                "* **Java** - OOP, classes, methods\n" +
                "* **Kotlin** - Android development, null safety\n" +
                "* **HTML/CSS** - web basics, styling\n" +
                "* **Data Structures** - arrays, lists, trees\n" +
                "* **Algorithms** - sorting, searching, Big O\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"Python basics\"\n" +
                "* \"Data structures\"\n" +
                "* \"Sorting algorithms\""
        }

        if (q.containsAny("english", "grammar")) {
            return "## English Topics\n\nI can help with:\n\n" +
                "* **Tenses** - present, past, future\n" +
                "* **Parts of Speech** - noun, verb, adjective, etc.\n" +
                "* **Active/Passive Voice** - conversion rules\n" +
                "* **Essay Writing** - structure, tips\n" +
                "* **Vocabulary** - common words, synonyms\n" +
                "* **Punctuation** - commas, semicolons, colons\n\n" +
                "Ask me a specific topic! For example:\n" +
                "* \"English tenses\"\n" +
                "* \"Active passive voice\"\n" +
                "* \"Essay writing guide\""
        }

        // -- FALLBACK --
        return "I don't have a specific answer for that in my offline knowledge base.\n\n" +
            "**Try asking about:**\n" +
            "* Math - \"quadratic formula\", \"trigonometry\", \"calculus\"\n" +
            "* Physics - \"Newton's laws\", \"Ohm's law\", \"kinematics\"\n" +
            "* Chemistry - \"periodic table\", \"acids and bases\", \"mole concept\"\n" +
            "* Biology - \"cell structure\", \"DNA\", \"photosynthesis\"\n" +
            "* Programming - \"Python basics\", \"data structures\", \"sorting algorithms\"\n" +
            "* English - \"tenses\", \"parts of speech\", \"essay writing\"\n" +
            "* Study Tips - \"study techniques\", \"memory tips\", \"exam preparation\"\n\n" +
            "Or try being more specific with your question!"
    }

    // Extension function for cleaner matching
    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }
}


