package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zayaai.app.NotesViewModel
import com.zayaai.app.FlashcardsViewModel
import com.zayaai.app.QuizViewModel
import com.zayaai.app.ToDoViewModel
import com.zayaai.app.StudyPlannerViewModel
import com.zayaai.app.MindMapViewModel
import com.zayaai.app.FormulaViewModel
import com.zayaai.app.BookmarkViewModel
import com.zayaai.app.StudyArticlesViewModel
import com.zayaai.app.DictionaryViewModel

class ViewModelFactory(private val repository: AiZayaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> NotesViewModel(repository) as T
            modelClass.isAssignableFrom(FlashcardsViewModel::class.java) -> FlashcardsViewModel(repository) as T
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> QuizViewModel(repository) as T
            modelClass.isAssignableFrom(ToDoViewModel::class.java) -> ToDoViewModel(repository) as T
            modelClass.isAssignableFrom(StudyPlannerViewModel::class.java) -> StudyPlannerViewModel(repository) as T
            modelClass.isAssignableFrom(MindMapViewModel::class.java) -> MindMapViewModel(repository) as T
            modelClass.isAssignableFrom(FormulaViewModel::class.java) -> FormulaViewModel(repository) as T
            modelClass.isAssignableFrom(BookmarkViewModel::class.java) -> BookmarkViewModel(repository) as T
            modelClass.isAssignableFrom(StudyArticlesViewModel::class.java) -> StudyArticlesViewModel(repository) as T
            modelClass.isAssignableFrom(DictionaryViewModel::class.java) -> DictionaryViewModel() as T
            modelClass.isAssignableFrom(TextWriterViewModel::class.java) -> TextWriterViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class StudyMaterialViewModelFactory(
    private val repository: AiZayaRepository,
    private val db: AiZayaDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyMaterialViewModel::class.java)) {
            return StudyMaterialViewModel(repository, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


