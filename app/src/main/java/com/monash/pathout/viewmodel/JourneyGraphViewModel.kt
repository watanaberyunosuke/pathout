package com.monash.pathout.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.monash.pathout.model.Journey
import com.monash.pathout.ui.MainActivity
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class JourneyGraphViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    var groupedJourneys: Map<String, Double>? = null
    val allJourneys: MutableLiveData<List<Journey>> by lazy {
        MutableLiveData<List<Journey>>()
    }

    val chartType: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val startDate: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }

    val endDate: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }

    fun loadAllJourneys() {
        val allJourneys = ArrayList<Journey>()

        db.collection("journey_history").whereEqualTo("userId", Firebase.auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    allJourneys.add(document.toObject())
                    Log.d(TAG, "${document.id} => ${document.data}")
                }

                this.allJourneys.value = allJourneys
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun convertToDateString(epochMillis: Long): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(Date(epochMillis))
    }

    private companion object {
        private val TAG = MainActivity::class.java.simpleName;
    }

    fun isT1AfterT2(t1: Long, t2: Long): Boolean {
        val t1Date = Instant.ofEpochMilli(t1).atZone(ZoneId.of("Australia/Melbourne")).toLocalDate()
        val t2Date = Instant.ofEpochMilli(t2).atZone(ZoneId.of("Australia/Melbourne")).toLocalDate()

        return t1Date >= t2Date
    }
}