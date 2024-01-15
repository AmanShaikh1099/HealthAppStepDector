package com.example.healthappstepdector.presentation.DataClasses

import java.time.LocalDateTime
/**
 * Represents an exercise session for a user.
 *
 * This data class holds information about a specific exercise session, including the user's name,
 * the name of the exercise performed, the date and time of the session, and metrics such as calories burned and steps taken.
 *
 * @property userName The name of the user who completed the exercise session.
 * @property exerciseName The name of the exercise performed during the session.
 * @property dateTime The date and time when the exercise session occurred.
 * @property calories The number of calories burned during the exercise session.
 * @property steps The number of steps taken during the exercise session, if applicable.
 */
data class ExerciseSession(
    val userName: String,
    val exerciseName: String,
    val dateTime: LocalDateTime,
    val calories: Float ,
    val steps: Int
)
