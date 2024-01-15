// Import statements
package com.example.healthappstepdector.presentation.theme
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.navigation.NavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthappstepdector.R
import com.example.healthappstepdector.presentation.DataClasses.ExerciseSession
import com.example.healthappstepdector.presentation.DataClasses.UserData
import com.example.healthappstepdector.presentation.ForegroundServices.NotificationActionActivity
import com.example.healthappstepdector.presentation.ForegroundServices.StepDetectorCheckWorker
import com.example.healthappstepdector.presentation.ForegroundServices.StepDetectorService
import com.example.healthappstepdector.presentation.readWriteCsv.readExerciseSessionsFromCSV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit





private const val NOTIFICATION_CHANNEL_ID = "step_detector_channel"
private const val PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
const val ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 123
private const val YES_ACTION = "com.example.healthappstepdector.YES_ACTION"
private const val NO_ACTION = "com.example.healthappstepdector.NO_ACTION"
/**
 * Displays the welcome screen of the health app step detector.
 *
 * This screen presents user details, step counter, exercise sessions, and various user options.
 * It also handles permission requests and initializes step detection services.
 *
 * @param userName The name of the currently logged-in user.
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param fromNotification Boolean indicating if this screen was launched from a notification.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun WelcomeScreen(userName: String, navController: NavController, context: Context,fromNotification: Boolean) {
    val userDetailsState = remember { mutableStateOf<UserData?>(null) }
    val stepCounter = remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var currentTime by remember { mutableStateOf(getFormattedTime()) }
    var showBreakDetails by remember { mutableStateOf(false) }
    var showLastExerciseDetails by remember { mutableStateOf(false) }
    var showHealthStatusDetails by remember { mutableStateOf(false) }
    val exerciseSessions = remember { mutableStateOf<List<ExerciseSession>>(listOf()) }
    var healthStatusColor by remember { mutableStateOf(Color.White) }
    var healthStatusText by remember { mutableStateOf("Low") }
    var timeSinceLastExercise by remember { mutableStateOf("No recent exercise") }
    val loadedUserData = readUserDetailsFromExcel(userName, context)
    val updatedUserData = loadedUserData ?: UserData(userName, 0, "Chair Squats", "None", "Low",lastLogin = getFormattedDate())
    val loadDataTrigger = remember { mutableStateOf(false) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("WelcomeScreen", "BODY_SENSORS_BACKGROUND permission granted")
            startStepDetectorService(context)
        } else {
            Log.d("WelcomeScreen", "BODY_SENSORS_BACKGROUND permission denied")
        }
    }
    LaunchedEffect(loadDataTrigger.value) {
        userDetailsState.value = readUserDetailsFromExcel(userName, context)

    }
    if (fromNotification) {
        LaunchedEffect(userName) {
            incrementBreaksValue(userName, context)
            checkAndUpdateBreaks(context, userName)
            refreshUserData(userName, context, userDetailsState, coroutineScope)
        }
    }
    val yesPendingIntent =
        createYesPendingIntent(context,userName)
    val noPendingIntent =
        createPendingIntent(context, 2, NO_ACTION)

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val destinationFilePath = context.filesDir.resolve("DataClasses/user_data.csv.xlsx").absolutePath
    copyFileToDestination(context, "UserData/user_data.csv.xlsx", destinationFilePath)

    val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    YES_ACTION -> {
                        Log.d("Notification", "Yes clicked")
                        // Handle "Yes" action
                        context?.let { ctx ->
                            navController.navigate("exercisesWithTutorials")
                            val notificationManager = NotificationManagerCompat.from(ctx)
                            notificationManager.cancel(1)
                        }
                    }
                   NO_ACTION -> {
                        Log.d("Notification", "No clicked")
                        navController.navigate("motivationScreen/{userName}")
                        val notificationManager = NotificationManagerCompat.from(context!!)
                        notificationManager.cancel(1)
                        // Handle "No" action
                    }
                    else -> {
                        Log.d("Notification", "Unexpected action: ${it.action}")
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        val workRequest = PeriodicWorkRequestBuilder<StepDetectorCheckWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("WelcomeScreen", "Scheduled StepDetectorCheckWorker")
    }
    LaunchedEffect(Unit) {
        promptDisableBatteryOptimization(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("WelcomeScreen", "Requesting BODY_SENSORS_BACKGROUND permission")
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS_BACKGROUND)
        } else {
            Log.d("WelcomeScreen", "Checking if StepDetectorService is running")
            if (!StepDetectorService.isServiceRunning) {
                Log.d("WelcomeScreen", "Starting StepDetectorService for older Android versions")
                startStepDetectorService(context)
            } else {
                Log.d("WelcomeScreen", "StepDetectorService is already running")
            }

        }
    }

    // BroadcastReceiver to handle step count updates
    DisposableEffect(Unit) {
        val stepCountReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.example.healthappstepdector.STEP_COUNT_UPDATE" -> {

                        stepCounter.value = intent.getIntExtra("stepCount", 0)
                        Log.d("StepDetection", "Step count updated: ${stepCounter.value}")
                    }
                    "com.example.healthappstepdector.NO_MOVEMENT" -> {
                        context?.let { ctx ->
                            Log.d("StepDetection", "Background")
                            showNoMovementNotification(ctx, yesPendingIntent,
                                noPendingIntent)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("com.example.healthappstepdector.STEP_COUNT_UPDATE")
            addAction("com.example.healthappstepdector.NO_MOVEMENT")
        }
        context.registerReceiver(stepCountReceiver, filter)

        onDispose {
            context.unregisterReceiver(stepCountReceiver)
        }
    }

    // Register the receiver
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(YES_ACTION)
            addAction(NO_ACTION)
        }
        context.registerReceiver(notificationReceiver, filter)

        onDispose {
            // Unregister the receiver when the composable is disposed
            context.unregisterReceiver(notificationReceiver)
        }
    }
     LaunchedEffect(userName) {
        requestActivityRecognitionPermission(context)
        exerciseSessions.value = readExerciseSessionsFromCSV(context)
        loadDataTrigger.value = !loadDataTrigger.value
        val currentDate = LocalDate.now()
        val totalStepsToday = exerciseSessions.value.filter { it.dateTime.toLocalDate().isEqual(currentDate) }.sumOf { it.steps }
        val totalCaloriesToday = exerciseSessions.value
            .filter { it.dateTime.toLocalDate().isEqual(currentDate) }
            .fold(0f) { total, session -> total + session.calories }

        // Determine health status color
        healthStatusColor = if (totalStepsToday > 100 || totalCaloriesToday > 50f) Color.Green else Color.Red
        healthStatusText = if (healthStatusColor == Color.Green) "Good" else "Low"
        val mostRecentExerciseSession = findMostRecentExerciseSessionForUser(userName, context)
        mostRecentExerciseSession?.let {
            timeSinceLastExercise = timeAgo(it.dateTime)
        }
        checkAndUpdateBreaks(context, userName)


        Log.d("WelcomeScreen", "Loading user details for user: $userName")
        withContext(Dispatchers.IO) {
            checkAndUpdateBreaks(context, userName)
           refreshUserData(userName, context, userDetailsState, coroutineScope)
            // Update the break count in user data with the value from SharedPreferences
            context?.let { ctx ->
                val sharedPreferences = ctx.getSharedPreferences("UserBreaks", Context.MODE_PRIVATE)
                val breaks = sharedPreferences.getInt("$userName-breaks", 0)
                updatedUserData.breaks = breaks
                withContext(Dispatchers.Main) {
                    userDetailsState.value = updatedUserData
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getFormattedTime()
            delay(60000) // Delay for 1 minute
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            userDetailsState.value?.let { userDetails ->

                Text(
                    text = " ${userDetails.username}",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Active: $timeSinceLastExercise",
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ActivityIconButton(
                            iconRes = R.drawable.workbreak,
                            contentDescription = "Breaks",
                            onClick = { showBreakDetails = !showBreakDetails }
                        )
                        if (showBreakDetails) {
                            Log.d("DisplayBreaks", " user ${userDetails.username} Breaks: ${userDetails.breaks}")
                            Text(text = "Breaks: ${userDetails.breaks}",color=Color.White)
                        } else {
                            Text(text = "Breaks",color=Color.White)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ActivityIconButton(
                            iconRes = R.drawable.running,
                            contentDescription = "Last Exercise",
                            onClick = { showLastExerciseDetails = !showLastExerciseDetails }
                        )
                        if (showLastExerciseDetails) {
                            userDetailsState.value?.lastExercisePerformed?.let { lastExercise ->
                                Text(text = "Last Exercise: $lastExercise", color = Color.White)
                            } ?: Text(text = "No exercise data available", color = Color.White)
                        } else {
                            Text(text = "Last Exercise", color = Color.White)
                        }
                    }
                }

                // Second row with one icon
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ActivityIconButton(
                            iconRes = R.drawable.healthstatus,
                            contentDescription = "Health Status",
                            onClick = { showHealthStatusDetails = !showHealthStatusDetails }
                        )
                        if (showHealthStatusDetails) {
                            Text(
                                text = "Health Status: $healthStatusText",
                                color = healthStatusColor
                            )
                        } else {
                            Text(text = "Health Status", color = Color.White)
                        }

                    }
                }



                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Go Back",
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black)
                            .padding(8.dp)
                            .clickable { navController.navigate("selectUser") },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold

                    )

                    Spacer(modifier = Modifier.width(2.dp)) // Add space between the buttons

                    Text(
                        text = "Start my day",
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black)
                            .padding(8.dp)
                            .clickable { navController.navigate("exercisesWithTutorials/$userName") },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } ?: run {
                // Handle the case when userDetailsState.value is null
                Text(text = "User details not found")
            }
        }
    }
}

/**
 * Starts the step detector service.
 *
 * This function initiates the StepDetectorService to track steps in the background.
 *
 * @param context The current context used to start the service.
 */

