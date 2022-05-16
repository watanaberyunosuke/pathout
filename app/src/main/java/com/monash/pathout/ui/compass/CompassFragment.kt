package com.monash.pathout.ui.compass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.monash.pathout.SensorService
import com.monash.pathout.databinding.CompassFragmentBinding

class CompassFragment : Fragment() {
    private var binding: CompassFragmentBinding? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CompassFragmentBinding.inflate(inflater, container, false)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val direction = intent?.getStringExtra("COMPASS_DIRECTION")
                val angle = intent?.getDoubleExtra("COMPASS_ANGLE", 0.0)
                binding!!.directionTV.text = "${
                    String.format("%.2f", angle)
                } $direction"
                binding!!.directionTV.gravity = Gravity.CENTER_HORIZONTAL
                binding!!.compassImageView.rotation = angle?.toFloat()?.times(-1) ?: 1F
            }
        }

        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                it, IntentFilter("COMPASS_ON_SENSOR_CHANGED")
            )
        }

        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        startForegroundServiceForSensors()
    }

    override fun onDestroy() {
        super.onDestroy()

        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
                it
            )
        }
    }

    private fun startForegroundServiceForSensors() {
        val intent = Intent(requireContext(), SensorService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }
}