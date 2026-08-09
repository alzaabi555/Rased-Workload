package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.WorkloadRepository

class WorkloadApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { WorkloadRepository(database.workloadDao()) }
}
