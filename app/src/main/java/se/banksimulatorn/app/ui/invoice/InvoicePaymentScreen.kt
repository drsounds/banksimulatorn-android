package se.banksimulatorn.app.ui.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Account
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePaymentScreen(
    viewModel: InvoicePaymentViewModel,
    onBack: () -> Unit
) {
    val invoice by viewModel.invoice.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is InvoiceUiEvent.Success) {
                onBack()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pay_invoice)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        invoice?.let { inv ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.invoice_details), style = MaterialTheme.typography.titleLarge)
                        DetailRow("Amount", currencyFormatter.format(inv.amount).replace("€", ""))
                        DetailRow(stringResource(R.string.minimum_amount), currencyFormatter.format(inv.minimumAmount).replace("€", ""))
                        DetailRow("Status", inv.status.name)
                    }
                }

                Text(stringResource(R.string.select_payment_account), style = MaterialTheme.typography.titleMedium)
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAccountId = acc.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedAccountId == acc.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(acc.name, fontWeight = FontWeight.Bold)
                                    Text(currencyFormatter.format(acc.balance).replace("€", ""), style = MaterialTheme.typography.bodySmall)
                                }
                                RadioButton(selected = selectedAccountId == acc.id, onClick = { selectedAccountId = acc.id })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { selectedAccountId?.let { viewModel.payInvoice(it, inv.amount) } },
                    enabled = selectedAccountId != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(stringResource(R.string.pay) + " " + currencyFormatter.format(inv.amount).replace("€", ""))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
