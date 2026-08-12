package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AppViewModelFactory
import com.example.ui.DistributionViewModel
import com.example.ui.MainScreen
import com.example.ui.SettingsViewModel
import com.example.ui.SubjectViewModel
import com.example.ui.TeacherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as WorkloadApplication
    val factory = AppViewModelFactory(app.repository, app)
    
    val subjectViewModel = ViewModelProvider(this, factory)[SubjectViewModel::class.java]
    val teacherViewModel = ViewModelProvider(this, factory)[TeacherViewModel::class.java]
    val distributionViewModel = ViewModelProvider(this, factory)[DistributionViewModel::class.java]
    val settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
    
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          MainScreen(
              subjectViewModel = subjectViewModel,
              teacherViewModel = teacherViewModel,
              distributionViewModel = distributionViewModel,
              settingsViewModel = settingsViewModel
          )
        }
      }
    }
  }
}

