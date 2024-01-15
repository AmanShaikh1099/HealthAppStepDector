package com.example.healthappstepdector.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCounterViewModel : ViewModel() {

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> = _stepCount

    private val _caloriesBurned = MutableLiveData<Float>()
    val caloriesBurned: LiveData<Float> = _caloriesBurned

    fun updateStepCount(newCount: Int) {
        _stepCount.value = newCount
        _caloriesBurned.value = calculateCalories(newCount)
    }

    private fun calculateCalories(steps: Int): Float {
        val calorieFactor = 0.04f // Adjust as needed
        return steps * calorieFactor
    }

}