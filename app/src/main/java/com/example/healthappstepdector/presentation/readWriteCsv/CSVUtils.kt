package com.example.healthappstepdector.presentation.readWriteCsv

import android.content.Context
import android.util.Log
import com.example.healthappstepdector.presentation.DataClasses.ExerciseSession
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Scanner

fun writeExerciseSessionsToCSV(context: Context, sessions: List<ExerciseSession>) {
    val fileName = "exercise_data.csv"
    val file = File(context.filesDir, fileName)

    FileWriter(file, true).use { writer ->
        if (file.length() == 0L) {
            // Updated header to include Steps
            writer.append("UserName,ExerciseName,DateTime,Calories,Steps\n")
        }
        sessions.forEach { session ->
            Log.d("CSVWriteCheck", "Session to write: User - ${session.userName}, Exercise - ${session.exerciseName}, DateTime - ${session.dateTime}, Calories - ${session.calories}, Steps - ${session.steps}")
            val dateTimeStr = session.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // Include steps in the CSV line
            writer.append("${session.userName},${session.exerciseName},$dateTimeStr,${session.calories},${session.steps}\n")
        }
    }
}


fun readExerciseSessionsFromCSV(context: Context): List<ExerciseSession> {
    val fileName = "exercise_data.csv"
    val file = File(context.filesDir, fileName)
    val exerciseSessions = mutableListOf<ExerciseSession>()
    val currentDate = LocalDate.now()

    if (!file.exists()) {
        return exerciseSessions
    }

    Scanner(file, StandardCharsets.UTF_8.name()).use { scanner ->
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            val tokens = line.split(',')

            // Skip the header line or any malformed line
            if (tokens.size == 5 && tokens[0] != "UserName") {
                val userName = tokens[0]
                val exerciseName = tokens[1]
                val dateTime = LocalDateTime.parse(tokens[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val sessionDate = dateTime.toLocalDate()

                // Only add the session if the date matches today's date
                if (sessionDate.isEqual(currentDate)) {
                    val calories = tokens[3].toFloatOrNull() ?: 0f
                    val steps = tokens[4].toIntOrNull() ?: 0

                    Log.d("CSVReadCheck", "Reading session: User - $userName, Exercise - $exerciseName, DateTime - $dateTime, Calories - $calories, Steps - $steps")

                    exerciseSessions.add(ExerciseSession(userName, exerciseName, dateTime, calories, steps))
                }
            }
        }
    }
    return exerciseSessions
}
