package com.example.healthappstepdector.presentation.ForegroundServices
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
/**
 * Worker for periodically checking if the Step Detector Service is running.
 *
 * This Worker is responsible for ensuring that the Step Detector Service is active.
 * If the service is not running, it starts the service. It's typically scheduled to
 * run at regular intervals to maintain continuous step detection.
 *
 * @param context The context used for accessing application-specific resources and classes.
 * @param workerParams Parameters for configuring the behavior of this worker.
 */
class StepDetectorCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    /**
     * The main logic of the Worker that performs the check and potentially starts the Step Detector Service.
     *
     * @return The result of the work, indicating whether it was successful or requires a retry.
     */
    override fun doWork(): Result {
        if (!isServiceRunning(applicationContext)) {
            val serviceIntent = Intent(applicationContext, StepDetectorService::class.java)
            applicationContext.startService(serviceIntent)
        }
        return Result.success()
    }
    /**
     * Checks if the Step Detector Service is currently running.
     *
     * This method checks shared preferences to determine if the service is active.
     *
     * @param context The context used to access shared preferences.
     * @return True if the service is running, false otherwise.
     */
    private fun isServiceRunning(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("StepDetectorServiceRunning", false)
    }
}