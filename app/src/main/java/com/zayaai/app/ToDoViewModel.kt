package com.zayaai.app

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ToDoTask(
    val id: Long = 0L,
    val task: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class ToDoViewModel(private val repository: AiZayaRepository) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: String) {
        viewModelScope.launch {
            repository.insertTask(TaskEntity(task = task))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            // We need a TaskEntity to delete, or a deleteById in DAO. 
            // Since DAO has @Delete, we find it or just create a dummy with ID.
            repository.deleteTask(TaskEntity(id = id, task = ""))
        }
    }
}

