package com.monash.pathout.viewmodel

import com.monash.pathout.repository.StopRepository.Companion.instance
import android.app.Application
import androidx.lifecycle.*
import com.monash.pathout.repository.StopRepository
import com.monash.pathout.model.Departure
import com.monash.pathout.model.Stop
import java.util.ArrayList

class StopViewModel(application: Application?) : AndroidViewModel(application!!) {
    private val stopRepository: StopRepository?

    var nearbyStops: LiveData<List<Stop>>
    private val stop: MutableLiveData<Stop>

    private var departures: MutableLiveData<List<Departure>>
    var transportModeFilter: Int

    fun getNearbyStops(
        coordinates: String?,
        maxResults: Int,
        callback: (Int) -> Unit
    ) {
        nearbyStops =
            stopRepository!!.getNearbyStops(
                coordinates,
                maxResults,
                transportModeFilter,
                callback
            )
    }

    fun getDepartures(routeType: Int, stopId: Int): LiveData<List<Departure>> {
        departures = stopRepository!!.getStopDepartures(routeType, stopId)
        return departures
    }

    fun setDepartures(departures: List<Departure>) {
        this.departures.value = departures
    }

    fun getStop(): LiveData<Stop> {
        return stop
    }

    fun setStop(stop: Stop) {
        this.stop.value = stop
    }

    init {
        stopRepository = instance
        nearbyStops = MutableLiveData(ArrayList())
        departures = MutableLiveData(ArrayList())
        stop = MutableLiveData()
        transportModeFilter = -1
    }
}