package com.example

import android.app.Application
import com.example.data.db.PrisimDatabase
import com.example.data.repository.PrisimRepository

class PrisimApplication : Application() {
    val database by lazy { PrisimDatabase.getDatabase(this) }
    val repository by lazy { PrisimRepository(database.prisimDao(), this) }
}
