package se.banksimulatorn.app.ui.aichat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel,
    modifier: Modifier = Modifier
) {
    var eventText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AIChatUiEvent.Loading -> isLoading = true
                is AIChatUiEvent.Success -> {
                    isLoading = false
                    resultMessage = event.message
                    eventText = ""
                }
                is AIChatUiEvent.Error -> {
                    isLoading = false
                    resultMessage = event.message
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("AI Economic Simulator", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Describe an event (e.g. 'I got a 2000 SEK bonus' or 'Inflation rose by 2%') and Gemini will apply it to your world.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventText,
            onValueChange = { eventText = it },
            placeholder = { Text("What happened today?") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(onClick = { viewModel.simulateEvent(eventText) }, enabled = eventText.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Simulate")
                    }
                }
            }
        )

        if (resultMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(resultMessage!!, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
