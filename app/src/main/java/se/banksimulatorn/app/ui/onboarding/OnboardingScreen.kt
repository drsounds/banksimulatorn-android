package se.banksimulatorn.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onSuccess: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val aiStatus by viewModel.aiStatusMessage.collectAsState()
    val isAiReady by viewModel.isAiModelReady.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                OnboardingUiEvent.Loading -> isLoading = true
                OnboardingUiEvent.Success -> {
                    isLoading = false
                    onSuccess()
                }
                is OnboardingUiEvent.Error -> {
                    isLoading = false
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.tell_me_life),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Gemini will generate your initial assets, accounts, and budget based on your story.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // --- AI Model Status Indicator ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAiReady) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isAiReady) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = Color(0xFF2E7D32))
                    }
                    Text(aiStatus, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Example Personas
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = { description = "30 year old nurse living in Stockholm. Has a small car and 5000 SEK in savings." },
                    label = { Text("Nurse") }
                )
                SuggestionChip(
                    onClick = { description = "22 year old student in Gothenburg. No car, living in a condo. Limited budget." },
                    label = { Text("Student") }
                )
                SuggestionChip(
                    onClick = { description = "45 year old architect in Malmö. Owns a villa, high income, large mortgage." },
                    label = { Text("Architect") }
                )
            }
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("E.g. I am a 30 year old nurse...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.generateLife(description) },
                enabled = description.isNotBlank() && !isLoading && isAiReady,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.generate_simulator))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { viewModel.skipOnboarding() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip AI and use default SEK data")
            }
        }
    }
}
