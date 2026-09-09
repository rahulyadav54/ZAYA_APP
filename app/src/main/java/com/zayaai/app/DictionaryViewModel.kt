package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DictionaryViewModel : ViewModel() {
    private val _searchResult = MutableStateFlow("")
    val searchResult: StateFlow<String> = _searchResult

    // Keep old name for backward compat but map to single result
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchWord(word: String) {
        if (word.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = AIService.generate(
                    prompt = "Define the word: \"$word\"",
                    systemPrompt = """You are a comprehensive multilingual dictionary designed for students.
For the given English word, provide a well-structured response using Markdown formatting exactly as shown below.
Use clear headings, bullet points, and bold text for readability.

## [WORD] — *[part of speech]*

### Pronunciation
`/phonetic transcription/`

### English Definition
Give 1-2 clear, simple definitions that a student can easily understand.

### Example Sentences
- **Sentence 1:** (a simple example)
- **Sentence 2:** (an academic/contextual example)

### Nepali Meaning
Give the Nepali translation/meaning in Devanagari script with a short example sentence in Nepali.

### Hindi Meaning
Give the Hindi translation/meaning in Devanagari script with a short example sentence in Hindi.

### Tamil Meaning
Give the Tamil translation/meaning in Tamil script with a short example sentence in Tamil.

### Synonyms
List 4-5 synonyms separated by commas.

### Antonyms
List 3-4 antonyms separated by commas (write "N/A" if none).

### Did You Know?
A fun or interesting fact, etymology, or memory tip related to this word that helps students remember it.

IMPORTANT: Always respond with accurate translations. Use proper Devanagari for Nepali and Hindi, and proper Tamil script for Tamil. Keep language simple and student-friendly."""
                )
                _searchResult.value = result
                _searchResults.value = listOf(result)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage ?: "Failed to look up word."}"
                _searchResult.value = errorMsg
                _searchResults.value = listOf(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _searchResult.value = ""
        _searchResults.value = emptyList()
    }
}

