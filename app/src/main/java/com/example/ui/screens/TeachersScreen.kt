package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TeacherViewModel
import com.example.ui.SubjectViewModel
import com.example.data.entities.TeacherEntity
import com.example.data.entities.TeacherRole
import com.example.data.entities.GradeEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersScreen(viewModel: TeacherViewModel, subjectViewModel: SubjectViewModel) {
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val subjects by subjectViewModel.subjects.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    var showDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    
    var teacherName by remember { mutableStateOf("") }
    var maxWorkload by remember { mutableStateOf("20") }
    var reduction by remember { mutableStateOf("0") }
    val selectedSubjects = remember { mutableStateListOf<Long>() }
    val selectedGrades = remember { mutableStateListOf<Long>() }
    var selectedRole by remember { mutableStateOf(TeacherRole.TEACHER) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var allGrades by remember { mutableStateOf<List<GradeEntity>>(emptyList()) }

    LaunchedEffect(selectedSubjects.toList()) {
        val grades = mutableListOf<GradeEntity>()
        selectedSubjects.forEach { subjId ->
            val g = subjectViewModel.getGradesForSubject(subjId).firstOrNull() ?: emptyList()
            grades.addAll(g)
        }
        allGrades = grades
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المعلمين", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTeacher = null
                    teacherName = ""
                    maxWorkload = "20"
                    reduction = "0"
                    selectedRole = TeacherRole.TEACHER
                    selectedSubjects.clear()
                    selectedGrades.clear()
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة معلم")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (teachers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد معلمين. أضف معلم للبدء.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(teacher.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                        val roleName = when(teacher.role) {
                                            TeacherRole.TEACHER -> "معلم"
                                            TeacherRole.SENIOR_TEACHER -> "معلم أول"
                                            TeacherRole.COORDINATOR -> "منسق مادة"
                                            else -> "أخرى"
                                        }
                                        Text(roleName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            editingTeacher = teacher
                                            teacherName = teacher.name
                                            maxWorkload = teacher.maxWorkload.toString()
                                            reduction = teacher.reductionAmount.toString()
                                            selectedRole = teacher.role
                                            
                                            val tSubjects = viewModel.getTeacherSubjectsSync(teacher.teacherId)
                                            selectedSubjects.clear()
                                            selectedSubjects.addAll(tSubjects.map { it.subjectId })
                                            
                                            val tGrades = viewModel.getTeacherAllowedGradesSync(teacher.teacherId)
                                            selectedGrades.clear()
                                            selectedGrades.addAll(tGrades.map { it.gradeId })
                                            
                                            showDialog = true
                                        }
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteTeacher(teacher) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val actualLoad = maxOf(0, teacher.maxWorkload - teacher.reductionAmount)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("الحد الأعلى", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${teacher.maxWorkload}", style = MaterialTheme.typography.titleMedium)
                                }
                                Column {
                                    Text("التخفيض", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${teacher.reductionAmount}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text("النصاب الفعلي", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$actualLoad", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingTeacher == null) "إضافة معلم جديد" else "تعديل بيانات المعلم", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("اسم المعلم") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = maxWorkload,
                                onValueChange = { maxWorkload = it },
                                label = { Text("الحد الأعلى") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reduction,
                                onValueChange = { reduction = it },
                                label = { Text("التخفيض") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        ExposedDropdownMenuBox(
                            expanded = roleDropdownExpanded,
                            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = when(selectedRole) {
                                    TeacherRole.TEACHER -> "معلم"
                                    TeacherRole.SENIOR_TEACHER -> "معلم أول"
                                    TeacherRole.COORDINATOR -> "منسق مادة"
                                    else -> "أخرى"
                                },
                                onValueChange = { },
                                label = { Text("الصفة الوظيفية") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = roleDropdownExpanded,
                                onDismissRequest = { roleDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(text = { Text("معلم") }, onClick = { selectedRole = TeacherRole.TEACHER; roleDropdownExpanded = false })
                                DropdownMenuItem(text = { Text("معلم أول") }, onClick = { selectedRole = TeacherRole.SENIOR_TEACHER; roleDropdownExpanded = false })
                                DropdownMenuItem(text = { Text("منسق مادة") }, onClick = { selectedRole = TeacherRole.COORDINATOR; roleDropdownExpanded = false })
                            }
                        }
                        
                        if (subjects.isNotEmpty()) {
                            Text("المواد الدراسية:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                subjects.forEach { subject ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            if (selectedSubjects.contains(subject.subjectId)) selectedSubjects.remove(subject.subjectId)
                                            else selectedSubjects.add(subject.subjectId)
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = selectedSubjects.contains(subject.subjectId), onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(subject.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                        
                        if (allGrades.isNotEmpty()) {
                            Text("الصفوف المفضلة (اختياري):", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                allGrades.forEach { grade ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            if (selectedGrades.contains(grade.gradeId)) selectedGrades.remove(grade.gradeId)
                                            else selectedGrades.add(grade.gradeId)
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = selectedGrades.contains(grade.gradeId), onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(grade.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (teacherName.isNotBlank()) {
                            if (editingTeacher == null) {
                                viewModel.addTeacher(
                                    name = teacherName,
                                    role = selectedRole,
                                    maxWorkload = maxWorkload.toIntOrNull() ?: 20,
                                    reduction = reduction.toIntOrNull() ?: 0,
                                    subjects = selectedSubjects.toList(),
                                    allowedGrades = selectedGrades.toList()
                                )
                            } else {
                                val updatedTeacher = editingTeacher!!.copy(
                                    name = teacherName,
                                    role = selectedRole,
                                    maxWorkload = maxWorkload.toIntOrNull() ?: 20,
                                    reductionAmount = reduction.toIntOrNull() ?: 0
                                )
                                viewModel.updateTeacher(
                                    teacher = updatedTeacher,
                                    subjects = selectedSubjects.toList(),
                                    allowedGrades = selectedGrades.toList()
                                )
                            }
                            showDialog = false
                        }
                    }) {
                        Text(if (editingTeacher == null) "إضافة" else "تحديث")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}
