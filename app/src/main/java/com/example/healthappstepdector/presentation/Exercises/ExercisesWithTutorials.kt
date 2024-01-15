package com.example.healthappstepdector.presentation.Exercises

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.healthappstepdector.R
import com.example.healthappstepdector.presentation.DataClasses.ExerciseSession
import com.example.healthappstepdector.presentation.readWriteCsv.readExerciseSessionsFromCSV
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
/**
* Composable function that displays a list of exercises with tutorials.
*
* @param navController The NavController to handle navigation.
* @param userName The name of the user.
*/
@Composable
fun ExercisesWithTutorials(navController: NavController,userName: String) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
            .verticalScroll(scrollState),// Adjust padding as needed
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExerciseItem("Stretching", R.drawable.stretching, navController,userName)
        ExerciseItem("Chair Squats", R.drawable.squat, navController,userName)
        ExerciseItem("Walking", R.drawable.walking, navController,userName)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Go Back",
            modifier = Modifier
                .padding(8.dp)
                .clickable { navController.navigate("welcome/${userName}")}, // Navigate to the WelcomeScreen destination
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "My Logs",
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    navController.navigate("todayExerciseLogsScreen/$userName")
                },
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
/**
* Composable function that displays an exercise GIF with Text-to-Speech (TTS) instructions.
*
* @param navController The NavController to handle navigation.
* @param exerciseName The name of the exercise.
* @param userName The name of the user.
*/
@Composable
fun ExerciseGifWithTTS(navController: NavController, exerciseName: String,userName: String) {
    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) {} }
    var currentStep by remember { mutableStateOf(0) }
    var resetGifKey by remember { mutableStateOf(0) }


    val exerciseData = mapOf(
        "Trapezius Neck Stretch" to Pair(
            R.raw.nechstrechs, listOf(
                "",
                "Sit up straight in your office chair, feet flat on the floor. Keep your back straight and your shoulders relaxed, not hunched.",
                "Place your hand on the opposite side of your shoulder",
                "Gently tilt your head to one side, bringing your ear closer to the shoulder until you feel a stretch on the opposite side of your neck.",
                "Hold this position for 15-30 seconds, breathing deeply and relaxing into the stretch.",
                "Slowly lift your head back to the starting position.",
                "Repeat on the other side."
            )
        ),

        "Wrist Stretch" to Pair(
            R.raw.wriststrech, listOf(
                "",
                "Stand up and extend your right arm forward at shoulder height With your elbow straight, grasp your right hand with the left one",
                "Bend the wrist backward until you feel the stretch in your forearm Hold and then bend the wrist downward",
                "Switch arms and repeat."
            )
        ),
        "Back Stretch" to Pair(
            R.raw.backstrech2, listOf(
                "",
                "While sitting, grab the arm or the seat of your chair with both hands.",
                " With your buttocks staying stable, twist your upper body, using the arms or the seat of the chair to pull yourself into more of a twist",
                "Repeat on the other side."
            )
        ),
        "Chair Squats" to Pair(
            R.raw.chairsquats, listOf(
                "",
                "Place a chair behind you.Standup straight with a tight core. Fold your arms front, your  feet should  be shoulder width and toes pointing forward.",
                "Slowly decend by bending your Knees and driving your hips back.Keep your chest and head up. Touch the chair with your butt",
                "Slowly rise back to the starting position.."
            )
        )
    )

    val (gifResource, instructions) = exerciseData[exerciseName] ?: Pair(
        R.raw.trapeziusneckstrech,
        listOf()
    )
    val gifUri = "android.resource://${context.packageName}/${gifResource}?key=$resetGifKey"

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val imagePainter = rememberAsyncImagePainter(model = gifUri, imageLoader = imageLoader)
    DisposableEffect(Unit) {
        onDispose {
            resetGifKey++ // Increment key on dispose
        }
    }


    DisposableEffect(key1 = tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = imagePainter,
            contentDescription = "$exerciseName GIF",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Button(
            onClick = {
                tts.stop()
                resetGifKey++
                navController.navigate("exerciseWithTracking/${exerciseName}/$userName")
            },
            // Position your button, e.g., at the bottom of the image
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Skip")
        }
    }


        // Read out the instructions with a delay between each one.
        instructions.forEachIndexed { index, instruction ->
            LaunchedEffect(key1 = index) {
                delay(8000 * index.toLong()) // Assuming each instruction is 8 seconds apart.
                tts.speak(instruction, TextToSpeech.QUEUE_ADD, null, null)
            }
        }

    }


/**
 * Composable function that represents an exercise item.
 *
 * @param name The name of the exercise.
 * @param drawableResId The resource ID of the exercise's icon.
 * @param navController The NavController to handle navigation.
 * @param userName The name of the user.
 */
