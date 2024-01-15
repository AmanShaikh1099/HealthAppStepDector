package Services
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.healthappstepdector.R
/**
 * A service responsible for counting steps using the Step Detector sensor.
 *
 * This service detects steps and broadcasts events to inform the app when a step is detected.
 * It also runs as a foreground service to ensure continued operation and provides notifications.
 */
class StepCountingService :  Service() {

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val stepDetectorSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    private val stepDetectorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Handle step detector events here
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                // Check if a step is detected
                Log.d("StepCountingService", "Step detected!")

                // Broadcast an event to inform the app that a step is detected
                val intent = Intent("step_detected")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Register the listener
        sensorManager.registerListener(
            stepDetectorListener,
            stepDetectorSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        // Register a receiver to handle step detection events
        val receiver = StepDetectedReceiver()
        val filter = IntentFilter("step_detected")
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)

        // Start foreground service (even if no notification is shown)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener when the service is destroyed
        sensorManager.unregisterListener(stepDetectorListener)

        // Unregister the receiver
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(StepDetectedReceiver())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Inner class to handle step detection events locally
    private inner class StepDetectedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Log statements in the context of WelcomeScreen.kt when a step is detected
            Log.d("WelcomeScreen", "Step detected in the background!")
        }
    }private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counting Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counting Service")
            .setContentText("Step Counting Service is running.")
            .setSmallIcon(R.drawable.ic_coffee_break)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }



    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "step_counting_channel"
    }
}