package com.codepocket.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codepocket.app.model.Snippet
import com.codepocket.app.model.TagConverter

@Database(entities = [Snippet::class], version = 1, exportSchema = false)
@TypeConverters(TagConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codepocket_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
