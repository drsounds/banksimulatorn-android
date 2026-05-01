package se.banksimulatorn.app.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.AccountType
import se.banksimulatorn.app.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.transaction_history)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    AccountList(
                        accounts = accounts,
                        selectedAccountId = selectedAccountId,
                        onAccountClick = { id ->
                            viewModel.selectAccount(id)
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val selectedAccount = accounts.find { it.id == selectedAccountId }
                    Column {
                        if (navigator.canNavigateBack()) {
                            TopAppBar(
                                title = { 
                                    val title = selectedAccount?.let { acc ->
                                        if (acc.name == "Checking") stringResource(R.string.checking)
                                        else if (acc.name == "Savings") stringResource(R.string.savings)
                                        else acc.name
                                    } ?: stringResource(R.string.details)
                                    Text(title) 
                                },
                                navigationIcon = {
                                    IconButton(onClick = { 
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_to_list))
                                    }
                                }
                            )
                        }
                        if (selectedAccount != null) {
                            TransactionList(transactions = transactions)
                        } else {
                            EmptyDetailState()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AccountList(
    accounts: List<Account>,
    selectedAccountId: Int?,
    onAccountClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(accounts) { account ->
            val isSelected = account.id == selectedAccountId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAccountClick(account.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.secondaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.large
            ) {
                ListItem(
                    headlineContent = { 
                        val name = when (account.name) {
                            "Checking" -> stringResource(R.string.checking)
                            "Savings" -> stringResource(R.string.savings)
                            else -> account.name
                        }
                        Text(name, fontWeight = FontWeight.Bold) 
                    },
                    supportingContent = { 
                        val type = if (account.type == AccountType.CHECKING) stringResource(R.string.private_account)
                                   else stringResource(R.string.savings_account)
                        Text(type) 
                    },
                    trailingContent = { 
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                        Text(
                            currencyFormatter.format(account.balance).replace("€", ""),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                stringResource(R.string.no_transactions),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItemDesignDesign(transaction)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun TransactionItemDesignDesign(transaction: Transaction) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    ListItem(
        headlineContent = { Text(transaction.merchant ?: transaction.description) },
        supportingContent = { 
            Column {
                if (transaction.merchant != null) {
                    val desc = if (transaction.description == "Credit card purchase") stringResource(R.string.credit_card_purchase)
                               else if (transaction.description == "Reserved") stringResource(R.string.reserved)
                               else transaction.description
                    Text(desc)
                }
                Text(dateFormatter.format(Date(transaction.timestamp))) 
            }
        },
        trailingContent = {
            Text(
                text = (if (transaction.amount > 0) "+" else "") + currencyFormatter.format(transaction.amount).replace("€", ""),
                color = if (transaction.amount > 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}

@Composable
fun EmptyDetailState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            stringResource(R.string.select_account),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
