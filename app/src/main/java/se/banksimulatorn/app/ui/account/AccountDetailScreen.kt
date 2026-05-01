package se.banksimulatorn.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.rounded.Settings
import se.banksimulatorn.app.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onNewTransactionClick: (Int) -> Unit,
    onSettingsClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val account by viewModel.account.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    account?.let { acc ->
                        IconButton(onClick = { onSettingsClick(acc.id) }) {
                            Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.account_settings))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        account?.let { acc ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.accounts),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    AccountSummaryCard(account = acc)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onNewTransactionClick(acc.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4B44F),
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.new_transaction), modifier = Modifier.padding(start = 4.dp))
                    }
                }

                val blockedTransactions = transactions.filter { it.status == TransactionStatus.BLOCKED || it.status == TransactionStatus.PENDING }
                if (blockedTransactions.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.blocked),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(blockedTransactions) { transaction ->
                        AccountTransactionCard(transaction = transaction)
                    }
                }

                val completedTransactions = transactions.filter { it.status == TransactionStatus.COMPLETED }
                if (completedTransactions.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.latest_transactions),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(completedTransactions) { transaction ->
                        AccountTransactionCard(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSummaryCard(account: Account) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E4E1)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                account.accountNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.balance), style = MaterialTheme.typography.bodyLarge)
                Text(currencyFormatter.format(account.balance).replace("€", ""), style = MaterialTheme.typography.bodyLarge)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.blocked_amount), style = MaterialTheme.typography.bodyLarge)
                Text("-" + currencyFormatter.format(account.blockedAmount).replace("€", ""), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFBA1A1A))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.available_amount), style = MaterialTheme.typography.bodyLarge)
                val available = account.balance - account.blockedAmount
                Text(currencyFormatter.format(available).replace("€", ""), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF006C4C))
            }
        }
    }
}

@Composable
fun AccountTransactionCard(transaction: Transaction) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val dateFormatter = SimpleDateFormat("MMMM d'th', yyyy", Locale.US) // Matches "April 21st" loosely

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (transaction.merchant) {
                        "ICA" -> "ICA"
                        "H&M" -> "H&M"
                        else -> transaction.merchant ?: transaction.description
                    },
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Normal
                )
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
                val detail = if (transaction.status == TransactionStatus.BLOCKED || transaction.status == TransactionStatus.PENDING) {
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
