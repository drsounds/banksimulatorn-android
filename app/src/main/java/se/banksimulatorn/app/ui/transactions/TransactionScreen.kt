package se.banksimulatorn.app.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by viewModel.account.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAccountId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TransactionUiEvent.SuccessRes -> {
                    snackbarHostState.showSnackbar(context.getString(event.resId))
                    amountText = ""
                    description = ""
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

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.new_transaction)) },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == TransactionType.DEPOSIT,
                        onClick = { selectedType = TransactionType.DEPOSIT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text(stringResource(R.string.deposit))
                    }
                    SegmentedButton(
                        selected = selectedType == TransactionType.WITHDRAWAL,
                        onClick = { selectedType = TransactionType.WITHDRAWAL },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text(stringResource(R.string.withdraw))
                    }
                    SegmentedButton(
                        selected = selectedType == TransactionType.TRANSFER,
                        onClick = { selectedType = TransactionType.TRANSFER },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text(stringResource(R.string.transfer))
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.whats_this_for)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedType == TransactionType.TRANSFER) {
                    Text(stringResource(R.string.target_account), style = MaterialTheme.typography.titleMedium)
                    allAccounts.filter { it.id != account?.id }.forEach { acc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetAccountId = acc.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = targetAccountId == acc.id, onClick = { targetAccountId = acc.id })
                            Text(acc.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        viewModel.performTransaction(selectedType, amount, description, targetAccountId)
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
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
