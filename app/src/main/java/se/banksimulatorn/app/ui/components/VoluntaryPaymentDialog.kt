package se.banksimulatorn.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.formatMoney
import se.banksimulatorn.app.ui.create.CreateFormValidation
import java.text.NumberFormat

@Composable
fun VoluntaryPaymentDialog(
    title: String,
    accounts: List<Account>,
    selectedAccountId: Int?,
    amountText: String,
    errorMessage: String?,
    currencyFormatter: NumberFormat,
    onAccountSelected: (Int) -> Unit,
    onAmountChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    val amount = CreateFormValidation.parseAmount(amountText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChanged,
                    label = { Text(stringResource(R.string.payment_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text(currencyFormatter.currency?.currencyCode ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.select_payment_account))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(accounts) { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAccountSelected(account.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAccountId == account.id,
                                onClick = { onAccountSelected(account.id) }
                            )
                            Column {
                                Text(account.name)
                                Text(
                                    currencyFormatter.formatMoney(account.balance),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val accountId = selectedAccountId
                    if (accountId != null && amount != null && amount > 0.0) {
                        onConfirm(accountId, amount)
                    }
                },
                enabled = selectedAccountId != null && amount != null && amount > 0.0
            ) {
                Text(stringResource(R.string.pay))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        }
    )
}
