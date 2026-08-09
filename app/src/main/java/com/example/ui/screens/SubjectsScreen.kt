package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.SubjectEntity
import com.example.data.entities.GradeEntity
import com.example.ui.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(viewModel: SubjectViewModel) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var subjectName by remember { mutableStateOf("") }
    
    var selectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    
    if (selectedSubject != null) {
        SubjectDetailScreen(viewModel, selectedSubject!!) { selectedSubject = null }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المواد", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة مادة")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (subjects.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد مواد مضافة حالياً. اضغط على الزر (+) لإضافة مادة.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(MaterialTheme.shapes.large)
                            .clickable { selectedSubject = subject },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(subject.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                                Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("إضافة مادة جديدة", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("اسم المادة") },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.addSubject(subjectName)
                            subjectName = ""
                            showDialog = false
                        }
                    }) {
                        Text("إضافة")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(viewModel: SubjectViewModel, subject: SubjectEntity, onBack: () -> Unit) {
    val grades by viewModel.getGradesForSubject(subject.subjectId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddGradeDialog by remember { mutableStateOf(false) }
    var gradeName by remember { mutableStateOf("") }
    var gradeWorkload by remember { mutableStateOf("") }
    var gradeClassCount by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGradeDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة صف")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (grades.isEmpty()) {
                item {
                    Text("لا توجد صفوف مضافة. أضف صفوفاً ليتم إسناد الفصول لها.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(grades) { grade ->
                    GradeItem(viewModel, grade)
                }
            }
        }
        
        if (showAddGradeDialog) {
            AlertDialog(
                onDismissRequest = { showAddGradeDialog = false },
                title = { Text("إضافة صف جديد", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = gradeName,
                            onValueChange = { gradeName = it },
                            label = { Text("اسم الصف (مثل: الصف الأول)") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = gradeWorkload,
                            onValueChange = { gradeWorkload = it },
                            label = { Text("نصاب حصص الفصل الواحد") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = gradeClassCount,
                            onValueChange = { gradeClassCount = it },
                            label = { Text("عدد الفصول") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val workload = gradeWorkload.toIntOrNull()
                        val count = gradeClassCount.toIntOrNull()
                        if (gradeName.isNotBlank() && workload != null && count != null) {
                            viewModel.addGradeWithClasses(subject.subjectId, gradeName, workload, count)
                            gradeName = ""
                            gradeWorkload = ""
                            gradeClassCount = ""
                            showAddGradeDialog = false
                        }
                    }) {
                        Text("إضافة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGradeDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GradeItem(viewModel: SubjectViewModel, grade: GradeEntity) {
    val classes by viewModel.getClassesForGrade(grade.gradeId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddClassDialog by remember { mutableStateOf(false) }
    var className by remember { mutableStateOf("") }
    
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
                Column {
                    Text(grade.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("النصاب: ${grade.workloadPerClass} حصص لكل فصل", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { viewModel.deleteGrade(grade) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف الصف", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("الفصول (${classes.size}):", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classes.forEach { schoolClass ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(schoolClass.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.deleteClass(schoolClass) },
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                InputChip(
                    selected = false,
                    onClick = { showAddClassDialog = true },
                    label = { Text("إضافة فصل") },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
    
    if (showAddClassDialog) {
        AlertDialog(
            onDismissRequest = { showAddClassDialog = false },
            title = { Text("إضافة فصل لـ ${grade.name}") },
            text = {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("اسم الفصل (مثال: أ)") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (className.isNotBlank()) {
                        viewModel.addClass(grade.gradeId, className)
                        className = ""
                        showAddClassDialog = false
                    }
                }) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddClassDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
