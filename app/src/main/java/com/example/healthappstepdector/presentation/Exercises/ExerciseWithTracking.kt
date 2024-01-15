package com.example.healthappstepdector.presentation.Exercises

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.healthappstepdector.R
import com.example.healthappstepdector.presentation.readWriteCsv.writeExerciseSessionsToCSV
import com.example.healthappstepdector.presentation.viewmodel.ExerciseViewModel
import com.example.healthappstepdector.presentation.viewmodel.StepCounterViewModel
import kotlinx.coroutines.delay


const val ELEVATION_CHANGE_THRESHOLD = 1.5f // Define a threshold for significant elevation change
/**
 * Composable function to display exercise options and navigate to specific exercise tracking screens.
 *
 * @param navController Navigation controller for app navigation.
 * @param exerciseType The type of exercise (e.g., "Chair Squats", "Walking", etc.).
 * @param userName The name of the currently logged-in user.
 */
@Composable
fun ExerciseWithTracking(navController: NavController, exerciseType: String,userName: String) {

    val context = LocalContext.current

    val stepCounterViewModel = viewModel<StepCounterViewModel>()

    when (exerciseType) {
        "Chair Squats" -> TrackChairSquats(navController, context, userName, exerciseType)
        "Walking" -> WalkingExerciseOption(navController, context, userName, exerciseType)
        else -> TrackStretch(navController, context, userName, exerciseType)
    }
}

/**
 * Composable function to display options for the walking exercise.
 *
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param userName The name of the currently logged-in user.
 * @param exerciseType The type of exercise (e.g., "Walking").
 */
@Composable
fun WalkingExerciseOption(navController: NavController, context: Context, userName: String, exerciseType: String) {
    var showCustomDialog by remember { mutableStateOf(true) }

    if (showCustomDialog) {
        CustomDialog(
            onDismissRequest = { showCustomDialog = false },
            onConfirm = {
                showCustomDialog = false
                navController.navigate("trackStepsWithElevation/$exerciseType/$userName")
            },
            onDismiss = {
                showCustomDialog = false
                navController.navigate("trackWalking/$exerciseType/$userName")
            },
            iconId = R.drawable.stairs, // Replace with your drawable resource ID
            question = "Are you taking a staircase during your walk?"
        )
    }
}

/**
 * Composable function  to display a custom dialog for exercise options.
 *
 * @param onDismissRequest Callback when the dialog is dismissed.
 * @param onConfirm Callback when "Yes" is clicked.
 * @param onDismiss Callback when "No" is clicked.
 * @param iconId Drawable resource ID for the icon.
 * @param question The question to display in the dialog.
 */
@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    iconId: Int, // Drawable resource ID for the icon
    question: String
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black // or your desired background color
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null, // Provide appropriate description
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified // or your desired icon color
                )
                Text(text = question, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Row {
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                        Text("Yes")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("No")
                    }
                }
            }
        }
    }
}
/**
 *Composable function to track stretching exercise.
 *
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param userName The name of the currently logged-in user.
 * @param exerciseType The type of exercise (e.g., "Stretching").
 */
