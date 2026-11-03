package com.zayaai.app

sealed class AppVoiceAction {
    data class Navigate(val route: String, val spokenFeedback: String) : AppVoiceAction()
    data class GoBack(val spokenFeedback: String) : AppVoiceAction()
}

object AppVoiceCommandHandler {

    /**
     * Parse recognized speech and determine if it is an in-app tool or navigation command.
     * Returns an [AppVoiceAction] if matched, or null if it's general conversational speech.
     */
    fun matchCommand(rawQuery: String): AppVoiceAction? {
        val q = rawQuery.lowercase().trim()
            .replace(Regex("^(please|can you|could you|hey zaya|zaya|ai zaya|i want to|kindly)\\s+"), "")
            .trim()

        // ─── BACK / CLOSE NAVIGATION ───
        if (q in listOf("close this", "close", "go back", "back", "exit", "close screen", "return", "close chat", "exit screen")) {
            return AppVoiceAction.GoBack("Closing and going back.")
        }
        if (q.startsWith("close ") || q.startsWith("exit ") || q == "leave") {
            return AppVoiceAction.GoBack("Going back now.")
        }

        // ─── HOME ───
        if (q.contains("home") || q in listOf("main menu", "dashboard", "main screen")) {
            return AppVoiceAction.Navigate(Screen.Home.route, "Taking you to the Home screen.")
        }

        // ─── AI CHAT ───
        if (q.contains("ai chat") || q.contains("start chat") || q.contains("open chat") ||
            q.contains("chat with ai") || q == "chat" || q.endsWith(" chat")) {
            return AppVoiceAction.Navigate(Screen.AIChat.route, "Opening AI Chat for you now.")
        }

        // ─── EVENTS HUB / HACKATHON ───
        if (q.contains("event") || q.contains("hackathon") || q.contains("zayathon") ||
            q.contains("ticket") || q.contains("competition") || q.contains("workshop")) {
            return AppVoiceAction.Navigate(Screen.EventsHub.route, "Opening Events Hub and Hackathons.")
        }

        // ─── IMAGE GENERATOR ───
        if (q.contains("image generator") || q.contains("generate image") || q.contains("create image") ||
            q.contains("ai image") || q.contains("image gen") || q.contains("make image")) {
            return AppVoiceAction.Navigate(Screen.ImageGenerator.route, "Launching Image Generator.")
        }

        // ─── CODE GENERATOR ───
        if (q.contains("code generator") || q.contains("generate code") || q.contains("coding tool") ||
            q.contains("code gen") || q.contains("write code") || q.contains("compiler")) {
            return AppVoiceAction.Navigate(Screen.CodeGenerator.route, "Opening Code Generator.")
        }

        // ─── TEXT WRITER ───
        if (q.contains("text writer") || q.contains("write essay") || q.contains("write article") ||
            q.contains("essay writer") || q.contains("article writer") || q.contains("copywriter")) {
            return AppVoiceAction.Navigate(Screen.TextWriter.route, "Opening Text Writer.")
        }

        // ─── QUIZ GENERATOR ───
        if (q.contains("quiz generator") || q.contains("create quiz") || q.contains("quiz gen") ||
            q.contains("take quiz") || q.contains("practice quiz") || q == "quiz") {
            return AppVoiceAction.Navigate(Screen.QuizGenerator.route, "Opening Quiz Generator.")
        }

        // ─── DOCUMENT ANALYZER ───
        if (q.contains("document analyzer") || q.contains("analyze document") || q.contains("pdf analyzer") ||
            q.contains("doc analyzer") || q.contains("analyze pdf") || q.contains("pdf")) {
            return AppVoiceAction.Navigate(Screen.DocumentAnalyzer.route, "Opening Document Analyzer.")
        }

        // ─── PPT GENERATOR ───
        if (q.contains("ppt generator") || q.contains("presentation generator") || q.contains("slides generator") ||
            q.contains("powerpoint") || q.contains("make slides") || q.contains("presentation")) {
            return AppVoiceAction.Navigate(Screen.PPTGenerator.route, "Opening PPT Presentation Generator.")
        }

        // ─── PROMPT ENGINEER ───
        if (q.contains("prompt engineer") || q.contains("prompt library") || q.contains("prompts")) {
            return AppVoiceAction.Navigate(Screen.PromptEngineer.route, "Opening Prompt Engineer.")
        }

        // ─── SCIENTIFIC CALCULATOR ───
        if (q.contains("calculator") || q.contains("scientific calculator") || q.contains("math calculator") || q == "calc") {
            return AppVoiceAction.Navigate(Screen.Calculator.route, "Opening Scientific Calculator.")
        }

        // ─── UNIT CONVERTER ───
        if (q.contains("unit converter") || q.contains("convert unit") || q.contains("converter") || q.contains("measurement")) {
            return AppVoiceAction.Navigate(Screen.UnitConverter.route, "Opening Unit Converter.")
        }

        // ─── WORD COUNTER ───
        if (q.contains("word counter") || q.contains("word count") || q.contains("count words")) {
            return AppVoiceAction.Navigate(Screen.WordCounter.route, "Opening Word Counter.")
        }

        // ─── DICTIONARY ───
        if (q.contains("dictionary") || q.contains("vocab") || q.contains("meaning of word") || q.contains("thesaurus")) {
            return AppVoiceAction.Navigate(Screen.Dictionary.route, "Opening Dictionary.")
        }

        // ─── NOTES ───
        if (q.contains("notes") || q.contains("my notes") || q.contains("take a note") || q.contains("notebook") || q == "note") {
            return AppVoiceAction.Navigate(Screen.Notes.route, "Opening your Notes.")
        }

        // ─── STUDY PLANNER ───
        if (q.contains("study planner") || q.contains("planner") || q.contains("study schedule") || q.contains("my timetable")) {
            return AppVoiceAction.Navigate(Screen.StudyPlanner.route, "Opening Study Planner.")
        }

        // ─── POMODORO TIMER ───
        if (q.contains("pomodoro") || q.contains("focus timer") || q.contains("start timer") || q.contains("timer")) {
            return AppVoiceAction.Navigate(Screen.Pomodoro.route, "Opening Pomodoro Focus Timer.")
        }

        // ─── FLASHCARDS ───
        if (q.contains("flashcard") || q.contains("study cards") || q.contains("cards")) {
            return AppVoiceAction.Navigate(Screen.Flashcards.route, "Opening Flashcards.")
        }

        // ─── FORMULA LIBRARY ───
        if (q.contains("formula") || q.contains("formulas") || q.contains("equation")) {
            return AppVoiceAction.Navigate(Screen.FormulaLibrary.route, "Opening Formula Library.")
        }

        // ─── MIND MAP ───
        if (q.contains("mind map") || q.contains("concept map") || q.contains("brainstorm")) {
            return AppVoiceAction.Navigate(Screen.MindMap.route, "Opening Mind Map.")
        }

        // ─── STUDENT TOOLS ───
        if (q.contains("student tools") || q.contains("study tools") || q.contains("academic tools")) {
            return AppVoiceAction.Navigate(Screen.StudentTools.route, "Opening Student Tools catalog.")
        }

        // ─── PROFILE ───
        if (q.contains("profile") || q.contains("my profile") || q.contains("account info") || q.contains("my account")) {
            return AppVoiceAction.Navigate(Screen.Profile.route, "Opening your Profile.")
        }

        // ─── SETTINGS ───
        if (q.contains("settings") || q.contains("preference") || q.contains("configure")) {
            return AppVoiceAction.Navigate(Screen.Settings.route, "Opening Settings.")
        }

        // ─── CHAT HISTORY ───
        if (q.contains("history") || q.contains("past chat") || q.contains("saved conversations")) {
            return AppVoiceAction.Navigate(Screen.History.route, "Opening Chat History.")
        }

        return null
    }
}

