package com.monash.pathout.model

import com.google.gson.annotations.SerializedName

class Route {
    @SerializedName("route_type")
    var routeType = 0

    @SerializedName("route_id")
    var routeId = 0

    @SerializedName("route_name")
    var routeName: String? = null

    @SerializedName("route_number")
    var routeNumber: String? = null

    @SerializedName("route_gtfs_id")
    var routeGtfsId: String? = null
    var geopath: List<Any>? = null
}