package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyArticlesViewModel(private val repository: AiZayaRepository) : ViewModel() {
    val articles = repository.allArticles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addArticle(title: String, content: String, category: String) {
        viewModelScope.launch {
            val article = StudyArticleEntity(
                title = title,
                content = content,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            repository.insertArticle(article)
        }
    }

    fun deleteArticle(article: StudyArticleEntity) {
        viewModelScope.launch {
            repository.deleteArticle(article)
        }
    }
}

