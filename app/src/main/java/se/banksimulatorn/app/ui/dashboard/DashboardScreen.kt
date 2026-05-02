package se.banksimulatorn.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
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
import se.banksimulatorn.app.data.AccountType
import se.banksimulatorn.app.data.Loan
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import se.banksimulatorn.app.data.RevolvingCreditAccount
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAccountClick: (Int) -> Unit,
    onHistoryClick: () -> Unit,
    onNewTransactionClick: (Int) -> Unit,
    onNewPurchaseClick: (Int) -> Unit,
    onLoanClick: (Int) -> Unit,
    onCreditClick: (Int) -> Unit,
    onTransactionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val revolvingCredits by viewModel.revolvingCredits.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        LargeTopAppBar(
            title = {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            actions = {
                IconButton(onClick = onHistoryClick) {
                    Icon(Icons.Rounded.History, contentDescription = stringResource(R.string.transaction_history))
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.accounts),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                AccountsUnifiedCard(accounts = accounts, onAccountClick = onAccountClick)
            }

            item {
                Button(
                    onClick = { revolvingCredits.firstOrNull()?.let { onNewPurchaseClick(it.id) } },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4B44F),
                        contentColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.new_purchase))
                }
            }
            item {
                Text(
                    stringResource(R.string.loans),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(loans) { loan ->
                LoanCard(loan = loan, onClick = { onLoanClick(loan.id) })
            }

            item {
                Text(
                    stringResource(R.string.credits),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(revolvingCredits) { card ->
                RevolvingCreditItem(card = card, onClick = { onCreditClick(card.id) })
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { accounts.firstOrNull()?.let { onNewTransactionClick(it.id) } },
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
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AccountsUnifiedCard(accounts: List<Account>, onAccountClick: (Int) -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            accounts.forEachIndexed { index, account ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAccountClick(account.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (account.name) {
                                "Checking" -> stringResource(R.string.checking)
                                "Service" -> stringResource(R.string.service)
                                "Savings" -> stringResource(R.string.savings)
                                else -> account.name
                            },
                            style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E4053)
                        )
                        Text(
                            currencyFormatter.format(account.balance - account.blockedAmount).replace("€", ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (account.balance - account.blockedAmount > 0) Color(0xFF006C4C) else Color(0xFFBA1A1A)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (account.type == AccountType.CHECKING) stringResource(R.string.private_account) else stringResource(R.string.savings_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            currencyFormatter.format(account.balance).replace("€", ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                if (index < accounts.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun LoanCard(loan: Loan, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E6E1)), // Slightly pinkish/beige
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (loan.name == "Mortgage") stringResource(R.string.mortgage)
                           else loan.name,
                    style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E4053)
                )
                Text(
                    "-" + currencyFormatter.format(loan.balance).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFBA1A1A)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (loan.type == "Mortgage loan") stringResource(R.string.mortgage_loan)
                    else loan.type,
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                )
                Text(
                    stringResource(R.string.upcoming_payment) + ": " + currencyFormatter.format(loan.nextPaymentAmount).replace("€", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RevolvingCreditItem(card: RevolvingCreditAccount, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(card.name, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E4053))
                Text(
                    "-" + currencyFormatter.format(card.usedCredit).replace("€", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFBA1A1A)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.credits), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    currencyFormatter.format(card.creditLimit).replace("€", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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
    isBlocked: Boolean = false,
    onClick: () -> Unit = {}
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    
    Card(
        onClick = onClick,
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