fun startStepDetectorService(context: Context) {
    val serviceIntent = Intent(context, StepDetectorService::class.java)
    ContextCompat.startForegroundService(context, serviceIntent)
}
/**
 * Reads user details from an Excel file and returns a UserData object.
 *
 * This function opens an Excel file, reads the details for a specific user, and constructs a UserData object.
 * It returns `null` if the user is not found or in case of an error.
 *
 * @param userName The username to look up in the Excel file.
 * @param context The current context, used to access application assets.
 * @return A UserData object containing the user's details, or null if not found.
 */
private fun readUserDetailsFromExcel(userName: String, context: Context): UserData? {
    val fileName = "user_data.csv.xlsx"
    val internalStorageFolderPath = context.filesDir.absolutePath + "/DataClasses/"
    val internalFilePath = "$internalStorageFolderPath$fileName"

    val file = File(internalFilePath)
    if (!file.exists() || file.length() == 0L) {
        Log.e("ExcelRead", "File does not exist or is empty at path: $internalFilePath")
        return null
    }

    return try {
        FileInputStream(file).use { fileInputStream ->
            val workbook: Workbook = XSSFWorkbook(fileInputStream)
            val sheet = workbook.getSheet("Sheet1")

            if (sheet != null) {
                for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(rowIndex)
                    val cell = row?.getCell(0)
                    if (cell != null && cell.stringCellValue.equals(userName, ignoreCase = true)) {
                        val breaksCell = row.getCell(1)
                        Log.e("ExcelRead", "Breaks: $breaksCell")
                        val exercisesPerformedCell = row.getCell(2)
                        val lastExercisePerformedCell = row.getCell(3)
                        Log.e("ExcelRead", "lastExercisePerformedCell: $lastExercisePerformedCell")
                        val healthStatusCell = row.getCell(4)
                        val lastLoginCell = row.getCell(5)

                        val breaks = breaksCell?.numericCellValue?.toInt() ?: 0
                        val exercisesPerformed = exercisesPerformedCell?.stringCellValue ?: ""
                        val lastExercisePerformed = lastExercisePerformedCell?.stringCellValue ?: ""
                        val healthStatus = healthStatusCell?.stringCellValue ?: ""
                        val lastLogin = lastLoginCell?.stringCellValue ?: getFormattedDate()

                        workbook.close()
                        return UserData(
                            username = userName,
                            breaks = breaks,
                            exercisesPerformed = exercisesPerformed,
                            lastExercisePerformed = lastExercisePerformed,
                            healthStatus = healthStatus,
                            lastLogin = lastLogin
                        )
                    }
                }
            }
            workbook.close()
            Log.d("ExcelRead", "User not found: $userName")
            null
        }
    } catch (e: Exception) {
        Log.e("ExcelRead", "Error reading user data from Excel", e)
        null
    }
}
/**
 * Copies a file from the application's assets to the internal storage.
 *
 * This function is typically used to copy a file from the app's assets directory to a location
 * in the internal storage where it can be modified or accessed more easily.
 * If the file already exists in the destination, it will not be copied again.
 *
 * @param context The current context used to access the application's assets.
 * @param assetFileName The name of the file in the assets directory to be copied.
 * @param destinationFilePath The file path in the internal storage where the file will be copied to.
 */
