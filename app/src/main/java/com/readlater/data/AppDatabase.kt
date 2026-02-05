package com.readlater.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SavedEvent::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedEventDao(): SavedEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readlater_database"
                )
                    .fallbackToDestructiveMigration() // Wipe DB on version change/error
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
