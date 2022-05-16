package com.monash.pathout.repository

import android.app.Application
import com.monash.pathout.dao.JourneyDAO
import com.monash.pathout.database.JourneyDatabase
import com.monash.pathout.entity.Journey
import kotlinx.coroutines.flow.Flow

class JourneyRepository(application: Application) {

    private var journeyDao: JourneyDAO =
        JourneyDatabase.getInstance(application).journeyDao()

    val allJourneys: Flow<List<Journey>> = journeyDao.getAll()

    suspend fun findById(journeyId: Int): Journey? {
        return journeyDao.findByID(journeyId)
    }

    fun getNumberofJourneys(): Int {
        return journeyDao.getNumberofJourneys()
    }

    suspend fun insert(journey: Journey) {
        journeyDao.insert(journey)
    }

    suspend fun delete(journey: Journey) {
        journeyDao.delete(journey)
    }


    suspend fun update(journey: Journey) {
        journeyDao.update(journey)
    }

    suspend fun deleteAll() {
        journeyDao.deleteAll()
    }
}