private fun copyFileToDestination(context: Context, assetFileName: String, destinationFilePath: String) {
    try {
        val destinationFile = File(destinationFilePath)

        if (!destinationFile.parentFile.exists()) {
             destinationFile.parentFile.mkdirs()
        }

        if (!destinationFile.exists()) {
            // Copy the file only if it doesn't exist in the destination folder
            context.assets.open(assetFileName).use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            println("File copied successfully to: ${destinationFile.absolutePath}")
        } else {
            println("File already exists in the destination folder: ${destinationFile.absolutePath}")
        }
    } catch (e: IOException) {
        println("Error copying file from assets to internal storage: $e")
    }
}
/**
 * Increments the break count for a given user in shared preferences and updates the Excel file.
 *
 * The function checks if the current date is different from the last recorded date and resets the count if necessary.
 * It then increments the count, updates the shared preferences, and writes the new data to the Excel file.
 *
 * @param userName The name of the user for whom to increment the break count.
 * @param context The current context.
 */
private fun incrementBreaksValue(userName: String, context: Context?) {
    context?.let { ctx ->
        val sharedPreferences = ctx.getSharedPreferences("UserBreaks", Context.MODE_PRIVATE)
        val currentDate = getFormattedDate()
        val lastBreakDate = sharedPreferences.getString("$userName-date", "")

        val newBreaks = if (lastBreakDate != currentDate) {
            1 // Reset breaks count since the date has changed
        } else {
            sharedPreferences.getInt("$userName-breaks", 0) + 1 // Increment the breaks count
        }

        Log.d("incrementBreaksValue", "Incrementing breaks for $userName: $newBreaks")

        val editor = sharedPreferences.edit()
        editor.putInt("$userName-breaks", newBreaks)
        editor.putString("$userName-date", currentDate)
        editor.apply() // Use apply for asynchronous commit

        val lastExerciseSession = findMostRecentExerciseSessionForUser(userName, ctx)
        val lastExercisePerformed = lastExerciseSession?.exerciseName ?: "No recent exercise"

        try {
            writeUserDetailsToExcel(userName, newBreaks, lastExercisePerformed, ctx)
            Log.d("incrementBreaksValue", "Updated Excel for $userName")
        } catch (e: Exception) {
            Log.e("incrementBreaksValue", "Error writing to Excel", e)
        }
    } ?: Log.e("incrementBreaksValue", "Context is null")
}
/**
 * Writes updated user details to an Excel file.
 *
 * This function updates or creates a row in the Excel file for a specific user with the provided details,
 * including break count, last exercise performed, and other user-specific data.
 *
 * @param userName The name of the user whose details are being updated.
 * @param newBreaks The updated number of breaks taken by the user.
 * @param lastExercisePerformed The name of the last exercise performed by the user.
 * @param context The current context used to access application assets.
 */
