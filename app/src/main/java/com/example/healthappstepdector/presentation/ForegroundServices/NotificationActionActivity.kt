package com.example.healthappstepdector.presentation.ForegroundServices

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthappstepdector.presentation.MainActivity

/**
 * Activity to handle actions from notifications.
 *
 * This activity is triggered when a user interacts with a notification from the app.
 * It primarily handles the "YES" action from the notification, which navigates the user to a specific screen.
 */
class NotificationActionActivity : AppCompatActivity() {
    //Companion object for handling Yes
    companion object {
        const val YES_ACTION = "com.example.healthappstepdector.YES_ACTION"
        // Add other constants here if needed
    }
    /**
     * Handles the creation of the activity and processes the intent that started it.
     *
     * If the intent's action is YES_ACTION, the user is navigated to the exercises tutorial screen.
     * The userName and other relevant data are passed along to the next activity.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
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
