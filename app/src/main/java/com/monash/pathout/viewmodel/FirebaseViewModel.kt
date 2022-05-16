package com.monash.pathout.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.monash.pathout.model.User
import com.monash.pathout.ui.MainActivity

class FirebaseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val user: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    fun loadUserProfile(email: String) {
        db.collection("user").whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    user.value = result.documents[0].toObject()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private companion object {
        private val TAG = MainActivity::class.java.simpleName;
    }
}