private fun writeUserDetailsToExcel(
    userName: String,
    newBreaks: Int,
    lastExercisePerformed: String,
    context: Context
) {
    val fileName = "user_data.csv.xlsx"
    val internalStorageFolderPath = context.filesDir.absolutePath + "/DataClasses/"
    val internalFilePath = "$internalStorageFolderPath$fileName"
    val currentDate = getFormattedDate()

    try {
        val file = File(internalFilePath)
        val workbook: Workbook
        val sheet: Sheet

        if (!file.exists() || file.length() == 0L) {
            // Create a new workbook and sheet if the file doesn't exist or is empty
            workbook = XSSFWorkbook()
            sheet = workbook.createSheet("Sheet1")
        } else {
            FileInputStream(file).use { fis ->
                workbook = XSSFWorkbook(fis)
                sheet = workbook.getSheet("Sheet1") ?: workbook.createSheet("Sheet1")
            }
        }

        val userRowNumber = findRowNumberForUsername(userName, context) ?: sheet.physicalNumberOfRows
        val row = sheet.getRow(userRowNumber) ?: sheet.createRow(userRowNumber)


        val nameCell = row.createCell(0)
        nameCell.setCellValue(userName)

        val breaksCell = row.createCell(1)
        breaksCell.setCellValue(newBreaks.toDouble())

        val exercisesPerformed  = row.createCell(2)
        exercisesPerformed .setCellValue("xyz")

        val exerciseCell = row.createCell(3)
        exerciseCell.setCellValue(lastExercisePerformed)

        val healthStatus= row.createCell(4)
        healthStatus.setCellValue("Low")

        val dateCell = row.createCell(5)
        dateCell.setCellValue(currentDate)

        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()
    } catch (e: Exception) {
        Log.e("ExcelUpdate", "Error updating Excel file", e)
    }
}
/**
 * Finds the row number in the Excel file that corresponds to the specified username.
 *
 * This function scans the Excel file to find a user's row based on their username.
 * It is used to locate where to update user-specific data in the file.
 *
 * @param userName The username for which to find the corresponding row number.
 * @param context The current context used to access application assets.
 * @return The row number where the user's data is located, or null if the user is not found.
 */
