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
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.monash.pathout.databinding.GraphFragmentBinding
import com.monash.pathout.viewmodel.JourneyGraphViewModel


class LineGraphFragment : Fragment() {
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
                drawLineChart(groupedJourneys)
            }
        }

        return binding.root
    }

    private fun drawLineChart(data: Map<String, Double>) {
        val anyChartView1 = binding.anyChartView
        anyChartView1.setProgressBar(binding.progressBar)

        val cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title("Trend of Distance Travelled each day")
        cartesian.yAxis(0).title("Number of distance travelled (m)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

        val seriesData: MutableList<DataEntry> = ArrayList()

        data.forEach { (date, distance) ->
            seriesData.add(CustomDataEntry(date, distance))
        }

        val set = Set.instantiate()

        set.data(seriesData)
        val seriesMapping = set.mapAs("{ x: 'x', value: 'value' }")


        val series = cartesian.line(seriesMapping)
        series.hovered().markers().enabled(true)
        series.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        anyChartView1.setChart(cartesian)
    }

    private inner class CustomDataEntry(
        x: String?,
        value: Number?,

        ) : ValueDataEntry(x, value) {
    }

}