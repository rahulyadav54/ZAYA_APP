package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: AiZayaRepository) : ViewModel() {

    private val _quizQuestions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestionEntity>> = _quizQuestions.asStateFlow()

    var currentQuestionIndex by mutableIntStateOf(0)
    var selectedOption by mutableStateOf<String?>(null)
    var score by mutableIntStateOf(0)
    var quizCompleted by mutableStateOf(false)
    var isSeeding by mutableStateOf(false)
        private set

    init {
        loadQuizQuestions()
    }

    private fun loadQuizQuestions() {
        viewModelScope.launch {
            repository.allQuizQuestions.collect { questions ->
                if (questions.isEmpty() && !isSeeding) {
                    seedDefaultQuestions()
                } else {
                    _quizQuestions.value = questions.shuffled()
                }
            }
        }
    }

    private fun seedDefaultQuestions() {
        isSeeding = true
        viewModelScope.launch {
            defaultQuizQuestions.forEach { q ->
                repository.insertQuizQuestion(q)
            }
            isSeeding = false
        }
    }

    fun addQuestion(question: String, optionA: String, optionB: String, optionC: String, optionD: String, correctAnswer: String, category: String) {
        viewModelScope.launch {
            repository.insertQuizQuestion(
                QuizQuestionEntity(
                    question = question,
                    optionA = optionA,
                    optionB = optionB,
                    optionC = optionC,
                    optionD = optionD,
                    correctAnswer = correctAnswer,
                    category = category
                )
            )
        }
    }

    fun selectOption(option: String) {
        selectedOption = option
    }

    fun moveToNextQuestion() {
        val currentQuestion = quizQuestions.value.getOrNull(currentQuestionIndex)
        if (currentQuestion != null && selectedOption == currentQuestion.correctAnswer) {
            score++
        }

        if (currentQuestionIndex < quizQuestions.value.size - 1) {
            currentQuestionIndex++
            selectedOption = null
        } else {
            quizCompleted = true
        }
    }

    fun resetQuiz() {
        currentQuestionIndex = 0
        selectedOption = null
        score = 0
        quizCompleted = false
        _quizQuestions.value = _quizQuestions.value.shuffled()
    }
}

private val defaultQuizQuestions = listOf(
    // General Knowledge
    QuizQuestionEntity(question = "What is the capital of France?", optionA = "Berlin", optionB = "Madrid", optionC = "Paris", optionD = "Rome", correctAnswer = "Paris", category = "General Knowledge"),
    QuizQuestionEntity(question = "Which planet is known as the Red Planet?", optionA = "Earth", optionB = "Mars", optionC = "Jupiter", optionD = "Venus", correctAnswer = "Mars", category = "Science"),
    QuizQuestionEntity(question = "What is the chemical symbol for water?", optionA = "O2", optionB = "H2O", optionC = "CO2", optionD = "NaCl", correctAnswer = "H2O", category = "Science"),
    QuizQuestionEntity(question = "Who painted the Mona Lisa?", optionA = "Vincent van Gogh", optionB = "Pablo Picasso", optionC = "Leonardo da Vinci", optionD = "Claude Monet", correctAnswer = "Leonardo da Vinci", category = "General Knowledge"),
    QuizQuestionEntity(question = "What is the largest ocean on Earth?", optionA = "Atlantic Ocean", optionB = "Indian Ocean", optionC = "Arctic Ocean", optionD = "Pacific Ocean", correctAnswer = "Pacific Ocean", category = "General Knowledge"),

    // Mathematics
    QuizQuestionEntity(question = "What is the value of Ï€ (pi) to two decimal places?", optionA = "3.14", optionB = "3.16", optionC = "2.14", optionD = "3.41", correctAnswer = "3.14", category = "Mathematics"),
    QuizQuestionEntity(question = "What is the square root of 144?", optionA = "10", optionB = "12", optionC = "14", optionD = "16", correctAnswer = "12", category = "Mathematics"),
    QuizQuestionEntity(question = "What is 15% of 200?", optionA = "20", optionB = "25", optionC = "30", optionD = "35", correctAnswer = "30", category = "Mathematics"),

    // Science
    QuizQuestionEntity(question = "What gas do plants absorb from the atmosphere?", optionA = "Oxygen", optionB = "Nitrogen", optionC = "Carbon Dioxide", optionD = "Hydrogen", correctAnswer = "Carbon Dioxide", category = "Science"),
    QuizQuestionEntity(question = "What is the speed of light approximately?", optionA = "300,000 km/s", optionB = "150,000 km/s", optionC = "500,000 km/s", optionD = "100,000 km/s", correctAnswer = "300,000 km/s", category = "Science"),
    QuizQuestionEntity(question = "Which element has the atomic number 1?", optionA = "Helium", optionB = "Oxygen", optionC = "Hydrogen", optionD = "Carbon", correctAnswer = "Hydrogen", category = "Science"),
    QuizQuestionEntity(question = "What is the powerhouse of the cell?", optionA = "Nucleus", optionB = "Ribosome", optionC = "Mitochondria", optionD = "Golgi body", correctAnswer = "Mitochondria", category = "Science"),

    // Technology
    QuizQuestionEntity(question = "What does CPU stand for?", optionA = "Central Processing Unit", optionB = "Computer Personal Unit", optionC = "Central Program Utility", optionD = "Core Processing Unit", correctAnswer = "Central Processing Unit", category = "Technology"),
    QuizQuestionEntity(question = "Who is the founder of Microsoft?", optionA = "Steve Jobs", optionB = "Bill Gates", optionC = "Mark Zuckerberg", optionD = "Jeff Bezos", correctAnswer = "Bill Gates", category = "Technology"),
    QuizQuestionEntity(question = "What programming language is Android primarily built with?", optionA = "Python", optionB = "Swift", optionC = "Java/Kotlin", optionD = "C#", correctAnswer = "Java/Kotlin", category = "Technology"),

    // History
    QuizQuestionEntity(question = "In which year did World War II end?", optionA = "1943", optionB = "1944", optionC = "1945", optionD = "1946", correctAnswer = "1945", category = "History"),
    QuizQuestionEntity(question = "Who was the first person to walk on the Moon?", optionA = "Buzz Aldrin", optionB = "Yuri Gagarin", optionC = "Neil Armstrong", optionD = "Michael Collins", correctAnswer = "Neil Armstrong", category = "History"),
    QuizQuestionEntity(question = "Which ancient civilization built the pyramids?", optionA = "Roman", optionB = "Greek", optionC = "Egyptian", optionD = "Mesopotamian", correctAnswer = "Egyptian", category = "History"),

    // English
    QuizQuestionEntity(question = "What is the past tense of 'run'?", optionA = "Runned", optionB = "Ran", optionC = "Running", optionD = "Runed", correctAnswer = "Ran", category = "English"),
    QuizQuestionEntity(question = "Which is a synonym for 'happy'?", optionA = "Sad", optionB = "Angry", optionC = "Joyful", optionD = "Tired", correctAnswer = "Joyful", category = "English")
)

