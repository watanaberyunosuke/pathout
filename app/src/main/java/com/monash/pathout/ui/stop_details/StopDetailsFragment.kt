package com.monash.pathout.ui.stop_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.monash.pathout.R
import com.monash.pathout.databinding.StopDetailsFragmentBinding
import com.monash.pathout.model.Departure
import com.monash.pathout.model.Route
import com.monash.pathout.model.Stop
import com.monash.pathout.ui.stop_details.StopDetailsFragment
import com.monash.pathout.viewmodel.StopViewModel
import java.util.stream.Collectors

class StopDetailsFragment : Fragment() {
    private var binding: StopDetailsFragmentBinding? = null
    private var viewModel: StopViewModel? = null
    private var initRV = false
    private val departuresArrayList: MutableList<Departure> = ArrayList()
    private var adapter: StopDetailsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(StopViewModel::class.java)
        initRV = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set for Material Motion
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)

        // Inflate the View for this fragment
        binding = StopDetailsFragmentBinding.inflate(inflater, container, false)

        // Listen for changes in selected stop live userInfoData
        viewModel!!.getStop().observe(requireActivity()) { stop: Stop? ->
            if (stop != null) {
                val stopDistance = stop.stopDistance.toInt().toString() + "m"
                val routeString = stop.routesString

                binding!!.stopName.text = stop.stopName
                binding!!.stopDistance.text = stopDistance

                // Change the resource image
                val TRAIN = 0
                val TRAM = 1
                val BUS = 2
                val VLINE = 3
                val NIGHT_BUS = 4

                when (stop.routeType) {
                    TRAIN -> binding!!.stopTypeImage.setImageResource(R.drawable.ic_baseline_train_24)
                    TRAM -> binding!!.stopTypeImage.setImageResource(R.drawable.ic_baseline_tram_24)
                    BUS, NIGHT_BUS -> binding!!.stopTypeImage.setImageResource(R.drawable.ic_baseline_directions_bus_24)
                    else -> binding!!.stopTypeImage.setImageResource(R.drawable.ic_baseline_navigation_24)
                }

                // Set up departures recycler view
                if (!initRV) {
                    setupRecyclerView(stop)
                }
            }
        }
        return binding!!.root
    }

    override fun onStart() {
        super.onStart()

        this.departuresArrayList.clear()
        adapter?.notifyDataSetChanged()

        // Show circular progress bar
        binding!!.deptProgressBar.isVisible = true
    }

    private fun setupRecyclerView(stop: Stop) {
        val stopRV = binding!!.idRVDepartures
        val linearLayoutManager = LinearLayoutManager(
            requireActivity().applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        val stopDetailsAdapter =
            viewModel?.let { StopDetailsAdapter(requireActivity(), this.departuresArrayList, it) }

        stopRV.layoutManager = linearLayoutManager
        stopRV.adapter = stopDetailsAdapter
        stopRV.itemAnimator = DefaultItemAnimator()

        this.adapter = stopRV.adapter as StopDetailsAdapter?

        val routeIds =
            stop.routes?.stream()?.map { obj: Route -> obj.routeId }?.collect(Collectors.toList())
        viewModel!!.getDepartures(stop.routeType, stop.stopId)
            .observe(requireActivity()) { departures: List<Departure> ->
                this.departuresArrayList.clear()
                for (departure in departures) {
                    val departureInRoute = routeIds?.contains(departure.routeId)

                    // Add only departures that are in the future
                    if (!departure.hasAlreadyPassed() && departureInRoute == true) {
                        this.departuresArrayList.add(departure)
                    }
                }

                if (departures.isNotEmpty()) {
                    binding!!.deptProgressBar.isVisible = false
                }
                stopDetailsAdapter?.notifyDataSetChanged()
            }

        initRV = true
    }

    companion object {
        @JvmField
        val TAG = StopDetailsFragment::class.java.simpleName
    }
}