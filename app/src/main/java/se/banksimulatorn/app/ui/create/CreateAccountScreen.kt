package se.banksimulatorn.app.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("ACCOUNT") }
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is CreateUiEvent.Success) {
                onBack()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.create_new)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == "ACCOUNT",
                    onClick = { selectedType = "ACCOUNT" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text(stringResource(R.string.accounts)) }
                SegmentedButton(
                    selected = selectedType == "CREDIT",
                    onClick = { selectedType = "CREDIT" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text(stringResource(R.string.credits)) }
                SegmentedButton(
                    selected = selectedType == "LOAN",
                    onClick = { selectedType = "LOAN" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text(stringResource(R.string.loans)) }
            }

            when (selectedType) {
                "ACCOUNT" -> BankAccountForm(onSave = viewModel::createBankAccount)
                "CREDIT" -> RevolvingCreditForm(onSave = viewModel::createRevolvingCredit)
                "LOAN" -> LoanForm(onSave = viewModel::createLoan)
            }
        }
    }
}

@Composable
fun BankAccountForm(onSave: (String, String, Double, AccountType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CHECKING) }

    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Account Name") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Initial Balance") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        RadioButton(selected = type == AccountType.CHECKING, onClick = { type = AccountType.CHECKING })
        Text("Checking")
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = type == AccountType.SAVINGS, onClick = { type = AccountType.SAVINGS })
        Text("Savings")
    }

    Button(
        onClick = { onSave(name, number, balance.toDoubleOrNull() ?: 0.0, type) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text("Create Account")
    }
}

@Composable
fun RevolvingCreditForm(onSave: (String, Double, Double, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var cycleDay by remember { mutableStateOf("1") }

    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Credit Name") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Credit Limit") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Interest Rate (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    OutlinedTextField(value = cycleDay, onValueChange = { cycleDay = it }, label = { Text("Invoice Cycle Day") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

    Button(
        onClick = { onSave(name, limit.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0, cycleDay.toIntOrNull() ?: 1) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text("Create Credit Line")
    }
}

@Composable
fun LoanForm(onSave: (String, Double, Double, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var cycleDay by remember { mutableStateOf("1") }

    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Loan Name") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Loan Balance") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("Monthly Fee") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    OutlinedTextField(value = cycleDay, onValueChange = { cycleDay = it }, label = { Text("Invoice Cycle Day") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

    Button(
        onClick = { onSave(name, balance.toDoubleOrNull() ?: 0.0, fee.toDoubleOrNull() ?: 0.0, cycleDay.toIntOrNull() ?: 1) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text("Create Loan")
    }
}
