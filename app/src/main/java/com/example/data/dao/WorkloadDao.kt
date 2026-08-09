package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkloadDao {
    // Subject
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    // Grade
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity): Long

    @Query("SELECT * FROM grades WHERE subjectId = :subjectId")
    fun getGradesForSubject(subjectId: Long): Flow<List<GradeEntity>>
    
    @Query("SELECT * FROM grades WHERE subjectId = :subjectId")
    suspend fun getGradesForSubjectSync(subjectId: Long): List<GradeEntity>

    @Delete
    suspend fun deleteGrade(grade: GradeEntity)

    // Class
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: ClassEntity): Long

    @Delete
    suspend fun deleteClass(schoolClass: ClassEntity)

    @Query("SELECT * FROM classes WHERE gradeId = :gradeId")
    fun getClassesForGrade(gradeId: Long): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE gradeId IN (SELECT gradeId FROM grades WHERE subjectId = :subjectId)")
    fun getClassesForSubject(subjectId: Long): Flow<List<ClassEntity>>
    
    @Query("SELECT * FROM classes WHERE gradeId IN (SELECT gradeId FROM grades WHERE subjectId = :subjectId)")
    suspend fun getClassesForSubjectSync(subjectId: Long): List<ClassEntity>

    // Teacher
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    // Cross Refs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherSubject(crossRef: TeacherSubjectCrossRef)
    
    @Query("DELETE FROM teacher_subjects WHERE teacherId = :teacherId")
    suspend fun deleteTeacherSubjects(teacherId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAllowedGrade(crossRef: TeacherAllowedGrade)
    
    @Query("DELETE FROM teacher_allowed_grades WHERE teacherId = :teacherId")
    suspend fun deleteTeacherAllowedGrades(teacherId: Long)

    @Query("SELECT s.* FROM subjects s INNER JOIN teacher_subjects ts ON s.subjectId = ts.subjectId WHERE ts.teacherId = :teacherId")
    suspend fun getTeacherSubjectsSync(teacherId: Long): List<SubjectEntity>

    @Query("SELECT t.* FROM teachers t INNER JOIN teacher_subjects ts ON t.teacherId = ts.teacherId WHERE ts.subjectId = :subjectId")
    fun getTeachersForSubject(subjectId: Long): Flow<List<TeacherEntity>>
    
    @Query("SELECT t.* FROM teachers t INNER JOIN teacher_subjects ts ON t.teacherId = ts.teacherId WHERE ts.subjectId = :subjectId AND t.isActive = 1")
    suspend fun getActiveTeachersForSubjectSync(subjectId: Long): List<TeacherEntity>

    @Query("SELECT * FROM teacher_allowed_grades WHERE teacherId = :teacherId")
    suspend fun getTeacherAllowedGradesSync(teacherId: Long): List<TeacherAllowedGrade>

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: DistributionSettings)

    @Query("SELECT * FROM distribution_settings WHERE id = 1")
    fun getSettings(): Flow<DistributionSettings?>
    
    @Query("SELECT * FROM distribution_settings WHERE id = 1")
    suspend fun getSettingsSync(): DistributionSettings?
}
