package com.humara.nagar.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.humara.nagar.ui.signup.ReferenceDataDao
import com.humara.nagar.ui.signup.model.CategoryDetails
import com.humara.nagar.ui.signup.model.LocalityDetails

@Database(entities = [LocalityDetails::class, CategoryDetails::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun referenceDataDao(): ReferenceDataDao

    companion object {
        private const val DB_NAME = "Humara_Nagar.db"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDataBase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME).build()
                INSTANCE = instance
                instance
            }
        }
    }
}