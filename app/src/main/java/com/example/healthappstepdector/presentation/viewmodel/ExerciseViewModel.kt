package com.example.healthappstepdector.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.healthappstepdector.presentation.DataClasses.ExerciseSession
import java.time.LocalDateTime
/**
 * ViewModel for managing exercise-related data.
 *
 * This ViewModel handles the recording and retrieval of exercise sessions. It maintains a list of completed
 * exercise sessions and provides functionality to record new sessions and retrieve the list of completed sessions.
 */
class ExerciseViewModel : ViewModel() {
    private val completedExercises = mutableListOf<ExerciseSession>()


    /**
     * Records a new exercise session for a user.
     *
     * Adds a new `ExerciseSession` to the list of completed exercises with the provided details.
     * The session is logged for debugging purposes.
     *
     * @param userName The name of the user who completed the exercise.
     * @param exerciseName The name of the exercise performed.
     * @param calories The number of calories burned during the exercise.
     * @param steps The number of steps taken during the exercise, if applicable.
     */
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
    /**
     * Retrieves the list of completed exercise sessions.
     *
     * @return A list of `ExerciseSession` objects representing all completed exercises.
     */
    fun getCompletedExercises(): List<ExerciseSession> = completedExercises
}