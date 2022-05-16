package com.monash.pathout.ui.stop_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.monash.pathout.R
import com.monash.pathout.model.Departure
import com.monash.pathout.ui.stop_details.StopDetailsAdapter
import com.monash.pathout.viewmodel.StopViewModel

class StopDetailsAdapter(
    private val context: Context,
    private val departuresArrayList: List<Departure>,
    private val stopViewModel: StopViewModel
) : RecyclerView.Adapter<StopDetailsAdapter.Viewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        // to inflate the layout for each item of recycler view.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.departure_card_layout, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // to set userInfoData to textview and imageview of each card layout
        val TRAIN = 0
        val departure = departuresArrayList[position]
        val routes = stopViewModel.getStop().value!!.routes

        if (stopViewModel.getStop().value!!.routeType != TRAIN) {
            if (routes != null) {
                for (route in routes) {
                    if (route.routeId == departure.routeId) {
                        holder.routeNumberTV.text = route.routeNumber
                        holder.routeNameTV.text = route.routeName
                        holder.routeScheduleTV.text = departure.calculateTimeDifference()
                    }
                }
            }
        } else {
            if (routes != null) {
                for (route in routes) {
                    if (route.routeId == departure.routeId) {
                        holder.routeNumberTV.text = route.routeName
                        holder.routeNameTV.text = if (departure.platformNumber != null) {
                            String.format("Platform %s", departure.platformNumber.toString())
                        } else {
                            ""
                        }

                        holder.routeScheduleTV.text = departure.calculateTimeDifference()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        // this method is used for showing number
        // of card items in recycler view.
        return departuresArrayList.size
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeNumberTV: TextView = itemView.findViewById(R.id.deptRouteNumber)
        val routeNameTV: TextView = itemView.findViewById(R.id.deptRouteName)
        val routeScheduleTV: TextView = itemView.findViewById(R.id.deptRouteSchedule)

    }

    companion object {
        val TAG = StopDetailsAdapter::class.java.simpleName
    }
}