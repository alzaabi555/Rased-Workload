package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Junction

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val subjectId: Long = 0,
    val name: String,
    val color: String = "",
    val icon: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class GradeEntity(
    @PrimaryKey(autoGenerate = true) val gradeId: Long = 0,
    val subjectId: Long,
    val name: String,
    val workloadPerClass: Int
)

@Entity(
    tableName = "classes",
    foreignKeys = [
        ForeignKey(
            entity = GradeEntity::class,
            parentColumns = ["gradeId"],
            childColumns = ["gradeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gradeId")]
)
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val classId: Long = 0,
    val gradeId: Long,
    val name: String,
    val isExcluded: Boolean = false,
    val fixedTeacherId: Long? = null // if assigned to a teacher permanently
)

enum class TeacherRole {
    TEACHER, SENIOR_TEACHER, COORDINATOR, HEAD_OF_DEPARTMENT, ADMIN_TASKS, CUSTOM
}

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val teacherId: Long = 0,
    val name: String,
    val employeeId: String = "",
    val phone: String = "",
    val email: String = "",
    val role: TeacherRole = TeacherRole.TEACHER,
    val maxWorkload: Int = 20, // default limit
    val reductionAmount: Int = 0,
    val maxDifferentGrades: Int = 2,
    val isActive: Boolean = true,
    val notes: String = ""
)

@Entity(
    tableName = "teacher_subjects",
    primaryKeys = ["teacherId", "subjectId"],
    foreignKeys = [
        ForeignKey(entity = TeacherEntity::class, parentColumns = ["teacherId"], childColumns = ["teacherId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SubjectEntity::class, parentColumns = ["subjectId"], childColumns = ["subjectId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("subjectId")]
)
data class TeacherSubjectCrossRef(
    val teacherId: Long,
    val subjectId: Long
)

@Entity(
    tableName = "teacher_allowed_grades",
    primaryKeys = ["teacherId", "gradeId"],
    foreignKeys = [
        ForeignKey(entity = TeacherEntity::class, parentColumns = ["teacherId"], childColumns = ["teacherId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = GradeEntity::class, parentColumns = ["gradeId"], childColumns = ["gradeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("gradeId")]
)
data class TeacherAllowedGrade(
    val teacherId: Long,
    val gradeId: Long,
    val isPreferred: Boolean = false
)

// To hold settings/templates
@Entity(tableName = "distribution_settings")
data class DistributionSettings(
    @PrimaryKey val id: Long = 1,
    val schoolName: String = "",
    val preparerName: String = "",
    val defaultMaxWorkload: Int = 20,
    val defaultMinWorkload: Int = 16,
    val coordinatorReduction: Int = 4,
    val seniorTeacherReduction: Int = 2,
    val defaultMaxGrades: Int = 2,
    val allowEmptyAssignments: Boolean = false,
    val prioritizeSeniorTeachers: Boolean = true
)
