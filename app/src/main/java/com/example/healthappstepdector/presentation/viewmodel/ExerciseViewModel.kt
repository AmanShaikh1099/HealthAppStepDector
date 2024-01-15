package com.example.healthappstepdector.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.healthappstepdector.presentation.DataClasses.ExerciseSession
import java.time.LocalDateTime

class ExerciseViewModel : ViewModel() {
    private val completedExercises = mutableListOf<ExerciseSession>()

    fun recordExerciseSession(userName: String, exerciseName: String,calories: Float,steps: Int) {
        val session = ExerciseSession(
            userName = userName,
            exerciseName = exerciseName,
            dateTime = LocalDateTime.now(),
            calories = calories,
            steps = steps
        )
        Log.d("ExerciseSessionLog", "Recording session: $session")
        completedExercises.add(session)
    }

    fun getCompletedExercises(): List<ExerciseSession> = completedExercises
}