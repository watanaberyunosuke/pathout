package com.monash.pathout.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.monash.pathout.databinding.GraphFragmentBinding
import com.monash.pathout.viewmodel.JourneyGraphViewModel


class BarGraphFragment : Fragment() {
    private lateinit var viewModel: JourneyGraphViewModel
    private lateinit var binding: GraphFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[JourneyGraphViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the View for this fragment
        binding = GraphFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[JourneyGraphViewModel::class.java]

        viewModel.endDate.observe(requireActivity()) {
            val startDate = viewModel.startDate.value

            val filteredJourneys = viewModel.allJourneys.value?.filter { journey ->
                val journeyTime = journey.journeyDate?.time!!
                viewModel.isT1AfterT2(journeyTime, startDate!!) && viewModel.isT1AfterT2(
                    it,
                    journeyTime
                )
            }

            if (filteredJourneys != null) {
                val groupedJourneys = filteredJourneys.groupBy {
                    viewModel.convertToDateString(it.journeyDate?.time!!)
                }
                    .mapValues { (date, dist) -> dist.sumOf { it.distance!! } }

                viewModel.groupedJourneys = groupedJourneys
                drawBarChart(groupedJourneys)
            }
        }

        return binding.root
    }

    private fun drawBarChart(data: Map<String, Double>) {
        val anyChartView = binding.anyChartView
        anyChartView.setProgressBar(binding.progressBar)

        val cartesian: Cartesian = AnyChart.column()

        val seriesData: MutableList<DataEntry> = ArrayList()

        data.forEach { (date, distance) ->
            seriesData.add(CustomDataEntry(date, distance))
        }

        val column: Column = cartesian.column(seriesData)

        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(5.0)
            .format("{%Value}{groupsSeparator: }")

        cartesian.animation(true)
        cartesian.title("Total distance travelled each day")

        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Date")
        cartesian.yAxis(0).title("Distance")

        anyChartView.setChart(cartesian)
    }

    private inner class CustomDataEntry internal constructor(
        x: String?,
        value: Number?,

        ) : ValueDataEntry(x, value) {
    }

}