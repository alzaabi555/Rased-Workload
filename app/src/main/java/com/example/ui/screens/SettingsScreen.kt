package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SettingsViewModel
import com.example.data.entities.DistributionSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var schoolName by remember { mutableStateOf(settings.schoolName) }
    var preparerName by remember { mutableStateOf(settings.preparerName) }
    var defaultMaxWorkload by remember { mutableStateOf(settings.defaultMaxWorkload.toString()) }
    var defaultMaxGrades by remember { mutableStateOf(settings.defaultMaxGrades.toString()) }

    LaunchedEffect(settings) {
        schoolName = settings.schoolName
        preparerName = settings.preparerName
        defaultMaxWorkload = settings.defaultMaxWorkload.toString()
        defaultMaxGrades = settings.defaultMaxGrades.toString()
    }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val dbFile = context.getDatabasePath("school_database")
                    if (dbFile.exists()) {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            dbFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        Toast.makeText(context, "تم النسخ الاحتياطي بنجاح", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "فشل النسخ الاحتياطي", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val dbFile = context.getDatabasePath("school_database")
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        dbFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Toast.makeText(context, "تمت استعادة البيانات، يرجى إعادة تشغيل التطبيق", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "فشل الاستعادة", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val newSettings = DistributionSettings(
                        id = settings.id,
                        schoolName = schoolName,
                        preparerName = preparerName,
                        defaultMaxWorkload = defaultMaxWorkload.toIntOrNull() ?: 24,
                        defaultMinWorkload = settings.defaultMinWorkload,
                        defaultMaxGrades = defaultMaxGrades.toIntOrNull() ?: 3,
                        coordinatorReduction = settings.coordinatorReduction,
                        seniorTeacherReduction = settings.seniorTeacherReduction,
                        allowEmptyAssignments = settings.allowEmptyAssignments,
                        prioritizeSeniorTeachers = settings.prioritizeSeniorTeachers
                    )
                    viewModel.updateSettings(newSettings)
                    Toast.makeText(context, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
                },
                icon = { Icon(Icons.Filled.Save, contentDescription = null) },
                text = { Text("حفظ التغييرات") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // General Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("بيانات المدرسة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("اسم المدرسة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = preparerName,
                        onValueChange = { preparerName = it },
                        label = { Text("اسم معد الجدول") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // Distribution Limits
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("محددات التوزيع", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = defaultMaxWorkload,
                            onValueChange = { defaultMaxWorkload = it },
                            label = { Text("أقصى نصاب (حصة)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        )
                        OutlinedTextField(
                            value = defaultMaxGrades,
                            onValueChange = { defaultMaxGrades = it },
                            label = { Text("أقصى عدد صفوف") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }

            // Backup & Restore
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("النسخ الاحتياطي والاستعادة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilledTonalButton(
                            onClick = { backupLauncher.launch("rased_backup.db") },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("نسخ احتياطي") }
                        FilledTonalButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("استعادة") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
