package com.example.healthappstepdector.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
/**
 * ViewModel for managing step count and calorie data.
 *
 * This ViewModel maintains the step count and calculated calories burned during physical activities.
 * It provides functionality to update the step count and automatically updates the calories burned based on the step count.
 */
class StepCounterViewModel : ViewModel() {

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private val _caloriesBurned = MutableLiveData<Float>()
    val caloriesBurned: LiveData<Float> = _caloriesBurned

    /**
     * Updates the current step count and calculates the calories burned.
     *
     * This function sets the new step count value and updates the calories burned based on the step count.
     * The calories are calculated using a predefined factor.
     *
     * @param newCount The new step count value to be updated.
     */
    fun updateStepCount(newCount: Int) {
        _stepCount.value = newCount
        _caloriesBurned.value = calculateCalories(newCount)
    }


    /**
     * Calculates the calories burned based on the number of steps taken.
     *
     * This function uses a predefined factor to convert steps into an equivalent calorie count.
     *
     * @param steps The number of steps taken.
     * @return The calculated calories burned.
     */
    private fun calculateCalories(steps: Int): Float {
        val calorieFactor = 0.04f // Adjust as needed
        return steps * calorieFactor
    }

}