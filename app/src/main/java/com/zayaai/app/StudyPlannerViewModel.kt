package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyPlannerViewModel(private val repository: AiZayaRepository) : ViewModel() {

    val studyPlans: StateFlow<List<StudyPlanEntity>> = repository.allStudyPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlan(subject: String, date: Long, startTime: String, endTime: String) {
        viewModelScope.launch {
            repository.insertStudyPlan(
                StudyPlanEntity(
                    subject = subject,
                    date = date,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    }

    fun deletePlan(plan: StudyPlanEntity) {
        viewModelScope.launch {
            repository.deleteStudyPlan(plan)
        }
    }
}

