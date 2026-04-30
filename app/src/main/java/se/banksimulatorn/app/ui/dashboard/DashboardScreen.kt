package se.banksimulatorn.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.AccountType
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAccountClick: (Int) -> Unit,
    onHistoryClick: () -> Unit,
    onNewTransactionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Banking Simulator",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Rounded.History, contentDescription = "History")
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
        val currentAccount = accounts.firstOrNull() // Simplify for design
        
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
                    "Accounts",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (currentAccount != null) {
                    BalanceCard(account = currentAccount)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onNewTransactionClick(currentAccount.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4B44F), // Gold/Yellowish
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("New transaction", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            // Real data would come from ViewModel, using mock logic for now to match UI
            item {
                Text(
                    "Blocked",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Filtered transactions for current account with status BLOCKED/PENDING
                TransactionItemDesign(
                    merchant = "ICA",
                    detail = "Reserved | MC ***-5195",
                    amount = -250.0,
                    isBlocked = true
                )
            }

            item {
                Text(
                    "Latest transactions",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TransactionItemDesign(
                    merchant = "H&M",
                    detail = "Credit card purchase",
                    date = "April 21st, 2026",
                    amount = -1289.0
                )
            }
        }
    }
}

@Composable
fun BalanceCard(account: Account) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY) // Looks like German format in image
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E4E1) // Light grey/greenish
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
                Text("Balance", style = MaterialTheme.typography.bodyLarge)
                Text(currencyFormatter.format(account.balance).replace("€", ""), style = MaterialTheme.typography.bodyLarge)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Blocked amount", style = MaterialTheme.typography.bodyLarge)
                Text("-" + currencyFormatter.format(account.blockedAmount).replace("€", ""), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFBA1A1A))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Available amount", style = MaterialTheme.typography.bodyLarge)
                val available = account.balance - account.blockedAmount
                Text(currencyFormatter.format(available).replace("€", ""), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF006C4C))
            }
        }
    }
}

@Composable
fun TransactionItemDesign(
    merchant: String,
    detail: String,
    date: String? = null,
    amount: Double,
    isBlocked: Boolean = false
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    
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
                Text(merchant, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Normal)
                Text(
                    currencyFormatter.format(amount).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (amount < 0) Color(0xFFBA1A1A) else Color(0xFF006C4C)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                if (date != null) {
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}
