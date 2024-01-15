// MainActivity.kt
package com.example.healthappstepdector.presentation

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthappstepdector.presentation.Exercises.ExerciseGifWithTTS
import com.example.healthappstepdector.presentation.Exercises.ExerciseLogsScreen
import com.example.healthappstepdector.presentation.Exercises.ExerciseWithTracking
import com.example.healthappstepdector.presentation.Exercises.ExercisesWithTutorials
import com.example.healthappstepdector.presentation.Exercises.MotivationScreen
import com.example.healthappstepdector.presentation.Exercises.StretchingOptions
import com.example.healthappstepdector.presentation.Exercises.TodayExerciseLogsScreen
import com.example.healthappstepdector.presentation.Exercises.TrackStepsWithElevation
import com.example.healthappstepdector.presentation.Exercises.TrackWalking
import com.example.healthappstepdector.presentation.theme.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE
import com.example.healthappstepdector.presentation.theme.HealthAppStepDectorTheme
import com.example.healthappstepdector.presentation.theme.SelectUserScreen
import com.example.healthappstepdector.presentation.theme.UserDetailsScreen
import com.example.healthappstepdector.presentation.theme.WelcomeScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "health_app_channel"
    private val NOTIFICATION_ID = 1
    private var navControllerState: MutableState<NavHostController?> = mutableStateOf(null)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fromNotification = intent.getBooleanExtra("fromNotification", false)
        val userName = intent.getStringExtra("userName") ?: ""

        setContent {
            HealthAppStepDectorTheme {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val navController = rememberNavController()
                navControllerState.value = navController
                val context = LocalContext.current
                val notificationManager = NotificationManagerCompat.from(this@MainActivity)
                createNotificationChannel(CHANNEL_ID,this@MainActivity)

                NavHost(navController, startDestination = if (fromNotification) "exercisesWithTutorials/$userName" else "home") {
                    composable("home") {
                        JetpackComposeHealthApp(navController, notificationManager, CHANNEL_ID, NOTIFICATION_ID,this@MainActivity,context)
                    }
                    composable("selectUser") {
                        SelectUserScreen(navController, context)
                    }
                    composable("welcome/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        WelcomeScreen(userName, navController, context,fromNotification)
                    }
                    composable("userDetails/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        UserDetailsScreen(userName, context)
                    }
                    composable(
                        "exercisesWithTutorials/{userName}",
                        arguments = listOf(navArgument("userName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("userName") ?: ""
                        ExercisesWithTutorials(navController, username)
                    }

                    composable("motivationScreen/{userName}") {backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        MotivationScreen(navController, userName) // Ensure userName is passed correctly
                    }


                    composable(
                        "stretchingOptions/{userName}",
                        arguments = listOf(navArgument("userName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        StretchingOptions(navController, userName)
                    }

                    composable(
                        "exerciseGifWithTTS/{exerciseName}/{userName}",
                        arguments = listOf(
                            navArgument("exerciseName") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        ExerciseGifWithTTS(navController, exerciseName, userName)
                    }

                    composable(
                        "exerciseWithTracking/{exerciseType}/{userName}",
                        arguments = listOf(
                            navArgument("exerciseType") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val exerciseType = backStackEntry.arguments?.getString("exerciseType") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        ExerciseWithTracking(navController, exerciseType, userName)
                    }
                    composable(
                        "trackStepsWithElevation/{exerciseType}/{userName}",
                        arguments = listOf(
                            navArgument("exerciseType") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val exerciseType = backStackEntry.arguments?.getString("exerciseType") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        TrackStepsWithElevation(navController, LocalContext.current, userName, exerciseType)
                    }
                    composable(
                        "trackWalking/{exerciseType}/{userName}",
                        arguments = listOf(
                            navArgument("exerciseType") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val exerciseType = backStackEntry.arguments?.getString("exerciseType") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        TrackWalking(navController, LocalContext.current, userName, exerciseType)
                    }

                    composable("exerciseLogs/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        ExerciseLogsScreen(navController, LocalContext.current, userName)
                    }
                    composable("todayExerciseLogsScreen/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        TodayExerciseLogsScreen(navController, LocalContext.current, userName)
                    }


                }

            }
        }
        lifecycleScope.launchWhenStarted {
            while(navControllerState.value == null) {
                delay(100) // Wait until NavController is initialized
            }
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val navigateTo = intent.getStringExtra("navigateTo")
        val userName = intent.getStringExtra("userName")

        if (navigateTo == "exercisesWithTutorials" && userName != null) {
            navControllerState.value?.navigate("exercisesWithTutorials/$userName")
        }
    }
}



private fun createNotificationChannel(channelId: String, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Health App Channel"
        val descriptionText = "Notification channel for Health App"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}



@Composable
fun JetpackComposeHealthApp(
    navController: NavHostController,
    notificationManager: NotificationManagerCompat,
    channelId: String,
    notificationId: Int,
    activity: Activity,
    context: Context,


)  {
    var isDialogOpen by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Customize the Button to have a grey background and white text
        Button(
            onClick = {
                isDialogOpen = true
                requestNotificationPermission(context)
            },
            modifier = Modifier
                .background(Color.Black) // Set the background color to grey
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White
            )
        ) {
            // Customize the Text color to be white
            Text(text = "Start", color = Color.White)
        }

        // Displays a dialog with a list of users
        if (isDialogOpen) {
            navController.navigate("selectUser")
        }
    }
}
private fun requestNotificationPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val notificationPermission = "com.example.healthappstepdector.permission.POST_NOTIFICATIONS"
        if (checkSelfPermission(
                context,
                "android.permission.ACTIVITY_RECOGNITION"
            ) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(context, notificationPermission) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    "android.permission.ACTIVITY_RECOGNITION",
                    notificationPermission
                ),
                ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
