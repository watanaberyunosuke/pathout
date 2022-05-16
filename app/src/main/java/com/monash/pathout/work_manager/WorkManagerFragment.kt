package com.monash.pathout.work_manager

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.work.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.monash.pathout.databinding.WorkManagerFragmentBinding
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit

class WorkManagerFragment : Fragment() {

    private lateinit var binding: WorkManagerFragmentBinding
    private lateinit var picker: MaterialTimePicker
    private var calendar: Calendar? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WorkManagerFragmentBinding.inflate(layoutInflater)

        binding.selectNotificationTimeBtn.setOnClickListener {
            showTimePicker()
        }

        binding.scheduleNotificationBtn.setOnClickListener {
            if (calendar == null) {
                Toast.makeText(requireContext(), "Please select a time first", Toast.LENGTH_LONG)
                    .show()
            } else {
                setNotification()
            }

        }

        binding.nowNotificationBtn.setOnClickListener {
            setNotification(instant = true)
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(4)
            .setMinute(0)
            .setTitleText("Select Back Up Time")
            .build()

        picker.show(requireActivity().supportFragmentManager, "Pathout")

        picker.addOnPositiveButtonClickListener {
            binding.scheduledNotificationTime.text =
                "${String.format("%02d", picker.hour)}:${String.format("%02d", picker.minute)}"

            calendar = Calendar.getInstance()
            calendar!![Calendar.HOUR_OF_DAY] = picker.hour
            calendar!![Calendar.MINUTE] = picker.minute
            calendar!![Calendar.SECOND] = 0
            calendar!![Calendar.MILLISECOND] = 0
        }
    }

    private fun setNotification(instant: Boolean = false) {
        Log.d(TAG, "Invoking setNotification method")
        val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()

        Log.d(TAG, "Added data payload for notification")

        Log.d(TAG, "Calling scheduleNotification method")
        if (instant) {
            scheduleNotification(0, data)
            return
        }

        val selectedTime = calendar?.timeInMillis
        val currentTime = currentTimeMillis()
        val delay = selectedTime?.minus(currentTime)
        if (delay != null) {
            scheduleNotification(delay, data)
            Toast.makeText(
                requireContext(),
                "Notification scheduled for ${
                    String.format(
                        "%02d",
                        picker.hour
                    )
                }:${String.format("%02d", picker.minute)}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        const val NOTIFICATION_ID = "Pathout_notification_id"
        const val NOTIFICATION_WORK = "Pathout_notification_work"
        val TAG = WorkManagerFragment::class.java.simpleName
    }

    private fun scheduleNotification(delay: Long, data: Data) {
        Log.d(TAG, "In scheduleNotification method, scheduling for delay of $delay ms")
        if (delay == 0L) {
            val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

            Log.d(TAG, "Starting uniqueWork using the NotificationWorker (work enqueued)")
            val instanceWorkManager = WorkManager.getInstance(requireContext())
            instanceWorkManager.beginUniqueWork(
                NOTIFICATION_WORK,
                ExistingWorkPolicy.REPLACE,
                notificationWork
            ).enqueue()
        } else {
            val notificationWork =
                PeriodicWorkRequest.Builder(NotificationWorker::class.java, 1, TimeUnit.DAYS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

            Log.d(TAG, "Starting uniqueWork using the NotificationWorker (work enqueued)")
            val instanceWorkManager = WorkManager.getInstance(requireContext())
            instanceWorkManager.enqueueUniquePeriodicWork(
                NOTIFICATION_WORK,
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }
}