package com.monash.pathout.model

import java.util.*

data class Journey(
    var userId: String? = "",
    var sourceLat: Double? = 0.0,
    var sourceLng: Double? = 0.0,
    var destinationName: String? = "",
    var destinationLat: Double? = 0.0,
    var destinationLng: Double? = 0.0,
    var distance: Double? = 0.0,
    var duration: Double? = 0.0,
    var journeyDate: Date? = Date()
)