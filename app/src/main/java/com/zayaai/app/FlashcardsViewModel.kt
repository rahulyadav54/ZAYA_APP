package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlashcardsViewModel(private val repository: AiZayaRepository) : ViewModel() {

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFlashcard(front: String, back: String, category: String = "General") {
        viewModelScope.launch {
            repository.insertFlashcard(FlashcardEntity(front = front, back = back, category = category))
        }
    }

    fun deleteFlashcard(flashcard: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
        }
    }
}