@Composable
fun ExerciseItem(name: String, drawableResId: Int, navController: NavController,userName: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                when (name) {
                    "Stretching" -> navController.navigate("stretchingOptions/$userName")
                    "Walking" -> navController.navigate("exerciseWithTracking/Walking/$userName")
                    "Chair Squats"->navController.navigate("exerciseGifWithTTS/Chair Squats/$userName")

                }
            },
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = drawableResId),
                contentDescription = "$name Icon",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
/**
 * Composable function that displays stretching exercise options.
 *
 * @param navController The NavController to handle navigation.
 * @param userName The name of the user.
 */
@Composable
fun StretchingOptions(navController: NavController,userName: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Align items horizontally
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("exerciseGifWithTTS/Trapezius Neck Stretch/$userName") }
            ) {
                Icon(painter = painterResource(id = R.drawable.neck), contentDescription = "Trapezius Neck Stretch", tint = Color.Unspecified)
                Text(
                    "Trapezius Neck Stretch",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("exerciseGifWithTTS/Wrist Stretch/$userName") }
            ) {
                Icon(painter = painterResource(id = R.drawable.wrist), contentDescription = "Wrist Stretch", tint = Color.Unspecified)
                Text(
                    "Wrist Stretch",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("exerciseGifWithTTS/Back Stretch/$userName") }
            ) {
                Icon(painter = painterResource(id = R.drawable.back), contentDescription = "Back Stretch", tint = Color.Unspecified)
                Text(
                    "Back Stretch",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
/**
 * Composable function that displays exercise logs for till current day.
 *
 * @param navController The NavController to handle navigation.
 * @param context The Android context.
 * @param userName The name of the user.
 */
@Composable
fun ExerciseLogsScreen(navController: NavController,context: Context, userName: String) {
    val exerciseLogs = remember { mutableStateOf<List<ExerciseSession>>(listOf()) }
    val today = LocalDate.now()

    LaunchedEffect(key1 = Unit) {
        exerciseLogs.value = readExerciseSessionsFromCSV(context)
            .filter { it.userName == userName && it.dateTime.toLocalDate().isEqual(today) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (exerciseLogs.value.isNotEmpty()) {
                exerciseLogs.value.forEach { session ->
                    Text(
                        text = "Exercise: ${session.exerciseName}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            } else {
                Text(
                    text = "No exercises logged today",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Text(
            text = "Go Back",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clickable { navController.popBackStack() },
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
/**
 * Composable function that displays exercise logs for the current day.
 *
 * @param navController The NavController to handle navigation.
 * @param context The Android context.
 * @param userName The name of the user.
 */
@Composable
fun TodayExerciseLogsScreen(navController: NavController, context: Context, userName: String) {
    val exerciseLogs = remember { mutableStateOf<List<ExerciseSession>>(listOf()) }
    val today = LocalDate.now()

    LaunchedEffect(key1 = Unit) {
        val allSessions = readExerciseSessionsFromCSV(context)
            .filter { it.userName == userName && it.dateTime.toLocalDate().isEqual(today) }

        // Log the sessions read from CSV
        allSessions.forEach { session ->
            Log.d("OriginalSession", "Session from CSV: User - ${session.userName}, Exercise - ${session.exerciseName}, DateTime - ${session.dateTime}, Calories - ${session.calories}, Steps - ${session.steps}")
        }

        val deduplicatedSessions = allSessions
            .groupBy { Pair(it.exerciseName, it.dateTime.truncatedTo(ChronoUnit.MINUTES)) }
            .mapValues { (_, sessions) -> sessions.maxByOrNull { it.dateTime } }
            .values
            .filterNotNull() // Add this line to filter out null values
            .toList()



        // Log the deduplicated sessions
        deduplicatedSessions.forEach { session ->
            Log.d("DeduplicatedSession", "Deduplicated session: User - ${session.userName}, Exercise - ${session.exerciseName}, DateTime - ${session.dateTime}, Calories - ${session.calories}, Steps - ${session.steps}")
        }

        exerciseLogs.value = deduplicatedSessions
    }

    val totalCalories = exerciseLogs.value.sumOf { it.calories.toDouble() }.toFloat()
    val totalSteps = exerciseLogs.value.sumOf { it.steps }

    // Log the total calculations
    Log.d("TodayExerciseLogs", "Total Calories: $totalCalories, Total Steps: $totalSteps")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                color = Color.Green,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (exerciseLogs.value.isNotEmpty()) {
                val groupedExercises = exerciseLogs.value.groupingBy { it.exerciseName }.eachCount()

                groupedExercises.forEach { (exerciseName, count) ->
                    Text(
                        text = "$exerciseName x$count",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Calories Burned: ",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${"%.2f".format(totalCalories)}",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Steps: ",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$totalSteps",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = "No exercises logged today",
                    color = Color.Green,
                    fontSize = 16.sp
                )
            }
        }

        Text(
            text = "Go Back",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .clickable { navController.popBackStack() },
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
