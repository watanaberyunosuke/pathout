package com.monash.pathout.model

import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

class Departure {
    @SerializedName("stop_id")
    var stopId = 0

    @SerializedName("route_id")
    var routeId = 0

    @SerializedName("run_id")
    var runId = 0

    @SerializedName("run_ref")
    var runRef: String? = null

    @SerializedName("direction_id")
    var directionId = 0

    @SerializedName("disruption_ids")
    var disruptionIds: ArrayList<Any>? = null

    @SerializedName("scheduled_departure_utc")
    var scheduledDepartureUtc: Date? = null

    @SerializedName("estimated_departure_utc")
    var estimatedDepartureUtc: Any? = null

    @SerializedName("at_platform")
    var isAtPlatform = false

    @SerializedName("platform_number")
    var platformNumber: Any? = null
    var flags: String? = null

    @SerializedName("departure_sequence")
    var departureSequence = 0

    fun hasAlreadyPassed(): Boolean {
        return Instant.now().isAfter(scheduledDepartureUtc!!.toInstant())
    }

    fun calculateTimeDifference(): String {
        val timeDiff = scheduledDepartureUtc!!.time - Date().time
        val secondsDiff = timeDiff / 1000 % 60
        val minutesDiff = timeDiff / (1000 * 60) % 60
        val hoursDiff = timeDiff / (1000 * 60 * 60) % 24

        // Convert to am/pm format
        val dateFormat: DateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val dateString =
            dateFormat.format(scheduledDepartureUtc!!)
                .toString()
        if (minutesDiff <= 0) {
            return "$secondsDiff second(s)\n$dateString"
        }
        return if (hoursDiff <= 0) {
            "$minutesDiff minute(s)\n$dateString"
        } else "$hoursDiff hour(s) $minutesDiff minute(s)\n$dateString"
    }
}