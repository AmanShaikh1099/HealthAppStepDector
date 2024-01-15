package com.example.healthappstepdector.presentation.ForegroundServices
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healthappstepdector.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StepDetectorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var stepCounter = 0
    private var lastStepTimeMillis = System.currentTimeMillis()
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        @Volatile
        var isServiceRunning = false
            private set
    }
    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::StepDetectorWakeLock")
        wakeLock?.acquire()
        isServiceRunning = true
        Log.d("StepDetectorService", "Service Created")
        setServiceRunningFlag(this, true)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepDetectorSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Creates a notification channel for Android O and above
        Log.d("StepDetectorService", "Service Started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "stepServiceChannel",
                "Step Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, "stepServiceChannel")
            .setContentTitle("Step Detector Running")
            .setContentText("Counting steps...")
            .setSmallIcon(R.drawable.walking) // Replace with your app's icon
            .build()

        startForeground(1, notification)

        stepDetectorSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
               // delay(10000)
                if ((System.currentTimeMillis() - lastStepTimeMillis) > 1 * 60 * 1000) { // No steps for 10 minutes
                    sendNoMovementBroadcast()
                    lastStepTimeMillis = System.currentTimeMillis()
                }

            }
        }

        return START_STICKY
    }

    private fun setServiceRunningFlag(context: Context, isRunning: Boolean) {
        val sharedPreferences = context.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("StepDetectorServiceRunning", isRunning).apply()
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            Log.d("StepDetectorService", "Step detected! Total steps: $stepCounter")
            sendStepCountBroadcast()
            stepCounter++
            lastStepTimeMillis = System.currentTimeMillis()

        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle sensor accuracy changes if necessary
    }

    private fun sendStepCountBroadcast() {
        val intent = Intent("com.example.healthappstepdector.STEP_COUNT_UPDATE")
        intent.putExtra("stepCount", stepCounter)
        sendBroadcast(intent)
    }

    private fun sendNoMovementBroadcast() {
        val intent = Intent("com.example.healthappstepdector.NO_MOVEMENT")
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        // Return null as this is a started service, not a bound service.
        return null
    }

    override fun onDestroy() {
        Log.d("StepDetectorService", "Service Destroyed")
        isServiceRunning = false
        setServiceRunningFlag(this, false)
        sensorManager.unregisterListener(this)
        super.onDestroy()
        wakeLock?.release()
    }


}
