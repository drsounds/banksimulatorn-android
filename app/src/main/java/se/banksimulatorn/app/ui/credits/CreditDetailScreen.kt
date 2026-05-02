package se.banksimulatorn.app.ui.credits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditDetailScreen(
    viewModel: CreditDetailViewModel,
    onSimulatePurchase: (Int) -> Unit,
    onTransactionClick: (Int) -> Unit,
    onSettingsClick: (Int) -> Unit,
    onInvoiceClick: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card by viewModel.creditCard.collectAsState()
    val revolvingAccount by viewModel.revolvingAccount.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.credits), style = MaterialTheme.typography.labelMedium)
                    Text(card?.cardNumber ?: "", style = MaterialTheme.typography.titleMedium)
                }
            },
            actions = {
                revolvingAccount?.let { c ->
                    IconButton(onClick = { onSettingsClick(c.id) }) {
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

        revolvingAccount?.let { acc ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRow(stringResource(R.string.credit_limit), currencyFormatter.format(acc.creditLimit).replace("€", ""))
                            if (acc.usedCredit != 0.0) {
                               InfoRow(
                                   stringResource(R.string.used_credit),
                                   "-" + currencyFormatter.format(acc.usedCredit).replace("€", ""),
                                   color = Color(0xFFBA1A1A)
                               )
                            }
                            if (acc.pendingInterest != 0.0) {
                                InfoRow(
                                    stringResource(R.string.interest),
                                    currencyFormatter.format(-acc.pendingInterest).replace("€", "")
                                )
                            }
                            if (acc.pendingAuthorizations != 0.0) {
                                InfoRow(stringResource(R.string.pending_authorizations), "-" + currencyFormatter.format(acc.pendingAuthorizations).replace("€", ""), color = Color(0xFFB06000))
                            }
                            val available =
                                acc.creditLimit - acc.usedCredit - acc.pendingAuthorizations - acc.pendingInterest
                            InfoRow(stringResource(R.string.available_amount), currencyFormatter.format(available).replace("€", ""), fontWeight = FontWeight.Bold)

                        }
                    }
                }

                item {
                    Button(
                        onClick = { onSimulatePurchase(acc.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4B44F),
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.simulate_purchase))
                    }
                }

                if (invoices.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.invoices), style = MaterialTheme.typography.labelLarge)
                    }
                    items(invoices) { invoice ->
                        InvoiceDetailItem(invoice = invoice, onPay = { onInvoiceClick(invoice.id) })
                    }
                }

                val blockedTransactions = transactions.filter { it.status == TransactionStatus.BLOCKED || it.status == TransactionStatus.PENDING }
                if (blockedTransactions.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.blocked), style = MaterialTheme.typography.labelLarge)
                    }
                    items(blockedTransactions) { transaction ->
                        CreditTransactionItem(transaction, onClick = { onTransactionClick(transaction.id) })
                    }
                }

                val completedTransactions = transactions.filter { it.status == TransactionStatus.COMPLETED }
                if (completedTransactions.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.latest_transactions), style = MaterialTheme.typography.labelLarge)
                    }
                    items(completedTransactions) { transaction ->
                        CreditTransactionItem(transaction, onClick = { onTransactionClick(transaction.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailItem(
    invoice: se.banksimulatorn.app.data.Invoice,
    onPay: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (invoice.status) {
                se.banksimulatorn.app.data.InvoiceStatus.PAID -> Color(0xFFE0E4E1).copy(alpha = 0.5f)
                se.banksimulatorn.app.data.InvoiceStatus.OVERDUE, se.banksimulatorn.app.data.InvoiceStatus.COLLECTION -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.due_date, dateFormatter.format(Date(invoice.dueDate))),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = currencyFormatter.format(invoice.amount).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (invoice.status == se.banksimulatorn.app.data.InvoiceStatus.PAID) Color.Gray else Color.Unspecified
                )
                Text(
                    text = invoice.status.name,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (invoice.status != se.banksimulatorn.app.data.InvoiceStatus.PAID) {
                Button(onClick = onPay) {
                    Text(stringResource(R.string.pay))
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = color, fontWeight = fontWeight)
    }
}

@Composable
fun CreditTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val dateFormatter = SimpleDateFormat("MMMM d'th', yyyy", Locale.US)
    val isBlocked = transaction.status == TransactionStatus.BLOCKED || transaction.status == TransactionStatus.PENDING

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) Color(0xFFF2E6E1) else Color(0xFFF2D9D9).copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(transaction.merchant ?: transaction.description, style = MaterialTheme.typography.headlineSmall)
                Text(
                    currencyFormatter.format(transaction.amount).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (transaction.amount < 0) Color(0xFFBA1A1A) else Color(0xFF006C4C)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val detail = if (isBlocked) {
                    stringResource(R.string.reserved) + (transaction.cardNumber?.let { " | $it" } ?: "")
                } else {
                    if (transaction.description == "Credit card purchase") stringResource(R.string.credit_card_purchase)
                    else transaction.description
                }
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                if (transaction.status == TransactionStatus.COMPLETED) {
                    Text(dateFormatter.format(Date(transaction.timestamp)), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}
