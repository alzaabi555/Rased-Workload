package com.example.domain

import com.example.data.entities.*
import kotlin.math.abs
import kotlin.math.max

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnassignedClassInfo(
    val classEntity: ClassEntity,
    val gradeName: String,
    val workload: Int
)

@JsonClass(generateAdapter = true)
data class DistributionResult(
    val assignments: List<Assignment>,
    val unassignedClasses: List<UnassignedClassInfo>,
    val evaluation: DistributionEvaluation,
    val isFeasible: Boolean,
    val errors: List<String>
)

@JsonClass(generateAdapter = true)
data class Assignment(
    val teacherId: Long,
    val teacherName: String,
    val classId: Long,
    val className: String,
    val gradeId: Long,
    val gradeName: String,
    val workload: Int
)

@JsonClass(generateAdapter = true)
data class DistributionEvaluation(
    val score: Int, // out of 100
    val maxWorkloadExceededCount: Int,
    val maxGradesExceededCount: Int,
    val unassignedCount: Int,
    val averageOccupancy: Double,
    val maxOccupancy: Double,
    val minOccupancy: Double
)

class DistributionEngine {

    fun calculateActualWorkload(teacher: TeacherEntity): Int {
        return max(0, teacher.maxWorkload - teacher.reductionAmount)
    }

    suspend fun distribute(
        subject: SubjectEntity,
        grades: List<GradeEntity>,
        classes: List<ClassEntity>,
        teachers: List<TeacherEntity>,
        teacherAllowedGrades: Map<Long, List<TeacherAllowedGrade>>,
        settings: DistributionSettings,
        variationSeed: Int = 0
    ): DistributionResult {
        val assignments = mutableListOf<Assignment>()
        val errors = mutableListOf<String>()
        val unassignedClasses = mutableListOf<UnassignedClassInfo>()

        val teacherMap = teachers.associateBy { it.teacherId }
        val gradeMap = grades.associateBy { it.gradeId }
        val classMap = classes.associateBy { it.classId }
        
        // Calculate actual capacity
        val teacherCapacities = teachers.associate { it.teacherId to calculateActualWorkload(it) }.toMutableMap()
        val teacherAssignedLoads = teachers.associate { it.teacherId to 0 }.toMutableMap()
        val teacherAssignedGrades = teachers.associate { it.teacherId to mutableSetOf<Long>() }
        
        // 1. Assign fixed classes
        val remainingClasses = mutableListOf<ClassEntity>()
        for (c in classes) {
            if (c.isExcluded) continue
            
            if (c.fixedTeacherId != null && teacherMap.containsKey(c.fixedTeacherId)) {
                val tId = c.fixedTeacherId
                val grade = gradeMap[c.gradeId]!!
                assignments.add(Assignment(tId, teacherMap[tId]!!.name, c.classId, c.name, c.gradeId, grade.name, grade.workloadPerClass))
                teacherAssignedLoads[tId] = (teacherAssignedLoads[tId] ?: 0) + grade.workloadPerClass
                teacherAssignedGrades[tId]?.add(c.gradeId)
            } else {
                remainingClasses.add(c)
            }
        }
        
        // Sort remaining classes to assign (larger workload first, or by grade)
        when (variationSeed % 3) {
            0 -> remainingClasses.sortByDescending { gradeMap[it.gradeId]?.workloadPerClass ?: 0 }
            1 -> remainingClasses.shuffle(kotlin.random.Random(System.currentTimeMillis() + variationSeed))
            2 -> remainingClasses.sortBy { it.classId }
        }

        // 2. Greedy Assignment
        for (c in remainingClasses) {
            val grade = gradeMap[c.gradeId] ?: continue
            val workload = grade.workloadPerClass
            
            // Find best teacher
            var bestTeacherId: Long? = null
            var bestScore = Double.MAX_VALUE
            
            val shuffledTeachers = teachers.shuffled(kotlin.random.Random(System.currentTimeMillis() + c.classId + variationSeed))
            for (teacher in shuffledTeachers) {
                val tId = teacher.teacherId
                val capacity = teacherCapacities[tId] ?: 0
                val currentLoad = teacherAssignedLoads[tId] ?: 0
                val currentGrades = teacherAssignedGrades[tId] ?: emptySet()
                
                // Hard Constraints
                if (currentLoad + workload > capacity) continue // Exceeds load
                
                val allowed = teacherAllowedGrades[tId] ?: emptyList()
                if (allowed.isNotEmpty() && allowed.none { it.gradeId == c.gradeId }) continue // Not allowed
                
                // Soft constraints score (lower is better)
                val newGradesCount = if (currentGrades.contains(c.gradeId)) currentGrades.size else currentGrades.size + 1
                val gradePenalty = if (newGradesCount > teacher.maxDifferentGrades) 1000.0 else if (currentGrades.contains(c.gradeId)) 0.0 else 10.0
                
                // 2. Prefer balancing occupancy
                val occupancyAfter = (currentLoad + workload).toDouble() / (capacity.takeIf { it > 0 } ?: 1)
                
                val score = gradePenalty + occupancyAfter + (if (variationSeed > 0) kotlin.random.Random.nextDouble(0.0, 5.0 * variationSeed) else 0.0)
                
                if (score < bestScore) {
                    bestScore = score
                    bestTeacherId = tId
                }
            }
            
            if (bestTeacherId != null) {
                assignments.add(Assignment(bestTeacherId, teacherMap[bestTeacherId]!!.name, c.classId, c.name, c.gradeId, grade.name, workload))
                teacherAssignedLoads[bestTeacherId] = (teacherAssignedLoads[bestTeacherId] ?: 0) + workload
                teacherAssignedGrades[bestTeacherId]?.add(c.gradeId)
            } else {
                unassignedClasses.add(UnassignedClassInfo(c, grade.name, workload))
            }
        }
        
        if (unassignedClasses.isNotEmpty()) {
            errors.add("Could not assign ${unassignedClasses.size} classes due to constraints.")
        }
        
        // Evaluate
        val evaluation = evaluate(assignments, unassignedClasses, teachers, teacherCapacities, teacherAssignedGrades)
        
        return DistributionResult(
            assignments = assignments,
            unassignedClasses = unassignedClasses,
            evaluation = evaluation,
            isFeasible = unassignedClasses.isEmpty() && evaluation.maxWorkloadExceededCount == 0,
            errors = errors
        )
    }
    
