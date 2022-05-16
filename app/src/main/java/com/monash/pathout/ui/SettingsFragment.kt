package com.monash.pathout.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.monash.pathout.R
import com.monash.pathout.viewmodel.JourneyViewModel

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var viewModel: JourneyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[JourneyViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "nav_method_key") {
            // Load navigation method from shared preferences
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val navigationMethod = pref.getString("nav_method_key", "").toString()
            val travelMethodTextView =
                requireActivity().findViewById<TextView>(R.id.nav_header_travel_method)
            travelMethodTextView.text = "Preferred Travel Method: $navigationMethod"
        }
    }

}