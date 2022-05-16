package com.monash.pathout.ui.preset_journey

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monash.pathout.R
import com.monash.pathout.entity.Journey
import com.monash.pathout.viewmodel.JourneyViewModel

class PresetJourneyAdapter(
    private val context: Context,
    private val presetJourneysArrayList: MutableList<Journey>,
    private val journeyViewModel: JourneyViewModel,
    private val onClickListener: (Journey) -> View.OnClickListener,
    private val onPresetJourneyNavigate: (Journey) -> Unit
) : RecyclerView.Adapter<PresetJourneyAdapter.Viewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.preset_journey_card_layout, parent, false)
        return Viewholder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set userInfoData to textview and imageview of each card layout
        val journey = presetJourneysArrayList[position]

        holder.journeyNameTV.text = journey.journeyName
        holder.journeyStartTV.text = "From: ${journey.sourceName}"
        holder.journeyStartAddrTV.text = journey.sourceAddress
        holder.journeyEndTV.text = "To: ${journey.destinationName}"
        holder.journeyEndAddrTV.text = journey.destinationAddress

        holder.itemView.setOnClickListener() {
            onPresetJourneyNavigate(journey)
        }

        holder.editBtn.setOnClickListener(
            onClickListener(journey)
        )

        holder.deleteBtn.setOnClickListener() {
            MaterialAlertDialogBuilder(context)
                .setTitle("Confirm deletion of ${journey.journeyName}?")
                .setNegativeButton("Cancel") { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton("Accept") { dialog, which ->
                    // Respond to positive button press
                    removeAt(position)
                }
                .show()
        }


    }

    override fun getItemCount(): Int {
        // this method is used for showing number
        // of card items in recycler view.
        return presetJourneysArrayList.size
    }

    private fun removeAt(position: Int) {
        journeyViewModel.delete(presetJourneysArrayList[position])
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, presetJourneysArrayList.size)
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journeyNameTV: TextView = itemView.findViewById(R.id.journeyName)
        val journeyStartTV: TextView = itemView.findViewById(R.id.journeyStart)
        val journeyStartAddrTV: TextView = itemView.findViewById(R.id.journeyStartAddress)
        val journeyEndTV: TextView = itemView.findViewById(R.id.journeyEnd)
        val journeyEndAddrTV: TextView = itemView.findViewById(R.id.journeyEndAddress)
        val editBtn: Button = itemView.findViewById(R.id.editPJ)
        val deleteBtn: Button = itemView.findViewById(R.id.deletePJ)

    }

    companion object {
        val TAG = PresetJourneyAdapter::class.java.simpleName
    }
}