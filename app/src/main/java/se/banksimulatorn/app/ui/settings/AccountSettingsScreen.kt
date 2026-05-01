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
    modifier: Modifier = Modifier
) {
    val account by viewModel.account.collectAsState()
    val revolvingAccount by viewModel.revolvingAccount.collectAsState()
    val loan by viewModel.loan.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is AccountSettingsUiEvent.Success) {
                snackbarHostState.showSnackbar(context.getString(R.string.settings_saved))
            }
        }
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
                account?.let { acc ->
                    AccountSettingsForm(
                        initialPositive = acc.positiveInterestRate,
                        initialOverdraft = acc.overdraftInterestRate,
                        initialCapDay = acc.interestCapitalizationDay,
                        onSave = { pos, over, day -> viewModel.saveAccountSettings(pos, over, day) }
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
    onSave: (Double, Double, Int) -> Unit
) {
    var positive by remember { mutableStateOf(initialPositive.toString()) }
    var overdraft by remember { mutableStateOf(initialOverdraft.toString()) }
    var capDay by remember { mutableStateOf(initialCapDay.toString()) }

    OutlinedTextField(
        value = positive,
        onValueChange = { positive = it },
        label = { Text(stringResource(R.string.positive_interest_rate)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
        value = overdraft,
        onValueChange = { overdraft = it },
        label = { Text(stringResource(R.string.overdraft_interest_rate)) },
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
    Button(
        onClick = { 
            onSave(
                positive.toDoubleOrNull() ?: 0.0,
                overdraft.toDoubleOrNull() ?: 0.0,
                capDay.toIntOrNull() ?: 1
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(stringResource(R.string.save_settings))
    }
}

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
