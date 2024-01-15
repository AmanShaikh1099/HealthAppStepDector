package com.example.healthappstepdector.presentation.DataClasses

/**
 * Represents a user's data in the health app.
 *
 * This data class encapsulates various details about a user, including their username,
 * number of breaks taken, details about exercises performed, health status, and last login time.
 *
 * @property username The unique identifier for the user.
 * @property breaks The number of breaks the user has taken, modifiable as the user takes more breaks.
 * @property exercisesPerformed A string summarizing the exercises performed by the user.
 * @property lastExercisePerformed The name of the last exercise the user performed.
 * @property healthStatus A string representing the user's current health status.
 * @property lastLogin The last date and time when the user logged into the app, modifiable upon each login.
 */
data class UserData (
    val username: String,
    var breaks: Int,
    val exercisesPerformed: String,
    val lastExercisePerformed: String,
    val healthStatus: String,
    var lastLogin: String
)