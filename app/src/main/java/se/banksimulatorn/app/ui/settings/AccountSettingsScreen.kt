package se.banksimulatorn.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel,
    onBack: () -> Unit,
    onClosed: () -> Unit = onBack,
    modifier: Modifier = Modifier
) {
    val account by viewModel.account.collectAsState()
    val revolvingAccount by viewModel.revolvingAccount.collectAsState()
    val loan by viewModel.loan.collectAsState()
    
    var showCloseDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AccountSettingsUiEvent.Success ->
                    snackbarHostState.showSnackbar(context.getString(R.string.settings_saved))
                is AccountSettingsUiEvent.Closed -> onClosed()
                else -> {}
            }
        }
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text(stringResource(R.string.close_account)) },
            text = { Text(stringResource(R.string.close_account_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showCloseDialog = false
                    viewModel.close()
                }) { Text(stringResource(R.string.close_account)) }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.account_settings)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentName = account?.name ?: revolvingAccount?.name ?: loan?.name
                currentName?.let { name ->
                    var newName by remember(name) { mutableStateOf(name) }
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.rename(newName) },
                        enabled = newName.isNotBlank() && newName.trim() != name,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.rename)) }
                }

                account?.let { acc ->
                    AccountSettingsForm(
                        initialPositive = acc.positiveInterestRate,
                        initialOverdraft = acc.overdraftInterestRate,
                        initialCapDay = acc.interestCapitalizationDay,
                        initialOverdraftFee = acc.overdraftFee,
                        initialOverdraftFeeThreshold = acc.overdraftFeeThreshold,
                        initialOverdraftFeeCoolingDays = acc.overdraftFeeCoolingDays,
                        onSave = { pos, over, day, fee, threshold, cooling ->
                            viewModel.saveAccountSettings(pos, over, day, fee, threshold, cooling)
                        }
                    )
                }
                
                revolvingAccount?.let { card ->
                    CreditSettingsForm(
                        initialCycleDay = card.invoiceCycleDay,
                        initialBnpl = card.isBnplMode,
                        initialInterestRate = card.interestRate,
                        onSave = { day, bnpl, rate -> viewModel.saveCreditSettings(day, bnpl, rate) }
                    )
                }
                
                loan?.let { l ->
                    LoanSettingsForm(
                        initialCycleDay = l.invoiceCycleDay,
                        initialFee = l.loanFee,
                        onSave = { day, fee -> viewModel.saveLoanSettings(day, fee) }
                    )
                }

                if (account != null || revolvingAccount != null || loan != null) {
                    OutlinedButton(
                        onClick = { showCloseDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.close_account)) }
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun AccountSettingsForm(
    initialPositive: Double,
    initialOverdraft: Double,
    initialCapDay: Int,
    initialOverdraftFee: Double,
    initialOverdraftFeeThreshold: Double,
    initialOverdraftFeeCoolingDays: Int,
    onSave: (Double, Double, Int, Double, Double, Int) -> Unit
) {
    var positive by remember { mutableStateOf(initialPositive.toString()) }
    var overdraft by remember { mutableStateOf(initialOverdraft.toString()) }
    var capDay by remember { mutableStateOf(initialCapDay.toString()) }
    var overdraftFee by remember { mutableStateOf(initialOverdraftFee.toString()) }
    var overdraftThreshold by remember { mutableStateOf(initialOverdraftFeeThreshold.toString()) }
    var coolingDays by remember { mutableStateOf(initialOverdraftFeeCoolingDays.toString()) }

    OutlinedTextField(
        value = positive,
        onValueChange = { positive = it },
        label = { Text(stringResource(R.string.positive_interest_rate)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
        value = capDay,
        onValueChange = { capDay = it },
        label = { Text(stringResource(R.string.interest_capitalization_day)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    Text(stringResource(R.string.overdraft_section), style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = overdraft,
        onValueChange = { overdraft = it },
        label = { Text(stringResource(R.string.overdraft_interest_rate)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
        value = overdraftFee,
        onValueChange = { overdraftFee = it },
        label = { Text(stringResource(R.string.overdraft_fee)) },
        supportingText = { Text(stringResource(R.string.overdraft_fee_help)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
        value = overdraftThreshold,
        onValueChange = { overdraftThreshold = it },
        label = { Text(stringResource(R.string.overdraft_fee_threshold)) },
        supportingText = { Text(stringResource(R.string.overdraft_fee_threshold_help)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
        value = coolingDays,
        onValueChange = { coolingDays = it },
        label = { Text(stringResource(R.string.overdraft_fee_cooling_days)) },
        supportingText = { Text(stringResource(R.string.overdraft_fee_cooling_days_help)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Button(
        onClick = {
            onSave(
                positive.toDecimalOrNull() ?: 0.0,
                overdraft.toDecimalOrNull() ?: 0.0,
                capDay.trim().toIntOrNull() ?: 1,
                overdraftFee.toDecimalOrNull() ?: 0.0,
                overdraftThreshold.toDecimalOrNull() ?: 0.0,
                coolingDays.trim().toIntOrNull() ?: 0
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(stringResource(R.string.save_settings))
    }
}

/** Accepts both "1,5" and "1.5", as Swedish keyboards type a comma. */
private fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

@Composable
fun CreditSettingsForm(
    initialCycleDay: Int,
    initialBnpl: Boolean,
    initialInterestRate: Double,
    onSave: (Int, Boolean, Double) -> Unit
) {
    var cycleDay by remember { mutableStateOf(initialCycleDay.toString()) }
    var bnpl by remember { mutableStateOf(initialBnpl) }
    var interestRate by remember { mutableStateOf(initialInterestRate.toString()) }

    OutlinedTextField(
        value = cycleDay,
        onValueChange = { cycleDay = it },
        label = { Text(stringResource(R.string.invoice_cycle_day)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = interestRate,
        onValueChange = { interestRate = it },
        label = { Text(stringResource(R.string.interest_rate)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked = bnpl, onCheckedChange = { bnpl = it })
        Text(stringResource(R.string.bnpl_mode))
    }
    Button(
        onClick = { 
            onSave(
                cycleDay.toIntOrNull() ?: 1, 
                bnpl,
                interestRate.toDoubleOrNull() ?: 0.0
            ) 
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(stringResource(R.string.save_settings))
    }
}

@Composable
fun LoanSettingsForm(
    initialCycleDay: Int,
    initialFee: Double,
    onSave: (Int, Double) -> Unit
) {
    var cycleDay by remember { mutableStateOf(initialCycleDay.toString()) }
    var fee by remember { mutableStateOf(initialFee.toString()) }

    OutlinedTextField(
        value = cycleDay,
        onValueChange = { cycleDay = it },
        label = { Text(stringResource(R.string.invoice_cycle_day)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = fee,
        onValueChange = { fee = it },
        label = { Text(stringResource(R.string.loan_fee)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Button(
        onClick = { onSave(cycleDay.toIntOrNull() ?: 1, fee.toDoubleOrNull() ?: 0.0) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(stringResource(R.string.save_settings))
    }
}
