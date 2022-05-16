package com.monash.pathout.api

import retrofit2.http.GET
import com.monash.pathout.model.StopRoot
import com.monash.pathout.model.DepartureRoot
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query

interface PTVEndpointInterface {
    @GET("stops/location/{coordinates}")
    fun getNearbyStops(@Path("coordinates") coordinates: String?,
                       @Query("max_results") MaxResults: Int): Call<StopRoot?>?

    @GET("departures/route_type/{route_type}/stop/{stop_id}")
    fun getStopDepartures(@Path("route_type") routeType: Int, @Path("stop_id") stopId: Int): Call<DepartureRoot?>?
}