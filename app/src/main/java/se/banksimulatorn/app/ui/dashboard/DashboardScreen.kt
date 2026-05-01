package se.banksimulatorn.app.ui.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import se.banksimulatorn.app.data.AccountType
import se.banksimulatorn.app.data.CreditCard
import se.banksimulatorn.app.data.Loan
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
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
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val creditCards by viewModel.creditCards.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Rounded.Person, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp // Space for the floating button
            ),
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
                    onClick = { creditCards.firstOrNull()?.let { onNewPurchaseClick(it.id) } },
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

            val blockedTransactions = allTransactions.filter { it.status == TransactionStatus.BLOCKED || it.status == TransactionStatus.PENDING }
            if (blockedTransactions.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.blocked),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(blockedTransactions) { transaction ->
                    TransactionItemDesign(
                        merchant = transaction.merchant ?: transaction.description,
                        detail = if (transaction.status == TransactionStatus.BLOCKED) stringResource(R.string.reserved) + (transaction.cardNumber?.let { " | $it" } ?: "") else transaction.description,
                        amount = transaction.amount,
                        isBlocked = true
                    )
                }
            }

            val completedTransactions = allTransactions.filter { it.status == TransactionStatus.COMPLETED }
            if (completedTransactions.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.latest_transactions),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(completedTransactions) { transaction ->
                    val dateFormatter = SimpleDateFormat("MMMM d'th', yyyy", Locale.US)
                    TransactionItemDesign(
                        merchant = transaction.merchant ?: transaction.description,
                        detail = if (transaction.description == "Credit card purchase") stringResource(R.string.credit_card_purchase) else transaction.description,
                        date = dateFormatter.format(Date(transaction.timestamp)),
                        amount = transaction.amount
                    )
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
            items(creditCards) { card ->
                CreditCardItem(card = card, onClick = { onCreditClick(card.id) })
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
                            style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E4053)
                        )
                        Text(
                            currencyFormatter.format(account.balance - account.blockedAmount).replace("€", ""),
                            style = MaterialTheme.typography.headlineSmall,
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
fun CreditCardItem(card: CreditCard, onClick: () -> Unit) {
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
                Text(
                    text = if (card.name == "MasterCard") stringResource(R.string.mastercard)
                           else card.name,
                    style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E4053)
                )
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
                Text(card.cardNumber, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
