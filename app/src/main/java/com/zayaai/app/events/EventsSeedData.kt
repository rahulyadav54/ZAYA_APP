package com.zayaai.app.events

object EventsSeedData {

    fun getInitialEvents(): List<EventItem> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        return listOf(
            EventItem(
                id = "evt_zaya_hackathon_2026",
                title = "AI ZAYA National Hackathon 2026",
                tagline = "Build the next generation of Agentic AI & Mobile Innovation",
                category = "Hackathons",
                organizer = "ZAYA CODE HUB & Tech Partners",
                isVerifiedOrganizer = true,
                posterUrl = "",
                startDate = now + oneDay * 7,
                endDate = now + oneDay * 9,
                registrationDeadline = now + oneDay * 5,
                mode = "Hybrid",
                venue = "Tech Innovation Campus, Bangalore & Discord Virtual Stage",
                locationUrl = "https://maps.google.com/?q=Bangalore+Tech+Center",
                meetingLink = "https://discord.gg/zayacodehub",
                isFree = true,
                price = 0.0,
                prizePool = "₹2,50,000",
                eligibility = "Open to all college students, developers, and tech teams across India and globally",
                registrationMode = "both",
                minTeamSize = 2,
                maxTeamSize = 5,
                availableDomains = listOf(
                    "Artificial Intelligence & Machine Learning",
                    "Generative AI",
                    "Web Development",
                    "Mobile App Development",
                    "Blockchain & Web3",
                    "Cybersecurity",
                    "Internet of Things (IoT)",
                    "Robotics & Automation",
                    "FinTech",
                    "Healthcare Technology",
                    "Education Technology",
                    "Renewable Energy & Sustainability",
                    "Agriculture Technology",
                    "Open Innovation",
                    "Other"
                ),
                allowCustomDomains = true,
                requireProjectDetails = false,
                capacity = 500,
                registeredCount = 286,
                description = """### Welcome to AI ZAYA National Hackathon 2026!
Join hundreds of top developers, innovators, and students to build breakthrough applications in **Artificial Intelligence, Mobile Systems, Cloud Workflows, and EdTech**.

### Tracks:
1. **Agentic AI & LLMs:** Autonomous agents, task delegation, multi-modal workflows.
2. **Mobile First & Flutter / Jetpack Compose:** High-performance, offline-capable mobile solutions.
3. **Open Innovation:** Tackle real-world problems in Healthcare, Education, and FinTech.

### Mentorship & Perks:
- Direct mentorship from industry staff and startup founders.
- Free cloud credits and API access.
- Verified completion certificates for all participating teams.""",
                rules = listOf(
                    "Teams must consist of 2 to 5 members.",
                    "All code must be written during the hackathon period. Open source libraries and frameworks are permitted.",
                    "Projects must be submitted on GitHub with a 2-minute demo video.",
                    "Plagiarism or submission of previously built projects results in immediate disqualification."
                ),
                agenda = listOf(
                    EventAgendaItem("Day 1 - 09:00 AM", "Opening Ceremony & Problem Statement Reveal", "Keynote by ZAYA CODE HUB leadership"),
                    EventAgendaItem("Day 1 - 11:30 AM", "Hacking Begins & Mentor Check-in 1", "Teams start building and aligning on architecture"),
                    EventAgendaItem("Day 2 - 03:00 PM", "Midway Review & Feedback Sessions", "Mentors review project repositories and progress"),
                    EventAgendaItem("Day 3 - 12:00 PM", "Code Freeze & Final Demo Presentations", "Top 10 teams pitch live to the jury"),
                    EventAgendaItem("Day 3 - 05:00 PM", "Award Ceremony & Certificate Distribution", "Winners announced live!")
                ),
                faqs = listOf(
                    EventFaqItem("Who is eligible to participate?", "Any undergraduate, postgraduate student or self-taught developer is welcome to join."),
                    EventFaqItem("Is there any registration fee?", "No! AI ZAYA Hackathon is 100% free with meals, swags, and certificates provided."),
                    EventFaqItem("Can I participate individually?", "Yes, individual participation is supported, though forming teams of 2 to 5 is encouraged.")
                ),
                contactEmail = "zayacodehub@gmail.com",
                contactPhone = "+91 98765 43210",
                websiteUrl = "https://admin.zayacodehub.in/hackathon2026",
                featured = true,
                status = "Upcoming"
            ),

            EventItem(
                id = "evt_codestorm_2026",
                title = "CodeStorm 2026: 24-Hour Competitive Coding League",
                tagline = "Test your Data Structures, Algorithms & Problem Solving Speed",
                category = "Coding Competitions",
                organizer = "ZAYA Competitive Programming Club",
                isVerifiedOrganizer = true,
                posterUrl = "",
                startDate = now + oneDay * 3,
                endDate = now + oneDay * 4,
                registrationDeadline = now + oneDay * 2,
                mode = "Online",
                venue = "Online Code Platform & Virtual Leaderboard",
                meetingLink = "https://meet.google.com/codestorm-2026",
                isFree = true,
                price = 0.0,
                prizePool = "₹75,000",
                eligibility = "All coding enthusiasts, engineers, and students",
                registrationMode = "individual_only",
                minTeamSize = 1,
                maxTeamSize = 1,
                capacity = 1000,
                registeredCount = 420,
                description = """CodeStorm is an intense 24-hour algorithmic showdown featuring problems crafted by competitive programmers. Solve challenging problems ranging from Dynamic Programming and Graph Theory to Number Theory and Greedy algorithms.

### Highlights:
- Live dynamic leaderboard with rating system.
- Direct interview fast-track opportunities with partner startups.
- Top 3 coders receive cash prizes, hardware swags, and gold badges.""",
                rules = listOf(
                    "Individual participation only.",
                    "Languages supported: C++, Java, Python, Kotlin, Rust.",
                    "Automated plagiarism and AI detection runs on every submission."
                ),
                agenda = listOf(
                    EventAgendaItem("08:00 PM", "Contest Launch & Problem Set Unlocking", "Problems 1-8 go live"),
                    EventAgendaItem("12:00 AM", "Bonus Hard Problems Release", "Extra challenge point tier opens"),
                    EventAgendaItem("08:00 PM (Next Day)", "Contest Ends & Final Standings", "Leaderboard frozen and results verified")
                ),
                faqs = listOf(
                    EventFaqItem("What platform is the contest hosted on?", "The contest runs on the official integrated Code Platform with real-time test cases."),
                    EventFaqItem("Is there negative marking for wrong submissions?", "Yes, wrong submissions incur a 10-minute penalty on time score.")
                ),
                contactEmail = "zayacodehub@gmail.com",
                featured = true,
                status = "Upcoming"
            ),

            EventItem(
                id = "evt_genai_masterclass",
                title = "Modern Android & Jetpack Compose + GenAI Masterclass",
                tagline = "Architect Production AI Apps with Compose, Room & Edge Models",
                category = "Workshops",
                organizer = "ZAYA CODE HUB Mobile Engineering",
                isVerifiedOrganizer = true,
                posterUrl = "",
                startDate = now + oneDay * 12,
                endDate = now + oneDay * 13,
                registrationDeadline = now + oneDay * 10,
                mode = "Online",
                venue = "Live Interactive Studio & YouTube Live Stream",
                meetingLink = "https://meet.google.com/zaya-masterclass",
                isFree = true,
                price = 0.0,
                prizePool = "Free Pro Certifications",
                eligibility = "Developers with basic Kotlin or Android knowledge",
                registrationMode = "individual_only",
                minTeamSize = 1,
                maxTeamSize = 1,
                capacity = 300,
                registeredCount = 210,
                description = """Deep dive into the architectural principles behind cutting-edge modern Android applications. Learn how to build 60/120 FPS Jetpack Compose interfaces, implement robust offline caching with Room & DataStore, and stream AI token responses smoothly without UI jank.

### What you will learn:
- Declarative UI and custom layout recycling.
- Edge AI inference vs Cloud AI gateway integration.
- Hardware-accelerated Canvas drawings and animations.""",
                rules = listOf(
                    "Participants should have Android Studio installed on their computer.",
                    "Interactive Q&A session at the end of each module."
                ),
                agenda = listOf(
                    EventAgendaItem("Session 1", "Compose Layout Internals & Performance Tuning", "Eliminating dropped frames in complex scroll views"),
                    EventAgendaItem("Session 2", "Integrating Local & Cloud LLMs in Android", "Streaming SSE responses into Markdown text in real time"),
                    EventAgendaItem("Session 3", "Project Build & Certification Exam", "Build a live app feature and earn a verified certificate")
                ),
                faqs = listOf(
                    EventFaqItem("Will recordings be provided?", "Yes, registered attendees will get permanent access to recordings and code repositories.")
                ),
                contactEmail = "zayacodehub@gmail.com",
                featured = false,
                status = "Upcoming"
            ),

            EventItem(
                id = "evt_web3_summit_2026",
                title = "Decentralized Systems & Web3 Builders Summit",
                tagline = "Explore Smart Contracts, Zero Knowledge Proofs & High Throughput Chains",
                category = "Conferences",
                organizer = "Web3 Developer Collective",
                isVerifiedOrganizer = true,
                posterUrl = "",
                startDate = now + oneDay * 20,
                endDate = now + oneDay * 21,
                registrationDeadline = now + oneDay * 15,
                mode = "Offline",
                venue = "Convention Centre, New Delhi",
                locationUrl = "https://maps.google.com/?q=New+Delhi+Convention+Centre",
                isFree = true,
                price = 0.0,
                prizePool = "₹1,00,000 Grant Fund",
                eligibility = "Open to developers, startup founders, and researchers",
                registrationMode = "both",
                minTeamSize = 1,
                maxTeamSize = 3,
                availableDomains = listOf("DeFi", "Infrastructure", "Layer 2", "Zero Knowledge", "NFTs & Gaming"),
                capacity = 400,
                registeredCount = 195,
                description = """A 2-day conference bringing together blockchain architects, cryptography researchers, and developers. Connect with founders, participate in hands-on smart contract security audits, and pitch your decentralized app for grant funding.""",
                rules = listOf(
                    "Physical badges will be issued at the registration counter via QR Ticket.",
                    "Government ID required for physical venue check-in."
                ),
                agenda = listOf(
                    EventAgendaItem("Day 1 - 10:00 AM", "Keynote: Scalable Distributed Ledgers", "State of modular architectures in 2026"),
                    EventAgendaItem("Day 1 - 02:00 PM", "Hands-on Workshop: Smart Contract Security", "Finding vulnerabilities before mainnet deployment"),
                    EventAgendaItem("Day 2 - 11:00 AM", "Builder Pitch Competition & Networking", "Top 5 projects awarded equity-free grants")
                ),
                faqs = listOf(
                    EventFaqItem("Is food provided?", "Yes, lunch and networking refreshments are provided for both days.")
                ),
                contactEmail = "zayacodehub@gmail.com",
                featured = false,
                status = "Upcoming"
            )
        )
    }
}