    private fun evaluate(
        assignments: List<Assignment>,
        unassignedClasses: List<UnassignedClassInfo>,
        teachers: List<TeacherEntity>,
        teacherCapacities: Map<Long, Int>,
        teacherAssignedGrades: Map<Long, Set<Long>>
    ): DistributionEvaluation {
        val teacherLoads = mutableMapOf<Long, Int>()
        for (a in assignments) {
            teacherLoads[a.teacherId] = (teacherLoads[a.teacherId] ?: 0) + a.workload
        }
        
        var maxLoadExceeded = 0
        var maxGradesExceeded = 0
        val occupancies = mutableListOf<Double>()
        
        for (teacher in teachers) {
            val tId = teacher.teacherId
            val load = teacherLoads[tId] ?: 0
            val capacity = teacherCapacities[tId] ?: 0
            
            if (load > capacity) maxLoadExceeded++
            if ((teacherAssignedGrades[tId]?.size ?: 0) > teacher.maxDifferentGrades) maxGradesExceeded++
            
            if (capacity > 0) {
                occupancies.add(load.toDouble() / capacity)
            } else if (load > 0) {
                occupancies.add(1.0)
            } else {
                occupancies.add(0.0)
            }
        }
        
        val minOcc = occupancies.minOrNull() ?: 0.0
        val maxOcc = occupancies.maxOrNull() ?: 0.0
        val avgOcc = if (occupancies.isNotEmpty()) occupancies.average() else 0.0
        
        // Score out of 100
        var score = 100
        score -= unassignedClasses.size * 10
        score -= maxLoadExceeded * 20
        score -= maxGradesExceeded * 10
        
        // Fairness penalty (max difference in occupancy)
        val diff = maxOcc - minOcc
        score -= (diff * 20).toInt()
        
        return DistributionEvaluation(
            score = max(0, score),
            maxWorkloadExceededCount = maxLoadExceeded,
            maxGradesExceededCount = maxGradesExceeded,
            unassignedCount = unassignedClasses.size,
            averageOccupancy = avgOcc,
            maxOccupancy = maxOcc,
            minOccupancy = minOcc
        )
    }
}
