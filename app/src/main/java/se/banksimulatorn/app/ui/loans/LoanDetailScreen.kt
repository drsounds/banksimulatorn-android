package se.banksimulatorn.app.ui.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.formatMoney
import se.banksimulatorn.app.ui.components.VoluntaryPaymentDialog
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    viewModel: LoanDetailViewModel,
    onSettingsClick: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loan by viewModel.loan.collectAsState()
    val paymentAccounts by viewModel.paymentAccounts.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentError by remember { mutableStateOf<String?>(null) }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    LaunchedEffect(Unit) {
        viewModel.paymentEvents.collect { event ->
            when (event) {
                LoanPaymentEvent.Success -> {
                    showPaymentDialog = false
                    paymentAmount = ""
                }
                is LoanPaymentEvent.Error -> paymentError = event.message
            }
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.loan), style = MaterialTheme.typography.titleMedium)
                }
            },
            actions = {
                loan?.let { l ->
                    IconButton(onClick = { onSettingsClick(l.id) }) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.account_settings))
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        loan?.let { l ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E6E1)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("9 9999-9999 0", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    stringResource(if (l.balance < 0.0) R.string.credit_balance else R.string.balance),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    currencyFormatter.formatMoney(0.0 - l.balance).replace("€", ""),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (l.balance < 0.0) Color(0xFF006C4C) else Color(0xFFBA1A1A)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.pending_interest), style = MaterialTheme.typography.bodyLarge)
                                Text(currencyFormatter.formatMoney(0.0 - l.pendingInterest).replace("€", ""), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFBA1A1A))
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            selectedAccountId = null
                            paymentAmount = ""
                            paymentError = null
                            showPaymentDialog = true
                        },
                        enabled = l.balance + l.pendingInterest > 0.0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.voluntary_payment))
                    }
                }

                item {
                    Text(stringResource(R.string.upcoming_payment), style = MaterialTheme.typography.labelLarge)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1).copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.due_date, l.nextPaymentDate), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Box(modifier = Modifier.background(Color(0xFFD4B44F), MaterialTheme.shapes.extraSmall).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(stringResource(R.string.unpaid), style = MaterialTheme.typography.labelSmall, color = Color.Black)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("June 30th", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF2E4053))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(currencyFormatter.formatMoney(l.nextPaymentAmount).replace("€", ""), style = MaterialTheme.typography.headlineLarge, color = Color(0xFF2E4053))
                                    Text("6 550,00", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }

                    }
                }

                item {
                    Text(stringResource(R.string.latest_transactions), style = MaterialTheme.typography.labelLarge)
                }
                item {
                    LoanTransactionItem(
                        title = stringResource(R.string.installment),
                        subtitle = stringResource(R.string.payment),
                        date = "March 28th, 2026",
                        amount = 14250.0,
                        isPositive = true
                    )
                }
                item {
                    LoanTransactionItem(
                        title = stringResource(R.string.interest),
                        subtitle = stringResource(R.string.interest),
                        date = "March 28th, 2026",
                        amount = -6500.0,
                        isPositive = false
                    )
                }
            }
        }

        if (showPaymentDialog) {
            VoluntaryPaymentDialog(
                title = stringResource(R.string.voluntary_payment),
                accounts = paymentAccounts,
                selectedAccountId = selectedAccountId,
                amountText = paymentAmount,
                errorMessage = paymentError,
                currencyFormatter = currencyFormatter,
                onAccountSelected = { selectedAccountId = it },
                onAmountChanged = { paymentAmount = it },
                onDismiss = { showPaymentDialog = false },
                onConfirm = { accountId, amount -> viewModel.payVoluntarily(accountId, amount) }
            )
        }
    }
}

@Composable
fun LoanTransactionItem(
    title: String,
    subtitle: String,
    date: String,
    amount: Double,
    isPositive: Boolean
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) Color(0xFFE0E4E1) else Color(0xFFF2E6E1)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E4053))
                Text(
                    (if (isPositive) "+" else "") + currencyFormatter.formatMoney(amount).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isPositive) Color(0xFF006C4C) else Color(0xFFBA1A1A)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(date, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}
