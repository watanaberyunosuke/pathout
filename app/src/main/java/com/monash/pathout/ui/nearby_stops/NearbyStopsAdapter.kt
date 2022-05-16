package com.monash.pathout.ui.nearby_stops

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.monash.pathout.R
import com.monash.pathout.model.Stop
import com.monash.pathout.ui.nearby_stops.NearbyStopsAdapter
import com.monash.pathout.ui.stop_details.StopDetailsFragment
import com.monash.pathout.viewmodel.StopViewModel

class NearbyStopsAdapter(
    private val context: Context,
    private val stopsArrayList: List<Stop>,
    private val stopViewModel: StopViewModel
) : RecyclerView.Adapter<NearbyStopsAdapter.Viewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.stop_card_layout, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set userInfoData to textview and imageview of each card layout
        val stop = stopsArrayList[position]
        val routeString = stop.routesString

        holder.stopNameTV.text = stop.stopName
        holder.stopDistanceTV.text = (Math.round(stop.stopDistance * 100) / 100).toString() + "m"
        holder.routeNumberTV.text = routeString

        val TRAIN = 0
        val TRAM = 1
        val BUS = 2
        val VLINE = 3
        val NIGHT_BUS = 4

        when (stop.routeType) {
            TRAIN -> holder.stopIV.setImageResource(R.drawable.ic_baseline_train_24)
            TRAM -> holder.stopIV.setImageResource(R.drawable.ic_baseline_tram_24)
            BUS, NIGHT_BUS -> holder.stopIV.setImageResource(R.drawable.ic_baseline_directions_bus_24)
            else -> holder.stopIV.setImageResource(R.drawable.ic_baseline_navigation_24)
        }

        holder.itemView.setOnClickListener {
            stopViewModel.setStop(stop)
            val manager = (context as AppCompatActivity).supportFragmentManager
            manager.beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    StopDetailsFragment(),
                    StopDetailsFragment.TAG
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return stopsArrayList.size
    }

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stopIV: ImageView = itemView.findViewById(R.id.stopTypeImage)
        val stopNameTV: TextView = itemView.findViewById(R.id.stopName)
        val stopDistanceTV: TextView = itemView.findViewById(R.id.stopDistance)
        val routeNumberTV: TextView = itemView.findViewById(R.id.routeNumber)

    }

    companion object {
        val TAG = NearbyStopsAdapter::class.java.simpleName
    }
}