private fun findRowNumberForUsername(userName: String, context: Context): Int? {
    val fileName = "user_data.xlsx" // Updated to a more standard file name
    val internalStorageFolderPath = context.filesDir.absolutePath + "/DataClasses/"
    val internalFilePath = "$internalStorageFolderPath$fileName"

    val file = File(internalFilePath)
    if (!file.exists() || file.length() == 0L) {
        Log.e("ExcelError", "File does not exist or is empty")
        return null
    }

    try {
        FileInputStream(file).use { fileInputStream ->
            val workbook: Workbook = XSSFWorkbook(fileInputStream)
            val sheet: Sheet = workbook.getSheet("Sheet1")

            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex)
                if (row != null) {
                    val cell = row.getCell(0)
                    if (cell != null && cell.stringCellValue.equals(userName, ignoreCase = true)) {
                        workbook.close()
                        return rowIndex
                    }
                }
            }
            workbook.close()
        }
    } catch (e: IOException) {
        Log.e("ExcelError", "Error finding row number", e)
    } catch (e: Exception) {
        Log.e("ExcelError", "An unexpected error occurred", e)
    }
    return null
}
/**
 * Refreshes the user data state based on updated information.
 *
 * This coroutine function fetches the most recent user data, including step count, exercise sessions,
 * and break count, and updates the UI state accordingly. It reads data from both the Excel file and Shared Preferences.
 *
 * @param userName The name of the user whose data is being refreshed.
 * @param context The current context.
 * @param userDetailsState The MutableState object to be updated with the latest user data.
 * @param coroutineScope The CoroutineScope in which to launch asynchronous tasks.
 */
