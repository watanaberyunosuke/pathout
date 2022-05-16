package com.monash.pathout.repository

import androidx.lifecycle.MutableLiveData
import java.util.ArrayList
import com.monash.pathout.model.Departure
import com.monash.pathout.model.StopRoot
import com.monash.pathout.api.PTVClient
import com.monash.pathout.model.DepartureRoot
import com.monash.pathout.model.Stop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StopRepository {
    private val stopsArraylist = MutableLiveData<List<Stop>>(ArrayList())
    private val departuresArrayList = MutableLiveData<List<Departure>>(ArrayList())

    fun getNearbyStops(
        coordinates: String?,
        maxResults: Int,
        transportModeFilter: Int,
        callback: (Int) -> Unit
    ): MutableLiveData<List<Stop>> {
        PTVClient.instance
            ?.apiService
            ?.getNearbyStops(coordinates, maxResults)?.enqueue(object : Callback<StopRoot?> {
                override fun onResponse(call: Call<StopRoot?>, response: Response<StopRoot?>) {
                    val stopRoot = response.body()
                    if (stopRoot != null) {
                        if (transportModeFilter == -1) {
                            stopsArraylist.setValue(stopRoot.stops)
                        } else {
                            val stops: MutableList<Stop> = ArrayList()
                            for (stop in stopRoot.stops!!) {
                                if (stop.routeType == transportModeFilter) {
                                    stops.add(stop)
                                }
                            }
                            stopsArraylist.setValue(stops)
                        }
                    }

                    stopsArraylist.value?.let { callback(it.size) }
                }

                override fun onFailure(call: Call<StopRoot?>, t: Throwable) {
                    stopsArraylist.postValue(ArrayList())
                }
            })

        return stopsArraylist
    }

    fun getStopDepartures(routeType: Int, stopId: Int): MutableLiveData<List<Departure>> {
        PTVClient.instance
            ?.apiService
            ?.getStopDepartures(routeType, stopId)?.enqueue(object : Callback<DepartureRoot?> {
                override fun onResponse(
                    call: Call<DepartureRoot?>,
                    response: Response<DepartureRoot?>
                ) {
                    val departureRoot = response.body()
                    if (departureRoot != null) {
                        val departures: MutableList<Departure> = ArrayList()
                        for (departure in departureRoot.departures!!) {
                            departures.add(departure)
                        }
                        departuresArrayList.value = departures
                    }
                }

                override fun onFailure(call: Call<DepartureRoot?>, t: Throwable) {
                    departuresArrayList.postValue(ArrayList())
                }
            })

        return departuresArrayList
    }

    companion object {
        private var stopRepository: StopRepository? = null

        @JvmStatic
        val instance: StopRepository?
            get() {
                if (stopRepository == null) {
                    stopRepository = StopRepository()
                }

                return stopRepository
            }
    }
}