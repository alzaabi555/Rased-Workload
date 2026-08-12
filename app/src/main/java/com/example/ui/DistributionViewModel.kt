package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkloadRepository
import com.example.data.entities.DistributionSettings
import com.example.data.entities.SubjectEntity
import com.example.data.entities.GradeEntity
import com.example.data.entities.ClassEntity
import com.example.domain.DistributionEngine
import com.example.domain.DistributionResult
import com.example.domain.Assignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class DistributionViewModel(private val repository: WorkloadRepository, private val application: Application) : ViewModel() {

    private val engine = DistributionEngine()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, DistributionResult::class.java)
    private val adapter = moshi.adapter<List<DistributionResult>>(listType)
    
    private val _distributionResults = MutableStateFlow<List<DistributionResult>?>(null)
    val distributionResults: StateFlow<List<DistributionResult>?> = _distributionResults
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    private var currentSubjectId: Long? = null

    fun loadOrDistribute(subject: SubjectEntity) {
        viewModelScope.launch {
            val file = File(application.filesDir, "distribution_subject_\${subject.subjectId}.json")
            if (file.exists()) {
                try {
                    val json = file.readText()
                    val results = adapter.fromJson(json)
                    _distributionResults.value = results
                    currentSubjectId = subject.subjectId
                } catch (e: Exception) {
                    e.printStackTrace()
                    distribute(subject)
                }
            } else {
                distribute(subject)
                currentSubjectId = subject.subjectId
            }
        }
    }

    private fun saveCurrentDistribution() {
        val subjectId = currentSubjectId ?: return
        val results = _distributionResults.value ?: return
        viewModelScope.launch {
            try {
                val json = adapter.toJson(results)
                val file = File(application.filesDir, "distribution_subject_\${subject.subjectId}.json")
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun distribute(subject: SubjectEntity) {
        viewModelScope.launch {
            _isProcessing.value = true
            currentSubjectId = subject.subjectId
            
            // Gather data for subject
            val grades = repository.getGradesForSubjectSync(subject.subjectId)
            val classes = repository.getClassesForSubjectSync(subject.subjectId)
            val teachers = repository.getActiveTeachersForSubjectSync(subject.subjectId)
            
            val allowedGradesMap = teachers.associate { t ->
                t.teacherId to repository.getTeacherAllowedGradesSync(t.teacherId)
            }
            
            val settings = repository.getSettingsSync() ?: DistributionSettings()
            
            val results = mutableListOf<DistributionResult>()
            for (i in 0 until 3) {
                val result = engine.distribute(
                    subject = subject,
                    grades = grades,
                    classes = classes,
                    teachers = teachers,
                    teacherAllowedGrades = allowedGradesMap,
                    settings = settings,
                    variationSeed = i
                )
                results.add(result)
            }
            
            // Sort results by score (descending so higher is better, or evaluate based on logic)
            // Assuming higher score is better:
            results.sortByDescending { it.evaluation.score }
            
            _distributionResults.value = results
            saveCurrentDistribution()
            _isProcessing.value = false
        }
    }

    fun reassignClass(resultIndex: Int, assignment: Assignment, newTeacherId: Long, newTeacherName: String) {
        val results = _distributionResults.value?.toMutableList() ?: return
        val result = results[resultIndex]
        val newAssignments = result.assignments.map {
            if (it.classId == assignment.classId) {
                it.copy(teacherId = newTeacherId, teacherName = newTeacherName)
            } else it
        }
        results[resultIndex] = result.copy(assignments = newAssignments)
        _distributionResults.value = results
        saveCurrentDistribution()
    }

    fun swapAssignments(resultIndex: Int, assignment1: Assignment, assignment2: Assignment) {
        val results = _distributionResults.value?.toMutableList() ?: return
        val result = results[resultIndex]
        val newAssignments = result.assignments.map {
            when (it.classId) {
                assignment1.classId -> it.copy(teacherId = assignment2.teacherId, teacherName = assignment2.teacherName)
                assignment2.classId -> it.copy(teacherId = assignment1.teacherId, teacherName = assignment1.teacherName)
                else -> it
            }
        }
        results[resultIndex] = result.copy(assignments = newAssignments)
        _distributionResults.value = results
        saveCurrentDistribution()
    }

    fun assignUnassignedClass(resultIndex: Int, classInfo: com.example.domain.UnassignedClassInfo, newTeacherId: Long, newTeacherName: String) {
        val results = _distributionResults.value?.toMutableList() ?: return
        val result = results[resultIndex]
        val classEntity = classInfo.classEntity
        val newAssignment = Assignment(newTeacherId, newTeacherName, classEntity.classId, classEntity.name, classEntity.gradeId, classInfo.gradeName, classInfo.workload)
        
        val newAssignments = result.assignments.toMutableList().apply { add(newAssignment) }
        val newUnassigned = result.unassignedClasses.filter { it.classEntity.classId != classEntity.classId }
        
        results[resultIndex] = result.copy(assignments = newAssignments, unassignedClasses = newUnassigned)
        _distributionResults.value = results
        saveCurrentDistribution()
    }
}
