// UserDetailsScreen.kt
package com.example.healthappstepdector.presentation.theme

// UserDetailsScreen.kt
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthappstepdector.presentation.DataClasses.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
/**
 * Composable function to display a screen for selecting a user.
 *
 * This screen fetches a list of users from an Excel file and displays them in a dropdown menu.
 * When a user is selected from the dropdown, the screen navigates to the welcome screen of the selected user.
 *
 * @param navController The NavController for managing app navigation.
 * @param context The Context where this screen is being displayed.
 */
@Composable
fun UserDetailsScreen(userName: String, context: Context) {
    val userDetailsState = remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(userName) {
        withContext(Dispatchers.IO) {
            val loadedUserData = readUserDetailsFromExcel(userName, context)
            userDetailsState.value = loadedUserData
        }
    }

    // Display user details
    userDetailsState.value?.let { UserData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "User: ${UserData.username}")
            Text(text = "Breaks: ${UserData.breaks}")
            Text(text = "Exercises Performed: ${UserData.exercisesPerformed}")
            Text(text = "Last Exercise: ${UserData.lastExercisePerformed}")
            Text(text = "Health Status: ${UserData.healthStatus}")
        }
    }
}
/**
 * Suspends execution to read user names from an Excel file and returns a list of UserData objects.
 *
 * This function opens an Excel file from the assets, reads user data row by row, and constructs a list of UserData objects.
 * It is designed to be called from a coroutine due to its potentially time-consuming file reading operations.
 *
 * @param context The Context used to access application assets.
 * @return A list of UserData objects representing users read from the Excel file.
 * @throws Exception if there's an error reading the Excel file.
 */

private suspend fun readUserDetailsFromExcel(userName: String, context: Context): UserData? {
    try {
        context.assets.open("UserData/user_data.csv.xlsx").use { inputStream ->
            val workbook: Workbook = XSSFWorkbook(inputStream)
            val sheet: Sheet = workbook.getSheet("Sheet1")

            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                val row: Row = sheet.getRow(rowIndex)

                if (row.getCell(0).stringCellValue == userName) {
                    return UserData(
                        username = row.getCell(0).stringCellValue,
                        breaks = row.getCell(1).numericCellValue.toInt(),
                        exercisesPerformed = row.getCell(2).stringCellValue,
                        lastExercisePerformed = row.getCell(3).stringCellValue,
                        healthStatus = row.getCell(4).stringCellValue,
                        lastLogin = row.getCell(5).stringCellValue
                    )
                }
            }

            workbook.close()
        }
    } catch (e: Exception) {
        Log.e("UserDetailsScreen", "Error reading user data from Excel", e)
    }

    return null
}