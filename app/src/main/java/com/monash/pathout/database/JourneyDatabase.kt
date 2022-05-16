package com.monash.pathout.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.monash.pathout.dao.JourneyDAO
import com.monash.pathout.entity.Journey


@Database(entities = [Journey::class], version = 4, exportSchema = false)
abstract class JourneyDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDAO

    companion object {
        @Volatile
        private var INSTANCE: JourneyDatabase? = null
        fun getInstance(context: Context): JourneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JourneyDatabase::class.java,
                    "JourneyDatabase"
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance
                instance
            }
        }
    }
}
