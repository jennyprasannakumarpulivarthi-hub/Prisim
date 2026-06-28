package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Post::class,
        Comment::class,
        Moment::class,
        Prisim::class,
        Message::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PrisimDatabase : RoomDatabase() {
    abstract fun prisimDao(): PrisimDao

    companion object {
        @Volatile
        private var INSTANCE: PrisimDatabase? = null

        fun getDatabase(context: Context): PrisimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrisimDatabase::class.java,
                    "prisim_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
