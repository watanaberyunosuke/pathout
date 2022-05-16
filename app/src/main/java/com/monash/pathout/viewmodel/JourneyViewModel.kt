package com.monash.pathout.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.monash.pathout.entity.Journey
import com.monash.pathout.repository.JourneyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val jRepository: JourneyRepository = JourneyRepository(application)

    var allJourneys: LiveData<List<Journey>> = jRepository.allJourneys.asLiveData()

    fun getNumberOfJourneys(): Int {
        return jRepository.getNumberofJourneys()
    }

    fun findJourneybyId(id: Int): LiveData<Journey?> = liveData(Dispatchers.IO) {
        emit(jRepository.findById(id))
    }

    fun insert(journey: Journey) =
        viewModelScope.launch(Dispatchers.IO) { jRepository.insert(journey) }

    fun delete(journey: Journey) =
        viewModelScope.launch(Dispatchers.IO) { jRepository.delete(journey) }

    fun update(journey: Journey) =
        viewModelScope.launch(Dispatchers.IO) { jRepository.update(journey) }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) { jRepository.deleteAll() }

}
