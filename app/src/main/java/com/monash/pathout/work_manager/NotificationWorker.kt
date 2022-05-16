package com.monash.pathout.work_manager

import android.app.Notification.DEFAULT_ALL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.monash.pathout.R
import com.monash.pathout.ui.MainActivity

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun doWork(): Result {
        val id = inputData.getLong(NOTIFICATION_ID, 0).toInt()
        sendNotification(id)

        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(id: Int) {
        Log.d(WorkManagerFragment.TAG, "In sendNotification method of Worker class")
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val titleNotification = "Here is your notification!"
        val subtitleNotification = "Pathout Notification"
        val pendingIntent = getActivity(applicationContext, 0, intent, FLAG_IMMUTABLE)

        Log.d(WorkManagerFragment.TAG, "Building notification and setting Pending Intent")
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_baseline_schedule_24)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.priority = PRIORITY_MAX

        Log.d(WorkManagerFragment.TAG, "Setting channel ID and creating notification channel")
        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
        Log.d(WorkManagerFragment.TAG, "Notification worker is finished executing its work")
    }

    companion object {
        const val NOTIFICATION_ID = "Pathout_notification_id"
        const val NOTIFICATION_NAME = "Pathout"
        const val NOTIFICATION_CHANNEL = "Pathout_channel_01"
    }
}