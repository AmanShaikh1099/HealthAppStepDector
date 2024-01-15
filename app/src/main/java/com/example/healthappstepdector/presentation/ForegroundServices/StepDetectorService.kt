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
/**
 * A service for detecting steps in the background.
 *
 * This service uses the device's step detector sensor to count steps. It runs as a foreground service
 * and sends broadcasts about step counts and periods of no movement. The service ensures it stays awake
 * using a WakeLock.
 */
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
    /**
     * Initializes the service, registers the step detector sensor, and acquires a WakeLock.
     */
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
    /**
     * Starts the service as a foreground service with a notification and registers the sensor listener.
     * It also starts a coroutine to monitor periods of inactivity (no steps detected).
     *
     * @param intent The Intent supplied to startService(), as given.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's current started state.
     */
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
    /**
     * Saves the current running state of the service in shared preferences.
     *
     * @param context The context to access shared preferences.
     * @param isRunning The current running state of the service.
     */
    private fun setServiceRunningFlag(context: Context, isRunning: Boolean) {
        val sharedPreferences = context.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("StepDetectorServiceRunning", isRunning).apply()
    }
    /**
     * Handles step detection events from the sensor and broadcasts the updated step count.
     *
     * @param event The SensorEvent.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            Log.d("StepDetectorService", "Step detected! Total steps: $stepCounter")
            sendStepCountBroadcast()
            stepCounter++
            lastStepTimeMillis = System.currentTimeMillis()

        }
    }
    /**
     * Handles accuracy changes in the sensor. Can be used to manage sensor accuracy if necessary.
     *
     * @param sensor The sensor whose accuracy has changed.
     * @param accuracy The new accuracy of this sensor.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle sensor accuracy changes if necessary
    }
    /**
     * Sends a broadcast indicating the current step count.
     */
    private fun sendStepCountBroadcast() {
        val intent = Intent("com.example.healthappstepdector.STEP_COUNT_UPDATE")
        intent.putExtra("stepCount", stepCounter)
        sendBroadcast(intent)
    }
    /**
     * Sends a broadcast indicating that no movement has been detected for a predefined period.
     */
    private fun sendNoMovementBroadcast() {
        val intent = Intent("com.example.healthappstepdector.NO_MOVEMENT")
        sendBroadcast(intent)
    }
    /**
     * Required method for binding - not used in this service.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an IBinder through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        // Return null as this is a started service, not a bound service.
        return null
    }

    /**
     * Unregisters the sensor listener and releases the WakeLock when the service is destroyed.
     */
    override fun onDestroy() {
        Log.d("StepDetectorService", "Service Destroyed")
        isServiceRunning = false
        setServiceRunningFlag(this, false)
        sensorManager.unregisterListener(this)
        super.onDestroy()
        wakeLock?.release()
    }


}
