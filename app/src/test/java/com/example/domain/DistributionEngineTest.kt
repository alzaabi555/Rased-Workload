package com.example.domain

import com.example.data.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistributionEngineTest {

    @Test
    fun testBasicExample() = runBlocking {
        val engine = DistributionEngine()
        
        val subject = SubjectEntity(1, "Social Studies")
        val grades = listOf(
            GradeEntity(1, 1, "5th", 4),
            GradeEntity(2, 1, "6th", 4),
            GradeEntity(3, 1, "7th", 4),
            GradeEntity(4, 1, "8th", 4)
        )
        
        val classes = mutableListOf<ClassEntity>()
        var classId = 1L
        repeat(9) { classes.add(ClassEntity(classId++, 1, "5th-${it+1}")) }
        repeat(8) { classes.add(ClassEntity(classId++, 2, "6th-${it+1}")) }
        repeat(7) { classes.add(ClassEntity(classId++, 3, "7th-${it+1}")) }
        repeat(6) { classes.add(ClassEntity(classId++, 4, "8th-${it+1}")) }
        
        assertEquals(30, classes.size)
        
        val teachers = mutableListOf<TeacherEntity>()
        val teacherNames = listOf("Ahmed", "Khalid", "Salem", "Nasser", "Majed", "Yousef")
        teacherNames.forEachIndexed { index, name ->
            teachers.add(
                TeacherEntity(
                    teacherId = index + 1L,
                    name = name,
                    maxWorkload = 20,
                    reductionAmount = 0,
                    maxDifferentGrades = 2
                )
            )
        }
        
        val settings = DistributionSettings()
        
        val result = engine.distribute(subject, grades, classes, teachers, emptyMap(), settings)
        
        assertTrue("Distribution should be feasible", result.isFeasible)
        assertEquals(0, result.unassignedClasses.size)
        
        // Check constraints
        val teacherLoads = result.assignments.groupBy { it.teacherId }.mapValues { entry ->
            entry.value.sumOf { it.workload }
        }
        
        teacherLoads.forEach { (tId, load) ->
            assertEquals("Teacher $tId should have 20 workload", 20, load)
        }
        
        val teacherGrades = result.assignments.groupBy { it.teacherId }.mapValues { entry ->
            entry.value.map { it.gradeId }.toSet().size
        }
        
        teacherGrades.forEach { (tId, gradeCount) ->
            assertTrue("Teacher $tId should have max 2 grades, got $gradeCount", gradeCount <= 2)
        }
    }
}
