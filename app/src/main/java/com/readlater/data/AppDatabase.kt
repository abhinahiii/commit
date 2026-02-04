package com.readlater.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SavedEvent::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedEventDao(): SavedEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2: Add completedAt and archivedAt columns, update status enum
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns
                database.execSQL("ALTER TABLE saved_events ADD COLUMN completedAt INTEGER")
                database.execSQL("ALTER TABLE saved_events ADD COLUMN archivedAt INTEGER")

                // Update status values: UPCOMING -> SCHEDULED, PAST -> SCHEDULED, CANCELLED -> ARCHIVED
                database.execSQL("UPDATE saved_events SET status = 'SCHEDULED' WHERE status = 'UPCOMING'")
                database.execSQL("UPDATE saved_events SET status = 'SCHEDULED' WHERE status = 'PAST'")
                database.execSQL("UPDATE saved_events SET status = 'ARCHIVED', archivedAt = ${System.currentTimeMillis()} WHERE status = 'CANCELLED'")
            }
        }

        // Migration from version 2 to 3: Add imageUrl column
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE saved_events ADD COLUMN imageUrl TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readlater_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
