package com.monash.pathout.ui.report

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.monash.pathout.R
import com.monash.pathout.databinding.ReportFragmentBinding
import com.monash.pathout.model.Journey
import com.monash.pathout.viewmodel.JourneyGraphViewModel

class ReportFragment : Fragment() {

    private var binding: ReportFragmentBinding? = null
    private var viewModel: JourneyGraphViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the View for this fragment
        binding = ReportFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[JourneyGraphViewModel::class.java]

        viewModel!!.loadAllJourneys()
        viewModel!!.allJourneys.observe(requireActivity()) {
            if (it.isNotEmpty()) {
                binding!!.reportTitle.text =
                    "Showing journey history for all journeys"
                binding!!.reportTitle.gravity = Gravity.CENTER_HORIZONTAL
                updateReport(it)
            }
        }

        viewModel!!.endDate.observe(requireActivity()) {
            val startDate = viewModel!!.startDate.value
            binding!!.reportTitle.text =
                "Showing journey history for dates\n${
                    startDate?.let { it1 ->
                        viewModel!!.convertToDateString(
                            it1
                        )
                    }
                } to ${
                    viewModel!!.convertToDateString(it)
                }"
            binding!!.reportTitle.gravity = Gravity.CENTER_HORIZONTAL

            val filteredJourneys = viewModel!!.allJourneys.value?.filter { journey ->
                val journeyTime = journey.journeyDate?.time!!
                viewModel!!.isT1AfterT2(journeyTime, startDate!!) && viewModel!!.isT1AfterT2(
                    it,
                    journeyTime
                )
            }

            if (filteredJourneys != null) {
                updateReport(filteredJourneys)
            } else {
                updateReport(ArrayList())
            }
        }

        viewModel!!.chartType.observe(requireActivity()) {
            val groupedJourneys = viewModel!!.groupedJourneys
            if (groupedJourneys != null) {
                if (it == "BAR") {
                    replaceFragment(BarGraphFragment())
                } else if (it == "LINE") {
                    replaceFragment(LineGraphFragment())
                }
            }
        }

        replaceFragment(BarGraphFragment())

        return binding!!.root
    }

    private fun updateReport(journeys: List<Journey>) {
        if (journeys.isEmpty()) {
            binding!!.numberOfJourneysVal.text = 0.toString()
            binding!!.totalDistanceCoveredVal.text = "0 metres"
            binding!!.totalTimeSpentVal.text = "0 seconds"

            return
        }


        val numberOfJourneys = journeys.size
        val totalDistance =
            journeys.map { journey -> journey.distance }.reduce { x, y -> x!! + y!! }
        val totalTime = journeys.map { journey -> journey.duration }.reduce { x, y -> x!! + y!! }

        binding!!.numberOfJourneysVal.text = numberOfJourneys.toString()
        binding!!.totalDistanceCoveredVal.text = "${totalDistance?.toInt()} metres"
        binding!!.totalTimeSpentVal.text = String.format("%.2f", totalTime) + " seconds"
    }

    private fun replaceFragment(nextFragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.graph_fragment_container_view, nextFragment)
            .commit()
    }
}