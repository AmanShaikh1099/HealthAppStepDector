package com.example.healthappstepdector.presentation.ForegroundServices

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthappstepdector.presentation.MainActivity

class NotificationActionActivity : AppCompatActivity() {
    companion object {
        const val YES_ACTION = "com.example.healthappstepdector.YES_ACTION"
        // Add other constants here if needed
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (intent.action == YES_ACTION) {
            val userName = intent.getStringExtra("userName") // Retrieve userName from the intent
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("navigateTo", "exercisesWithTutorials")
                putExtra("userName", userName)
                putExtra("fromNotification", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(mainIntent)

        }
        finish()
    }
}
