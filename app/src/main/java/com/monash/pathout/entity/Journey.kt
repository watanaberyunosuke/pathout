package com.monash.pathout.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Journey(
    @ColumnInfo(name = "journey_name") var journeyName: String?,
    @ColumnInfo(name = "source_name") var sourceName: String?,
    @ColumnInfo(name = "start_address") var sourceAddress: String?,
    @ColumnInfo(name = "source_latitude") var sourceLat: Double?,
    @ColumnInfo(name = "source_longitude") var sourceLng: Double?,
    @ColumnInfo(name = "destination_name") var destinationName: String?,
    @ColumnInfo(name = "destination_address") var destinationAddress: String?,
    @ColumnInfo(name = "destination_latitude") var destinationLat: Double?,
    @ColumnInfo(name = "destination_longitude") var destinationLng: Double?,
    @PrimaryKey(autoGenerate = true) var uid: Int = 0
) : Parcelable


