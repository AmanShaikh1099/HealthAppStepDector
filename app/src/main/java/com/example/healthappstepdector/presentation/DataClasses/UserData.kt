package com.example.healthappstepdector.presentation.DataClasses

data class UserData (
    val username: String,
    var breaks: Int,
    val exercisesPerformed: String,
    val lastExercisePerformed: String,
    val healthStatus: String,
    var lastLogin: String
)