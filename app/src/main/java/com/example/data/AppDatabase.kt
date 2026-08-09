package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.WorkloadDao
import com.example.data.entities.*

@Database(
    entities = [
        SubjectEntity::class,
        GradeEntity::class,
        ClassEntity::class,
        TeacherEntity::class,
        TeacherSubjectCrossRef::class,
        TeacherAllowedGrade::class,
        DistributionSettings::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workloadDao(): WorkloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workload_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
