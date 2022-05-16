package com.monash.pathout.ui.nearby_stops

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.monash.pathout.MapAnnotationDelegate
import com.monash.pathout.databinding.NearbyStopsFragmentBinding
import com.monash.pathout.model.Stop
import com.monash.pathout.ui.nearby_stops.NearbyStopsFragment
import com.monash.pathout.viewmodel.StopViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class NearbyStopsFragment : Fragment(), CoroutineScope {
    private var job: Job = Job()
    private var binding: NearbyStopsFragmentBinding? = null
    private var viewModel: StopViewModel? = null
    private lateinit var onNearbyStopsChangeListener: MapAnnotationDelegate

    private val serviceProvider = MapboxSearchSdk.serviceProvider
    private val stopsArraylist: MutableList<Stop> = ArrayList()
    private var nearbyStopsAdapter: NearbyStopsAdapter? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[StopViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set up for Material Motion
        sharedElementEnterTransition = MaterialFadeThrough()

        // Inflate the View for this fragment
        binding = NearbyStopsFragmentBinding.inflate(inflater, container, false)

        // Configure nearby stops
        setupRecyclerView()

        return binding!!.root
    }

    @SuppressLint("MissingPermission")
    private fun setupRecyclerView() {
        val stopRV = binding!!.idRVStops
        val linearLayoutManager = LinearLayoutManager(
            requireActivity().applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        nearbyStopsAdapter =
            viewModel?.let { NearbyStopsAdapter(requireActivity(), stopsArraylist, it) }
        stopRV.layoutManager = linearLayoutManager
        stopRV.adapter = nearbyStopsAdapter
        stopRV.itemAnimator = DefaultItemAnimator()

        serviceProvider.locationEngine()
            .getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    val location =
                        (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                            Point.fromLngLat(location.longitude, location.latitude)
                        }

                    val defaultLat = "-37.818373029081755"
                    val defaultLng = "144.9525536426746"
                    val locationStr =
                        (location?.latitude()
                            ?: defaultLat).toString() + "," + (location?.longitude()
                            ?: defaultLng).toString()
                    viewModel!!.getNearbyStops(locationStr, 30) {
                        if (it > 0) {
                            binding!!.emptyNearbyStops.visibility = View.GONE
                            binding!!.nearbyStopsProgressBar.isVisible = false
                        } else {
                            binding!!.emptyNearbyStops.visibility = View.VISIBLE
                            binding!!.nearbyStopsProgressBar.isVisible = false
                        }
                    }

                    viewModel!!.nearbyStops.observe(requireActivity()) { stops: List<Stop>? ->
                        stopsArraylist.clear()
                        stopsArraylist.addAll(stops!!)

                        nearbyStopsAdapter?.notifyDataSetChanged()
                        launch {
                            onNearbyStopsChangeListener.updateMapAnnotations(stops)
                        }
                    }
                }

                override fun onFailure(p0: Exception) {}
            })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onNearbyStopsChangeListener = context as MapAnnotationDelegate
        } catch (castException: ClassCastException) {
            // The activity does not implement the delegate fun
        }
    }

    override fun onStart() {
        super.onStart()
        this.stopsArraylist.clear()
        nearbyStopsAdapter?.notifyDataSetChanged()
    }

    companion object {
        val TAG = NearbyStopsFragment::class.java.simpleName
    }
}