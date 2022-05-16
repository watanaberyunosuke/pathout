package com.monash.pathout

import com.monash.pathout.model.Stop

interface MapAnnotationDelegate {
    suspend fun updateMapAnnotations(stops: List<Stop>)
}