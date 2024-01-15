// SelectUserScreen.kt
package com.example.healthappstepdector.presentation.theme

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.healthappstepdector.presentation.DataClasses.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Composable
fun SelectUserScreen(navController: NavController, context: Context) {
    // Use remember to keep the state across recompositions
    val userListState: MutableState<List<UserData>> = remember { mutableStateOf(emptyList()) }

    // Uses LaunchedEffect to start a coroutine when the composable is first launched
    LaunchedEffect(true) {
        // Loads data asynchronously and update the state
        withContext(Dispatchers.IO) {
            val loadedUserNames = readUserNamesFromExcel(context)
            // Updates the state using remember
            userListState.value = loadedUserNames
        }
    }
    CustomDropdownMenu(userListState, navController)
}

@Composable
fun CustomDropdownMenu(userListState: State<List<UserData>>, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf(userListState.value.firstOrNull()?.username ?: "") }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { expanded = !expanded }) {
                Text(text = if (selectedUser.isNotEmpty()) selectedUser else "Select User")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.align(Alignment.End)
            ) {
                userListState.value.forEach { userData ->
                    DropdownMenuItem(onClick = {
                        selectedUser = userData.username
                        expanded = false
                        navController.navigate("welcome/${userData.username}")
                    }) {
                        Text(text = userData.username)
                    }
                }
            }
        }
    }
}
private suspend fun readUserNamesFromExcel(context: Context): List<UserData> {
    val userList = mutableListOf<UserData>()

    try {
        context.assets.open("UserData/user_data.csv.xlsx").use { inputStream ->
            val workbook: Workbook = XSSFWorkbook(inputStream)
            val sheet: Sheet = workbook.getSheet("Sheet1")

            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                val row: Row = sheet.getRow(rowIndex)

                val username = row.getCell(0)?.stringCellValue ?: ""
                val breaks = row.getCell(1)?.numericCellValue?.toInt() ?: 0
                val exercisesPerformed = row.getCell(2)?.stringCellValue ?: ""
                val lastExercisePerformed = row.getCell(3)?.stringCellValue ?: ""
                val healthStatus = row.getCell(4)?.stringCellValue ?: ""
                val lastLogin = row.getCell(5)?.stringCellValue ?: ""

                val userData = UserData(
                    username = username,
                    breaks = breaks,
                    exercisesPerformed = exercisesPerformed,
                    lastExercisePerformed = lastExercisePerformed,
                    healthStatus = healthStatus,
                    lastLogin = lastLogin
                )

                userList.add(userData)
            }

            workbook.close()
        }
    } catch (e: Exception) {
        Log.e("SelectUserScreen", "Error reading user data from Excel", e)
    }

    return userList
}