package com.monash.pathout.ui.transport_mode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialElevationScale
import com.monash.pathout.R
import com.monash.pathout.databinding.TransportModeFragmentBinding
import com.monash.pathout.ui.nearby_stops.NearbyStopsFragment
import com.monash.pathout.ui.transport_mode.TransportModeFragment
import com.monash.pathout.viewmodel.StopViewModel

class TransportModeFragment : Fragment() {
    private var binding: TransportModeFragmentBinding? = null
    private var viewModel: StopViewModel? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity()).get(StopViewModel::class.java)

        // Set for Material Motion
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)

        // Inflate the View for this fragment
        binding = TransportModeFragmentBinding.inflate(inflater, container, false)
        addTransportModeClickListeners()


        // Add on click listener for news to open a browser
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun addTransportModeClickListeners() {
        val TRAIN = 0
        val TRAM = 1
        val BUS = 2
        val VLINE = 3
        val NIGHT_BUS = 4
        binding!!.busBtn.setOnClickListener {
            viewModel!!.transportModeFilter = BUS
            replaceFragmentWithNearbyStopsFragment()
        }
        binding!!.tramBtn.setOnClickListener {
            viewModel!!.transportModeFilter = TRAM
            replaceFragmentWithNearbyStopsFragment()
        }
        binding!!.trainBtn.setOnClickListener {
            viewModel!!.transportModeFilter = TRAIN
            replaceFragmentWithNearbyStopsFragment()
        }

        // Open disruptions in browser
        binding!!.disruptionsBtn.setOnClickListener { v: View? ->
            val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.ptv.vic.gov.au/disruptions/disruptions-information/"))
            startActivity(browserIntent)
        }

        // Open news in browser
        binding!!.newsBtn.setOnClickListener { v: View? ->
            val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.ptv.vic.gov.au/news-and-events/news/"))
            startActivity(browserIntent)
        }
    }

    private fun replaceFragmentWithNearbyStopsFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
                .addSharedElement(requireView(), getString(R.string.transport_mode_nearby_stops))
                .replace((requireView().parent as ViewGroup).id, NearbyStopsFragment(), NearbyStopsFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    companion object {
        val TAG = TransportModeFragment::class.java.simpleName
    }
}