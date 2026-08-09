package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkloadRepository
import com.example.data.entities.TeacherEntity
import com.example.data.entities.TeacherRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeacherViewModel(private val repository: WorkloadRepository) : ViewModel() {
    val teachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTeacher(name: String, role: TeacherRole, maxWorkload: Int, reduction: Int, subjects: List<Long> = emptyList(), allowedGrades: List<Long> = emptyList()) {
        viewModelScope.launch {
            repository.insertTeacher(
                TeacherEntity(
                    name = name,
                    role = role,
                    maxWorkload = maxWorkload,
                    reductionAmount = reduction
                ),
                subjects = subjects,
                allowedGrades = allowedGrades.map { com.example.data.entities.TeacherAllowedGrade(teacherId = 0, gradeId = it) }
            )
        }
    }
    
    fun updateTeacher(teacher: TeacherEntity, subjects: List<Long> = emptyList(), allowedGrades: List<Long> = emptyList()) {
        viewModelScope.launch {
            repository.updateTeacher(
                teacher,
                subjects = subjects,
                allowedGrades = allowedGrades.map { com.example.data.entities.TeacherAllowedGrade(teacherId = teacher.teacherId, gradeId = it) }
            )
        }
    }
    
    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
        }
    }
    
    suspend fun getTeacherSubjectsSync(teacherId: Long): List<com.example.data.entities.SubjectEntity> {
        return repository.getTeacherSubjectsSync(teacherId)
    }

    suspend fun getTeacherAllowedGradesSync(teacherId: Long): List<com.example.data.entities.TeacherAllowedGrade> {
        return repository.getTeacherAllowedGradesSync(teacherId)
    }
}
