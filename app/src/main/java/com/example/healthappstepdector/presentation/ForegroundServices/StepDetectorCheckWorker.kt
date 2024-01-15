package com.example.healthappstepdector.presentation.ForegroundServices
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class StepDetectorCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        if (!isServiceRunning(applicationContext)) {
            val serviceIntent = Intent(applicationContext, StepDetectorService::class.java)
            applicationContext.startService(serviceIntent)
        }
        return Result.success()
    }

    private fun isServiceRunning(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("StepDetectorServiceRunning", false)
    }
}