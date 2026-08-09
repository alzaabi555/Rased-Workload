package com.example.data

import com.example.data.dao.WorkloadDao
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

class WorkloadRepository(private val dao: WorkloadDao) {

    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()
    val settings: Flow<DistributionSettings?> = dao.getSettings()

    suspend fun insertSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    suspend fun insertGrade(grade: GradeEntity): Long = dao.insertGrade(grade)
    suspend fun deleteGrade(grade: GradeEntity) = dao.deleteGrade(grade)
    fun getGradesForSubject(subjectId: Long): Flow<List<GradeEntity>> = dao.getGradesForSubject(subjectId)
    suspend fun getGradesForSubjectSync(subjectId: Long): List<GradeEntity> = dao.getGradesForSubjectSync(subjectId)

    suspend fun insertClass(schoolClass: ClassEntity): Long = dao.insertClass(schoolClass)
    suspend fun deleteClass(schoolClass: ClassEntity) = dao.deleteClass(schoolClass)
    fun getClassesForGrade(gradeId: Long): Flow<List<ClassEntity>> = dao.getClassesForGrade(gradeId)
    fun getClassesForSubject(subjectId: Long): Flow<List<ClassEntity>> = dao.getClassesForSubject(subjectId)
    suspend fun getClassesForSubjectSync(subjectId: Long): List<ClassEntity> = dao.getClassesForSubjectSync(subjectId)

    suspend fun insertTeacher(teacher: TeacherEntity, subjects: List<Long>, allowedGrades: List<TeacherAllowedGrade>) {
        val teacherId = dao.insertTeacher(teacher)
        updateTeacherRelations(teacherId, subjects, allowedGrades)
    }

    suspend fun updateTeacher(teacher: TeacherEntity, subjects: List<Long>, allowedGrades: List<TeacherAllowedGrade>) {
        dao.updateTeacher(teacher)
        updateTeacherRelations(teacher.teacherId, subjects, allowedGrades)
    }

    private suspend fun updateTeacherRelations(teacherId: Long, subjects: List<Long>, allowedGrades: List<TeacherAllowedGrade>) {
        dao.deleteTeacherSubjects(teacherId)
        dao.deleteTeacherAllowedGrades(teacherId)
        
        subjects.forEach { subjectId ->
            dao.insertTeacherSubject(TeacherSubjectCrossRef(teacherId, subjectId))
        }
        allowedGrades.forEach { grade ->
            dao.insertTeacherAllowedGrade(grade.copy(teacherId = teacherId))
        }
    }

    suspend fun deleteTeacher(teacher: TeacherEntity) = dao.deleteTeacher(teacher)

    fun getTeachersForSubject(subjectId: Long): Flow<List<TeacherEntity>> = dao.getTeachersForSubject(subjectId)
    suspend fun getActiveTeachersForSubjectSync(subjectId: Long): List<TeacherEntity> = dao.getActiveTeachersForSubjectSync(subjectId)
    
    suspend fun getTeacherSubjectsSync(teacherId: Long): List<SubjectEntity> = dao.getTeacherSubjectsSync(teacherId)

    suspend fun getTeacherAllowedGradesSync(teacherId: Long): List<TeacherAllowedGrade> = dao.getTeacherAllowedGradesSync(teacherId)

    suspend fun insertSettings(settings: DistributionSettings) = dao.insertSettings(settings)
    suspend fun getSettingsSync(): DistributionSettings? = dao.getSettingsSync()
}
