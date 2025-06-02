package com.example.g355llm

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "start") {
        composable("start") { StartScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("qa") { PromptScreen(task = "qa") }
        composable("calendar") { PromptScreen(task = "calendar") }
    }
}

@Composable
fun StartScreen(navController: NavHostController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            try {
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val startupScript = File(downloads, "start_llm.sh")
                if (startupScript.exists()) {
                    Runtime.getRuntime().exec(arrayOf("sh", startupScript.absolutePath))
                    Toast.makeText(context, "✅ Servers starting...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "❌ start_llm.sh not found in Downloads", Toast.LENGTH_LONG).show()
                }
                navController.navigate("main")
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }) {
            Text("Start Server")
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Offline AI Assistant", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current

        Button(
            onClick = {
                val urlIntent =
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("http://127.0.0.1:8081")
                    }
                context.startActivity(urlIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Question Answering")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("calendar") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calendar Tasks")
        }
    }
}

@Composable
fun PromptScreen(task: String) {
    var userPrompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Prompt will be saved to file") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (task == "qa") "Question Answering" else "Calendar Tasks",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = userPrompt,
            onValueChange = { userPrompt = it },
            label = { Text("Enter your prompt") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                try {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val promptFile = File(downloadsDir, "prompt.txt")

                    val finalPrompt = if (task == "calendar") {
                        """
                        Convert the following text into JSON format with keys: "summary", "description", "start", "end", and "location".
                        Make sure "start" and "end" contain "dateTime" in ISO format and "timeZone": "Asia/Kolkata".

                        Text: "$userPrompt"

                        Respond only with the JSON.
                        """.trimIndent()
                    } else {
                        userPrompt
                    }

                    promptFile.writeText(finalPrompt)
                    response = "✅ Prompt saved to: ${promptFile.absolutePath}"
                } catch (e: Exception) {
                    response = "❌ Failed to save prompt: ${e.message}"
                }
            }
        ) {
            Text("Send")
        }

        Text(
            text = response,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
