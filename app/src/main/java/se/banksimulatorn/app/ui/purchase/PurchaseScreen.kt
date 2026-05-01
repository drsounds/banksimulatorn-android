package se.banksimulatorn.app.ui.purchase

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel,
    onBack: () -> Unit
) {
    val card by viewModel.creditCard.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    var merchant by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    
    var authorizedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var chargedAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PurchaseUiEvent.SuccessRes -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                    merchant = ""
                    transactionName = ""
                    amountText = ""
                }
                is PurchaseUiEvent.ErrorRes -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                }
                is PurchaseUiEvent.ErrorMsg -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.labelSmall) },
                actions = {
                    Button(
                        onClick = {
                            val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            viewModel.charge(merchant, transactionName, amount, authorizedAt, chargedAt)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4B44F),
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.charge), modifier = Modifier.padding(start = 4.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.make_simulated_charge),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("SimulatedCard", style = MaterialTheme.typography.titleLarge)
                    Text("4242-4242-4242-4242", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.merchant), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = { Text(stringResource(R.string.eg_ica_ab), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.transaction_name), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    placeholder = { Text(stringResource(R.string.eg_ica_ab), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.amount), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    suffix = { Text("kr", style = MaterialTheme.typography.titleLarge) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.End)
                )
            }

            // Date Authorized
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.date_authorized), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = dateFormatter.format(Date(authorizedAt)),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context, authorizedAt) { authorizedAt = it }
                        },
                    enabled = false,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.clickable {
                            showDatePicker(context, authorizedAt) { authorizedAt = it }
                        })
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.Black,
                        disabledTextColor = Color.Black,
                        disabledTrailingIconColor = Color.Black
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.End)
                )
            }

            // Date Charged
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.date_charged), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = chargedAt?.let { dateFormatter.format(Date(it)) } ?: "YYY-MM-DD",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context, chargedAt ?: authorizedAt) { chargedAt = it }
                        },
                    enabled = false,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.clickable {
                            showDatePicker(context, chargedAt ?: authorizedAt) { chargedAt = it }
                        })
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.Black,
                        disabledTextColor = if (chargedAt == null) Color.Gray else Color.Black,
                        disabledTrailingIconColor = Color.Black
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.End)
                )
            }
        }
    }
}

private fun showDatePicker(context: android.content.Context, initialTimestamp: Long, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimestamp }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
