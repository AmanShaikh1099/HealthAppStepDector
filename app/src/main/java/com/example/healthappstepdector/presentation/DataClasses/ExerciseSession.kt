package com.example.healthappstepdector.presentation.DataClasses

import java.time.LocalDateTime

data class ExerciseSession(
    val userName: String,
    val exerciseName: String,
    val dateTime: LocalDateTime,
    val calories: Float ,
    val steps: Int
)
