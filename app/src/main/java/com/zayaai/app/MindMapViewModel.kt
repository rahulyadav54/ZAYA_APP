package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MindMapViewModel(private val repository: AiZayaRepository) : ViewModel() {

    val nodes: StateFlow<List<MindMapNodeEntity>> = repository.getMindMapNodes("default_map")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNode(text: String, x: Float, y: Float) {
        viewModelScope.launch {
            repository.insertMindMapNode(
                MindMapNodeEntity(mapId = "default_map", text = text, positionX = x, positionY = y)
            )
        }
    }

    fun updateNodePosition(node: MindMapNodeEntity, x: Float, y: Float) {
        viewModelScope.launch {
            repository.insertMindMapNode(node.copy(positionX = x, positionY = y))
        }
    }

    fun deleteNode(node: MindMapNodeEntity) {
        viewModelScope.launch {
            repository.deleteMindMapNode(node)
        }
    }
}