fun refreshUserData(
    userName: String,
    context: Context,
    userDetailsState: MutableState<UserData?>,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        val originalUserData = readUserDetailsFromExcel(userName, context)
        val mostRecentSession = findMostRecentExerciseSessionForUser(userName, context)

        val sharedPreferences = context.getSharedPreferences("UserBreaks", Context.MODE_PRIVATE)
        val currentBreaks = sharedPreferences.getInt("$userName-breaks", 0)

        val updatedUserData = if (mostRecentSession != null && originalUserData != null) {
            // Update originalUserData with the most recent session details
            originalUserData.copy(
                breaks = currentBreaks,
                lastExercisePerformed = "${mostRecentSession.exerciseName}"
            )
        } else if (originalUserData == null) {
            // New user with no exercise data - create a new UserData instance with default values
            UserData(
                username = userName,
                breaks =currentBreaks,
                exercisesPerformed = "",
                lastExercisePerformed = "No recent exercise",
                healthStatus = "",
                lastLogin = getFormattedDate()
            )
        } else {
            // User has existing data but no recent exercise
            originalUserData
        }
        Log.d("refreshUserData", "Updating user data for $userName: Breaks = ${updatedUserData.lastExercisePerformed}")
        Log.d("refreshUserData", "Updating user data for $userName: Breaks = $currentBreaks")
        withContext(Dispatchers.Main) {
            userDetailsState.value = updatedUserData
        }
    }
}
/**
 * Checks and updates the break count for a user. Resets the count if a new day starts.
 *
 * @param context Context to access shared preferences.
 * @param userName Username for which to check and update breaks.
 */
private fun checkAndUpdateBreaks(context: Context, userName: String) {
    val sharedPreferences = context.getSharedPreferences("UserBreaks", Context.MODE_PRIVATE)
    val lastBreakDate = sharedPreferences.getString("$userName-date", null) ?: getFormattedDate()

    // Retrieve the last exercise performed
    val lastExerciseSession = findMostRecentExerciseSessionForUser(userName, context)
    val lastExercisePerformed = lastExerciseSession?.exerciseName ?: "No recent exercise"

    val currentDate = getFormattedDate()

    if (lastBreakDate != currentDate) {
        // Reset breaks for the new day and update the last exercise in Excel
        sharedPreferences.edit().apply {
            putInt("$userName-breaks", 0)
            putString("$userName-date", currentDate)
            apply()
        }

        // Write the updated break count and last exercise session to Excel
        writeUserDetailsToExcel(userName, 0, lastExercisePerformed, context)
    } else {
        // Update only the last exercise session to Excel without resetting the break count
        val currentBreaks = sharedPreferences.getInt("$userName-breaks", 0)
        Log.d("CheckUpdate", "Updated breaks for $userName: $currentBreaks")
        writeUserDetailsToExcel(userName, currentBreaks, lastExercisePerformed, context)
    }
}
/**
 * Finds the most recent exercise session for a specified user.
 *
 * This function reads all exercise sessions from a CSV file and returns the latest one for the given user.
 *
 * @param userName The name of the user whose exercise session is to be found.
 * @param context The current context used to access application assets.
 * @return The most recent ExerciseSession for the user, or null if no session is found.
 */
fun findMostRecentExerciseSessionForUser(userName: String, context: Context): ExerciseSession? {
    val allSessions = readExerciseSessionsFromCSV(context)
    return allSessions.filter { it.userName == userName }
        .maxByOrNull { it.dateTime }
}
/**
 * Creates a PendingIntent for broadcasting or handling specific actions.
 *
 * @param context The current context.
 * @param requestCode The request code to identify this PendingIntent.
 * @param action The action string that this PendingIntent will handle.
 * @return The created PendingIntent.
 */
fun createPendingIntent(context: Context, requestCode: Int, action: String): PendingIntent {
    val intent = Intent(action).apply {
        putExtra("requestCode", requestCode)
    }

    // Specify FLAG_IMMUTABLE for the PendingIntent
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    return PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        flags
    )
}
/**
 * Creates a PendingIntent to handle the "Yes" action in notifications.
 *
 * When triggered, it navigates to the specified activity or screen in the app.
 *
 * @param context The current context.
 * @param userName The username to be passed to the intent for further use.
 * @return A PendingIntent configured to handle the "Yes" action.
 */
fun createYesPendingIntent(context: Context, userName: String): PendingIntent {
    val yesIntent = Intent(context, NotificationActionActivity::class.java).apply {
        action = YES_ACTION
        putExtra("navigateTo", "exercisesWithTutorials")
        putExtra("userName", userName) // Include the userName
    }
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    return PendingIntent.getActivity(context, 0, yesIntent, flags)
}
/**
 * Displays a notification to the user suggesting a break when no movement is detected.
 *
 * The notification includes actions for the user to respond.
 *
 * @param context The current context used for creating the notification.
 * @param yesPendingIntent The PendingIntent to execute when the "Yes" action is selected.
 * @param noPendingIntent The PendingIntent to execute when the "No" action is selected.
 */
