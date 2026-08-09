package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkloadRepository
import com.example.data.entities.SubjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectViewModel(private val repository: WorkloadRepository) : ViewModel() {
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getGradesForSubject(subjectId: Long) = repository.getGradesForSubject(subjectId)
    fun getClassesForGrade(gradeId: Long) = repository.getClassesForGrade(gradeId)

    fun addSubject(name: String, color: String = "", icon: String = "", notes: String = "") {
        viewModelScope.launch {
            repository.insertSubject(SubjectEntity(name = name, color = color, icon = icon, notes = notes))
        }
    }

    fun addGradeWithClasses(subjectId: Long, name: String, workloadPerClass: Int, numberOfClasses: Int) {
        viewModelScope.launch {
            val gradeId = repository.insertGrade(com.example.data.entities.GradeEntity(subjectId = subjectId, name = name, workloadPerClass = workloadPerClass))
            for (i in 1..numberOfClasses) {
                repository.insertClass(com.example.data.entities.ClassEntity(gradeId = gradeId, name = "$name $i"))
            }
        }
    }

    fun addGrade(subjectId: Long, name: String, workloadPerClass: Int) {
        viewModelScope.launch {
            repository.insertGrade(com.example.data.entities.GradeEntity(subjectId = subjectId, name = name, workloadPerClass = workloadPerClass))
        }
    }

    fun addClass(gradeId: Long, name: String) {
        viewModelScope.launch {
            repository.insertClass(com.example.data.entities.ClassEntity(gradeId = gradeId, name = name))
        }
    }

    fun deleteClass(classEntity: com.example.data.entities.ClassEntity) {
        viewModelScope.launch {
            repository.deleteClass(classEntity)
        }
    }

    fun deleteGrade(gradeEntity: com.example.data.entities.GradeEntity) {
        viewModelScope.launch {
            repository.deleteGrade(gradeEntity)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}
