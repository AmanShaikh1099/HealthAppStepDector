package com.example.healthappstepdector.presentation.Exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.healthappstepdector.R
import kotlinx.coroutines.delay

@Composable
fun MotivationScreen(navController: NavController,userName: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
      // Display a suggestion to take a break
        TakeABreakSuggestion(navController,userName)
    }
}




@Composable
fun TakeABreakSuggestion(navController: NavController,userName: String) {
    val text = "It is nice to have a Break"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,


    ) {
        Icon(
            painter = painterResource(id = R.drawable.breaktime), // Replace with your big icon resource
            contentDescription = null, // Content description for accessibility
            modifier = Modifier.size(64.dp), // Adjust the size of the big icon as needed
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between the icon and text

        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Justify
        )
    }
    // Delay for 5 seconds before navigating to WelcomeScreen
    LaunchedEffect(true) {
        delay(5000)
        navController.popBackStack()
    }
}