private  fun showNoMovementNotification(
    context: Context,
    yesPendingIntent: PendingIntent,
    noPendingIntent: PendingIntent
) {
    val notificationPermission = "${context.packageName}.permission.POST_NOTIFICATIONS"

    if (checkSelfPermission(context, PERMISSION_POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        Log.e("Notification", "Permission to post notifications not granted.")
        return
    }

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("No Movement Detected")
        .setContentText("Consider taking a break and stretching.")
        .setSmallIcon(R.drawable.heart)
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.exercise))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setStyle(NotificationCompat.BigTextStyle())
        .addAction(1, "Yes", yesPendingIntent)
        .addAction(2, "No", noPendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1, notification)
    Log.d("Notification", "Notification displayed.")
}
/**
 * Requests activity recognition permission from the user.
 *
 * This is necessary for certain versions of Android to allow the app to track physical activity.
 *
 * @param context The current context where the permission request will be made.
 */
private fun requestActivityRecognitionPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (checkSelfPermission(context, "android.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(context, PERMISSION_POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    "android.permission.ACTIVITY_RECOGNITION",
                    PERMISSION_POST_NOTIFICATIONS
                ),
                ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE
            )
        }
    }
}

/**
 * Checks if the app is optimized for battery usage.
 *
 * Determines whether the app is on the battery optimization whitelist.
 * Being optimized can affect background processing.
 *
 * @param context The current context.
 * @return True if the app is optimized (whitelisted), false otherwise.
 */

private fun isAppOptimized(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
/**
 * Prompts the user to disable battery optimization for the app.
 *
 * This is important for services like step detection to work reliably in the background.
 *
 * @param context The current context used to display the settings screen.
 */
private fun promptDisableBatteryOptimization(context: Context) {
    if (!isAppOptimized(context)) {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

/**
 * Represents an icon used in the app's activities.
 *
 * This data class holds information about an activity icon including its resource ID,
 * description, and detail type.
 *
 * @property iconRes The resource ID of the icon.
 * @property contentDescription A description of the icon used for accessibility.
 * @property detailType A type descriptor of the icon's purpose or associated activity.
 */
data class ActivityIcon(val iconRes: Int, val contentDescription: String, val detailType: String)

// Define your data model
val activities = listOf(
    ActivityIcon(R.drawable.workbreak, "Breaks", "breaks"),
    ActivityIcon(R.drawable.healthstatus, "Health Status", "healthStatus"),
    ActivityIcon(R.drawable.running, "Last Exercise", "lastExercise")
)
/**
 * Creates a clickable icon button for different activities in the app.
 *
 * @param iconRes Resource ID for the icon image.
 * @param contentDescription Text description of the icon for accessibility.
 * @param onClick Callback function to be invoked when the icon is clicked.
 */
@Composable
fun ActivityIconButton(@DrawableRes iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.Unspecified,  // Add tint color for visibility
            modifier = Modifier
                .size(86.dp)
                .background(Color.Unspecified, CircleShape)
                .padding(16.dp)
        )
    }
}
/**
 * Gets the current time in a formatted string.
 *
 * This function returns the current system time in "HH:mm" format.
 *
 * @return The current time as a formatted string.
 */
fun getFormattedTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date())
}
/**
 * Gets the current date in a formatted string.
 *
 * This function returns the current system date in "yyyy-MM-dd" format.
 *
 * @return The current date as a formatted string.
 */
fun getFormattedDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}
/**
 * Calculates how much time has passed since a given LocalDateTime and returns a descriptive string.
 *
 * Useful for displaying the elapsed time in a human-readable format.
 *
 * @param exerciseDateTime The date and time to calculate the elapsed time from.
 * @return A string describing how long ago the provided date and time occurred.
 */
fun timeAgo(exerciseDateTime: LocalDateTime): String {
    val duration = Duration.between(exerciseDateTime, LocalDateTime.now())
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    return when {
        hours > 0 -> "$hours hours and $minutes minutes ago"
        minutes > 0 -> "$minutes minutes ago"
        else -> "Just now"
    }
}


