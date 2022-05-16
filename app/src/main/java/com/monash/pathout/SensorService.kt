package com.monash.pathout

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.monash.pathout.ui.MainActivity

// Modified and adapted from reference: https://www.raywenderlich.com/10838302-sensors-tutorial-for-android-getting-started
class SensorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val accelerometerVal = FloatArray(3)
    private val magnetometerVal = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val notificationActivityRequestCode = 0
    private val notificationId = 1

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Register accelerometer with normal sensor delay
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it, SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        // Register magnetic field (magnetometer) sensor with normal sensor delay
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        val notification = createNotification("NA", 0.0)
        startForeground(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerVal, 0, accelerometerVal.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerVal, 0, magnetometerVal.size)
        }

        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerVal,
            magnetometerVal
        )
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
        val angle = (degrees * 100) / 100

        val direction = getDirection(degrees)

        val intent = Intent()
        intent.putExtra("COMPASS_ANGLE", angle)
        intent.putExtra("COMPASS_DIRECTION", direction)
        intent.action = "COMPASS_ON_SENSOR_CHANGED"

        // Broadcast the intent
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun getDirection(angle: Double): String {
        var direction = ""

        when {
            angle >= 350 || angle <= 10 -> direction = "N"
            angle < 350 && angle > 280 -> direction = "NW"
            angle <= 280 && angle > 260 -> direction = "W"
            angle <= 260 && angle > 190 -> direction = "SW"
            angle <= 190 && angle > 170 -> direction = "S"
            angle <= 170 && angle > 100 -> direction = "SE"
            angle <= 100 && angle > 80 -> direction = "E"
            angle <= 80 && angle > 10 -> direction = "NE"
        }

        return direction
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createNotification(direction: String, angle: Double): Notification {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                application.packageName,
                "Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configure the notification channel.
            notificationChannel.enableLights(false)
            notificationChannel.setSound(null, null)
            notificationChannel.enableVibration(false)
            notificationChannel.vibrationPattern = longArrayOf(0L)
            notificationChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(baseContext, application.packageName)
        // Open activity intent
        val contentIntent = PendingIntent.getActivity(
            this, notificationActivityRequestCode,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("You're currently facing $direction at an angle of $angle°")
            .setWhen(System.currentTimeMillis())
            .setDefaults(0)
            .setVibrate(longArrayOf(0L))
            .setSound(null)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(contentIntent)

        return notificationBuilder.build()
    }
}