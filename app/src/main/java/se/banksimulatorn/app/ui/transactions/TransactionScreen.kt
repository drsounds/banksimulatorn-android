package se.banksimulatorn.app.ui.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.TransactionType
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val account by viewModel.account.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var targetAccountId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TransactionUiEvent.SuccessRes -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                    amountText = ""
                    descriptionText = ""
                }
                is TransactionUiEvent.ErrorRes -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                }
                is TransactionUiEvent.ErrorMsg -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_transaction)) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account Summary
            account?.let {
                Column {
                    Text(
                        text = if (it.name == "Checking") stringResource(R.string.deposit) 
                               else if (it.name == "Savings") stringResource(R.string.savings_account)
                               else it.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.balance) + ": ${currencyFormatter.format(it.balance).replace("€", "")}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Transaction Type Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val types = listOf(
                    Triple(TransactionType.DEPOSIT, Icons.Rounded.Add, R.string.deposit),
                    Triple(TransactionType.WITHDRAWAL, Icons.Rounded.Remove, R.string.withdraw),
                    Triple(TransactionType.TRANSFER, Icons.Rounded.SwapHoriz, R.string.transfer)
                )
                types.forEachIndexed { index, triple ->
                    val (type, icon, stringRes) = triple
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = selectedType == type) {
                                Icon(icon, contentDescription = null)
                            }
                        }
                    ) {
                        Text(stringResource(stringRes))
                    }
                }
            }

            // Target Account Selector (for Transfers)
            AnimatedVisibility(
                visible = selectedType == TransactionType.TRANSFER,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filteredAccounts = allAccounts.filter { it.id != account?.id }
                    val selectedAccount = filteredAccounts.find { it.id == targetAccountId }

                    OutlinedTextField(
                        value = selectedAccount?.name ?: stringResource(R.string.select_account),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.transfer_to)) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    targetAccountId = acc.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) amountText = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$ ") },
                singleLine = true
            )

            // Description Field
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text(stringResource(R.string.transaction_name)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.whats_this_for)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    viewModel.performTransaction(
                        selectedType,
                        amount,
                        descriptionText,
                        targetAccountId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        TransactionType.DEPOSIT -> MaterialTheme.colorScheme.primary
                        TransactionType.WITHDRAWAL -> MaterialTheme.colorScheme.error
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                val typeString = stringResource(when (selectedType) {
                    TransactionType.DEPOSIT -> R.string.deposit
                    TransactionType.WITHDRAWAL -> R.string.withdraw
                    TransactionType.TRANSFER -> R.string.transfer
                    TransactionType.INTEREST -> R.string.interest
                    TransactionType.INSTALLMENT -> R.string.installment
                })
                Text(
                    text = stringResource(R.string.confirm_transaction, typeString),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
