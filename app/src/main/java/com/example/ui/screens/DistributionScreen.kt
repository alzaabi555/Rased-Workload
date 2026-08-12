package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DistributionViewModel
import com.example.ui.SubjectViewModel
import com.example.domain.DistributionResult
import com.example.domain.Assignment
import com.example.ui.SettingsViewModel
import com.example.ui.TeacherViewModel
import com.example.domain.UnassignedClassInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionScreen(viewModel: DistributionViewModel, subjectViewModel: SubjectViewModel, settingsViewModel: SettingsViewModel, teacherViewModel: TeacherViewModel) {
    val subjects by subjectViewModel.subjects.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val results by viewModel.distributionResults.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val teachers by teacherViewModel.teachers.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var selectedResultIndex by rememberSaveable { mutableStateOf(0) }
    var selectedSectionIndex by rememberSaveable { mutableStateOf(0) }
    var selectedSubjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var selectedAssignment by remember { mutableStateOf<Assignment?>(null) }
    
    LaunchedEffect(selectedSubjectId, subjects) {
        val id = selectedSubjectId
        if (id != null && subjects.isNotEmpty()) {
            val subject = subjects.find { it.subjectId == id }
            if (subject != null) {
                viewModel.loadOrDistribute(subject)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text("التوزيع الآلي", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (subjects.isEmpty()) {
                Text("لا توجد مواد لإجراء التوزيع.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        val selectedSubject = subjects.find { it.subjectId == selectedSubjectId }
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedSubject?.name ?: "اختر المادة للبدء",
                            onValueChange = { },
                            label = { Text("المادة الدراسية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        selectedSubjectId = subject.subjectId
                                        subjectDropdownExpanded = false
                                        selectedResultIndex = 0
                                        selectedSectionIndex = 0
                                        selectedAssignment = null
                                    }
                                )
                            }
                        }
                    }
                    
                    val selectedSubject = subjects.find { it.subjectId == selectedSubjectId }
                    if (selectedSubject != null && !isProcessing) {
                        Spacer(modifier = Modifier.width(16.dp))
                        FilledTonalButton(onClick = { viewModel.distribute(selectedSubject) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة التوزيع")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة التوزيع")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            results?.let { resList ->
                if (resList.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedResultIndex,
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                    ) {
                        resList.forEachIndexed { index, res ->
                            Tab(
                                selected = selectedResultIndex == index,
                                onClick = { 
                                    selectedResultIndex = index 
                                    selectedSectionIndex = 0
                                    selectedAssignment = null
                                },
                                text = { Text("خيار ${index + 1}", style = MaterialTheme.typography.titleSmall) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val res = resList[selectedResultIndex]
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.isFeasible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (res.isFeasible) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (res.isFeasible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (res.isFeasible) "توزيع قابل للتطبيق" else "توجد تعارضات", 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (res.isFeasible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (!res.isFeasible && res.errors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                res.errors.take(2).forEach { Text("• $it", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedSectionIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    ) {
                        Tab(
                            selected = selectedSectionIndex == 0,
                            onClick = { selectedSectionIndex = 0 },
                            text = { Text("جدول الأنصبة", style = MaterialTheme.typography.titleSmall) }
                        )
                        Tab(
                            selected = selectedSectionIndex == 1,
                            onClick = { selectedSectionIndex = 1 },
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("فصول غير مسندة", style = MaterialTheme.typography.titleSmall)
                                    if (res.unassignedClasses.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = MaterialTheme.colorScheme.error) { 
                                            Text(res.unassignedClasses.size.toString(), color = MaterialTheme.colorScheme.onError) 
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedSectionIndex == 0) {
                        if (res.assignments.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("جدول الأنصبة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                if (selectedAssignment != null) {
                                    Text("انقر على فصل آخر للتبديل، أو انقر (نقل هنا) لنقله.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Row {
                                IconButton(onClick = { com.example.ui.PrintHelper.printDistributionResult(context, res, settings) }) {
                                    Icon(Icons.Filled.Print, contentDescription = "طباعة PDF", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { 
                                    val csvContent = buildString {
                                        append("المعلم,الصف,الفصل,النصاب\n")
                                        res.assignments.forEach { a ->
                                            append("${a.teacherName},${a.gradeName},${a.className},${a.workload}\n")
                                        }
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "جدول التوزيع")
                                        putExtra(Intent.EXTRA_TEXT, csvContent)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "تصدير التوزيع كـ CSV"))
                                }) {
                                    Icon(Icons.Filled.Share, contentDescription = "مشاركة / تصدير", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val assignmentsByTeacher = res.assignments.groupBy { it.teacherId }
                        
                        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 80.dp)) {
                            items(assignmentsByTeacher.entries.toList()) { (teacherId, assignments) ->
                                val teacherName = assignments.first().teacherName
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(), 
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(teacherName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                            if (selectedAssignment != null && selectedAssignment!!.teacherId != teacherId) {
                                                FilledTonalButton(
                                                    onClick = {
                                                        viewModel.reassignClass(selectedResultIndex, selectedAssignment!!, teacherId, teacherName)
                                                        selectedAssignment = null
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("نقل هنا", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        val totalLoad = assignments.sumOf { it.workload }
                                        val gradesCount = assignments.map { it.gradeId }.toSet().size
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("$totalLoad حصة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("$gradesCount صفوف", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        assignments.forEach { a ->
                                            val isSelected = selectedAssignment == a
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.small)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                    .clickable {
                                                        if (selectedAssignment == null) {
                                                            selectedAssignment = a
                                                        } else if (selectedAssignment == a) {
                                                            selectedAssignment = null
                                                        } else {
                                                            viewModel.swapAssignments(selectedResultIndex, selectedAssignment!!, a)
                                                            selectedAssignment = null
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("${a.gradeName} - ${a.className}", style = MaterialTheme.typography.bodyLarge, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("${a.workload} حصص", style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        } else {
                            Text("لا توجد أنصبة موزعة.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                        }
                    } else {
                        if (res.unassignedClasses.isNotEmpty()) {
                            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 80.dp)) {
                                item {
                                    Text("يوجد ${res.unassignedClasses.size} فصول غير مسندة:", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                                }
                                items(res.unassignedClasses) { ucInfo ->
                                    val uc = ucInfo.classEntity
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("• ${uc.name} (${ucInfo.gradeName})", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                                            var assignDropdown by remember { mutableStateOf(false) }
                                            Box {
                                                FilledTonalButton(
                                                    onClick = { assignDropdown = true },
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.filledTonalButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.error,
                                                        contentColor = MaterialTheme.colorScheme.onError
                                                    )
                                                ) {
                                                    Text("إسناد يدوياً")
                                                }
                                                DropdownMenu(expanded = assignDropdown, onDismissRequest = { assignDropdown = false }) {
                                                    teachers.forEach { teacher ->
                                                        DropdownMenuItem(text = { Text(teacher.name) }, onClick = {
                                                            viewModel.assignUnassignedClass(selectedResultIndex, ucInfo, teacher.teacherId, teacher.name)
                                                            assignDropdown = false
                                                        })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("اكتمل نصاب التوزيع. لا توجد فصول غير مسندة.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
