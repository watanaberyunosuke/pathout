package com.monash.pathout.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.monash.pathout.databinding.GraphSelectionBinding
import com.monash.pathout.viewmodel.JourneyGraphViewModel
import java.text.SimpleDateFormat
import java.util.*

class SelectGraphFragment : Fragment() {

    private var binding: GraphSelectionBinding? = null
    private lateinit var viewModel: JourneyGraphViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GraphSelectionBinding.inflate(inflater, container, false)

        binding!!.dateSelection.setOnClickListener {
            showDataRangePicker()
        }

        binding!!.barchart.setOnClickListener {
            viewModel.chartType.value = "BAR"
        }
        binding!!.linechart.setOnClickListener {
            viewModel.chartType.value = "LINE"
        }

        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val view = binding!!.root

        viewModel = ViewModelProvider(requireActivity())[JourneyGraphViewModel::class.java]

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun showDataRangePicker() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()
        dateRangePicker.show(parentFragmentManager, "tag")

        dateRangePicker.addOnPositiveButtonClickListener { dateSelected ->
            val startDate = dateSelected.first
            val endDate = dateSelected.second

            viewModel.startDate.value = startDate
            viewModel.endDate.value = endDate

            binding?.selectedDate?.text =
                "StartDate: " + convertDate(startDate) + "\nEndDate: " + convertDate(endDate)
        }
    }

    private fun convertDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        )
        return format.format(date)
    }


}
