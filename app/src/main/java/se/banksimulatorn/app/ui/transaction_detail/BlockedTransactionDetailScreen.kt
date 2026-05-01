package se.banksimulatorn.app.ui.transaction_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedTransactionDetailScreen(
    viewModel: BlockedTransactionDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction by viewModel.transaction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is BlockedUiEvent.Success -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                }
                is BlockedUiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is BlockedUiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.details)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            transaction?.let { t ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(t.merchant ?: t.description, style = MaterialTheme.typography.headlineMedium)
                                
                                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                                Text(
                                    currencyFormatter.format(t.amount).replace("€", ""),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = if (t.amount < 0) Color(0xFFBA1A1A) else Color(0xFF006C4C)
                                )
                                
                                HorizontalDivider()
                                
                                DetailRow(stringResource(R.string.transaction_id), t.id.toString())
                                DetailRow(stringResource(R.string.blocked), if (t.status == TransactionStatus.BLOCKED) stringResource(R.string.reserved) else t.status.name)
                                DetailRow(stringResource(R.string.date_authorized), t.authorizedAt?.let { dateFormatter.format(Date(it)) } ?: "-")
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.chargeNow() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4B44F), contentColor = Color.Black)
                            ) {
                                Text(stringResource(R.string.charge_amount_now))
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.releaseAmount() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A))
                            ) {
                                Text(stringResource(R.string.release_amount))
                            }
                        }
                    }
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
