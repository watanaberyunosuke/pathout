package com.monash.pathout.model

import com.google.gson.annotations.SerializedName

class Stop {
    @SerializedName("disruptionIds")
    var disruptionIds: List<Any>? = null

    @SerializedName("stop_distance")
    var stopDistance = 0.0

    @SerializedName("stop_suburb")
    var stopSuburb: String? = null

    @SerializedName("stop_name")
    var stopName: String? = null

    @SerializedName("stop_id")
    var stopId = 0

    @SerializedName("route_type")
    var routeType = 0
    var routes: List<Route>? = null

    @SerializedName("stop_latitude")
    var stopLatitude = 0.0

    @SerializedName("stop_longitude")
    var stopLongitude = 0.0

    @SerializedName("stop_landmark")
    var stopLandmark: String? = null

    @SerializedName("stop_sequence")
    var stopSequence = 0
    val routesString: String?
        get() {
            val TRAIN = 0
            var routeString = ""
            if (routeType != TRAIN && !routes!!.isEmpty()) {
                for (route in routes!!) {
                    val routeNumber = route.routeNumber
                    val routeName = route.routeName
                    routeString += if (routeString.isEmpty()) {
                        if (routeNumber!!.isEmpty()) {
                            routeName
                        } else {
                            String.format("%s\n%s",
                                    if (route.routeNumber!!.isEmpty()) "" else "Route " + route.routeNumber, route.routeName)
                        }
                    } else {
                        String.format("\n\n%s\n%s",
                                if (route.routeNumber!!.isEmpty()) "" else "Route " + route.routeNumber, route.routeName)
                    }
                }
            }
            return routeString
        }

    override fun toString(): String {
        return "Stop{" +
                "disruption_ids=" + disruptionIds +
                ", stop_distance=" + stopDistance +
                ", stop_suburb='" + stopSuburb + '\'' +
                ", stop_name='" + stopName + '\'' +
                ", stop_id=" + stopId +
                ", route_type=" + routeType +
                ", routes=" + routes +
                ", stop_latitude=" + stopLatitude +
                ", stop_longitude=" + stopLongitude +
                ", stop_landmark='" + stopLandmark + '\'' +
                ", stop_sequence=" + stopSequence +
                '}'
    }
}