@Composable
fun TrackStretch(navController: NavController, context: Context, userName: String, exerciseType: String) {
    val totalReps = 3
    val currentRep = remember { mutableStateOf(1) }
    val holdTime = remember { mutableStateOf(15) }
    val isHolding = remember { mutableStateOf(true) }
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val viewModel: ExerciseViewModel = viewModel()

    LaunchedEffect(key1 = currentRep.value) {
        if (isHolding.value) {
            while (holdTime.value > 0) {
                delay(1000)
                holdTime.value -= 1
            }
            if (holdTime.value == 0) {
                vibratePhone(vibrator)
                if (currentRep.value < totalReps) {
                    currentRep.value += 1
                    holdTime.value = 15
                } else {
                    isHolding.value = false
                    viewModel.recordExerciseSession(userName, exerciseType,0.0f,0)
                    val exercises = viewModel.getCompletedExercises()
                    writeExerciseSessionsToCSV(context, exercises)
                    navController.navigate("welcome/$userName")
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CircularProgressIndicator(
            progress = (holdTime.value.toFloat() / 15f),
            strokeWidth = 8.dp,
            modifier = Modifier.size(500.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(exerciseType, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Rep ${currentRep.value} / $totalReps", fontSize = 14.sp, color = Color.Green)
            Text("${holdTime.value}", fontSize = 18.sp)
        }
    }
}
/**
 * Function to vibrate the phone or any Wearable Devices.
 *
 * @param vibrator The Vibrator service.
 */
fun vibratePhone(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        // Deprecated in API 26
        vibrator.vibrate(500)
    }
}

/**
 * Composable function  to track chair squats exercise.
 *
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param userName The name of the currently logged-in user.
 * @param exerciseType The type of exercise (e.g., "Chair Squats").
 */

@Composable
fun TrackChairSquats(navController: NavController, context: Context, userName: String, exerciseType: String) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    var calorieCount by remember { mutableStateOf(0f) }
    var repCount by remember { mutableStateOf(0) }
    var squatDetected by remember { mutableStateOf(false) }
    var lastSquatTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val animatedRepCount by animateIntAsState(targetValue = repCount)
    val animatedCalorieCount by animateFloatAsState(targetValue = calorieCount)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val totalSquats = 10
    val viewModel: ExerciseViewModel = viewModel()
    // UI feedback
    val squatFeedbackColor = if (squatDetected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Adjust these thresholds based on your observed pattern
    val downwardThreshold = -0.7f
    val upwardThreshold = 1.5f
    val minSquatDurationMillis = 1000 // Minimum duration between squats in milliseconds

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor == accelerometer) {
                val accelerationY = event.values[1]

                val currentTime = System.currentTimeMillis()
                val squatDuration = currentTime - lastSquatTime

                if (!squatDetected && accelerationY < downwardThreshold && squatDuration >= minSquatDurationMillis) {
                    // Detect the start of a squat
                    squatDetected = true
                } else if (squatDetected && accelerationY > upwardThreshold) {
                    // Detect the end of a squat
                    squatDetected = false
                    lastSquatTime = currentTime
                    if (repCount < totalSquats) {
                        repCount++
                        vibratePhone(vibrator)  // Vibrate the phone for each rep, but not after the last one
                    }
                    //vibratePhone(vibrator)

                    // Calculate calories burned based on the Y-axis acceleration
                    val K = 0.1f // You can adjust this constant
                    calorieCount += K * accelerationY
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, accelerometer, SENSOR_DELAY_UI)

    DisposableEffect(sensorManager) {
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    LaunchedEffect(key1 = repCount) {
        if (repCount >= totalSquats) {
            viewModel.recordExerciseSession(userName, exerciseType,calorieCount,0 )
            val exercises = viewModel.getCompletedExercises()
            writeExerciseSessionsToCSV(context, exercises)
            navController.navigate("welcome/$userName")
        }
    }



    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Chair Squat Repetitions: $animatedRepCount /10",
                fontSize = 18.sp,
                color = squatFeedbackColor)
            Text("Calories Burned: ${"%.2f".format(animatedCalorieCount)}",
                fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}
/**
 * Composable function  to display options for walking exercise.
 *
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param userName The name of the currently logged-in user.
 * @param exerciseType The type of exercise (e.g., "Walking").
 */
@Composable
fun TrackWalking(navController: NavController, context: Context, userName: String, exerciseType: String) {
    val stepSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepSensor = stepSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val viewModel: ExerciseViewModel = viewModel()

    // Local state for baseline, steps, and calories
    var baselineSteps by remember { mutableStateOf<Int?>(null) }
    var sessionSteps by remember { mutableStateOf(0) }
    var calories by remember { mutableStateOf(0f) }

    DisposableEffect(stepSensorManager) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    val totalSteps = event.values[0].toInt()
                    if (baselineSteps == null) {
                        baselineSteps = totalSteps // Set the baseline when the sensor is first read
                    }
                    sessionSteps = totalSteps - (baselineSteps ?: totalSteps)
                    calories = calculateCalories(sessionSteps)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        stepSensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)

        onDispose {
            stepSensorManager.unregisterListener(sensorEventListener)
        }
    }

    val goal = 20
    val progress = (sessionSteps.toFloat() / goal).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = 8.dp,
            modifier = Modifier.size(250.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.footsteps), // Replace with your drawable resource
                contentDescription = "Steps Icon",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified

            )
            Text("Steps", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("$sessionSteps / $goal", fontSize = 14.sp, color = Color.Green)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Calories",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    ": ${"%.1f".format(calories)} kcal",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
            if (sessionSteps >= goal) {
                vibratePhone(vibrator)
                Log.d("ExerciseLogging", "Recording session: User - $userName, Type - $exerciseType, Calories - $calories, Steps - $sessionSteps")
                Text("Goal Reached!", fontSize = 18.sp, color = Color.Green)
                viewModel.recordExerciseSession(userName, exerciseType, calories, sessionSteps)
                val exercises = viewModel.getCompletedExercises()
                writeExerciseSessionsToCSV(context, exercises)
                navController.navigate("welcome/$userName")
            }
        }
    }
}
/**
 * Composable function  to display options for walking  with elevation exercise.
 *
 * @param navController Navigation controller for app navigation.
 * @param context The current context.
 * @param userName The name of the currently logged-in user.
 * @param exerciseType The type of exercise (e.g., "Walking").
 */
@Composable
fun TrackStepsWithElevation(navController: NavController, context: Context, userName: String, exerciseType: String) {
    // Sensor Manager
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // States for step count, elevation, and calories
    var stepCount by remember { mutableStateOf(0) }
    var currentAltitude by remember { mutableStateOf(0f) }
    var lastAltitude by remember { mutableStateOf<Float?>(null) }
    var isElevationChangeDetected by remember { mutableStateOf(false) }
    var caloriesBurned by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()
    val viewModel: ExerciseViewModel = viewModel()
    var sessionSteps by remember { mutableStateOf(0) }
    var baselineSteps by remember { mutableStateOf<Int?>(null) }

    // Initialize sensors
    val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    DisposableEffect(sensorManager) {
        val stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && isElevationChangeDetected) {
                    val totalSteps = event.values[0].toInt()
                    if (baselineSteps == null) {
                        baselineSteps = totalSteps // Set baseline at the first sensor read
                    }
                    // Calculate session steps and calories
                    sessionSteps = totalSteps - (baselineSteps ?: totalSteps)
                    caloriesBurned = calculateCalories(sessionSteps)
                }
        }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val pressureListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val pressure = it.values[0]
                    val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)

                    lastAltitude?.let { last ->
                        if (kotlin.math.abs(altitude - last) > ELEVATION_CHANGE_THRESHOLD) {
                            isElevationChangeDetected = true
                            currentAltitude = altitude
                        }
                    }
                    lastAltitude = altitude
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        stepSensor?.let { sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST) }
        pressureSensor?.let { sensorManager.registerListener(pressureListener, it, SensorManager.SENSOR_DELAY_FASTEST) }

        onDispose {
            sensorManager.unregisterListener(stepListener)
            sensorManager.unregisterListener(pressureListener)
        }
    }

    // UI for displaying step count, elevation, and calories
    Column(modifier = Modifier.padding(16.dp).verticalScroll(scrollState),  horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = R.drawable.person), // Replace with your drawable resource
            contentDescription = "Person Icon",
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified

        )
        Text(text = "Step Count with Elevation: $sessionSteps", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Calories Burned: ${"%.2f".format(caloriesBurned)} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold,color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Record the session
            viewModel.recordExerciseSession(userName, exerciseType, caloriesBurned, sessionSteps)
            val exercises = viewModel.getCompletedExercises()
            writeExerciseSessionsToCSV(context, exercises)
            navController.navigate("welcome/$userName")
        }) {
            Text("Stop")
        }
    }
}

fun calculateCalories(steps: Int): Float {
    val calorieFactor = 0.04f // This is an estimated value, adjust based on your algorithm
    return steps * calorieFactor
}







