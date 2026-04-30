package se.banksimulatorn.app.ui.credits

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.banksimulatorn.app.data.CreditCard
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.ui.res.stringResource
import se.banksimulatorn.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditDetailScreen(
    viewModel: CreditDetailViewModel,
    onSimulatePurchase: (Int) -> Unit,
    onBack: () -> Unit
) {
    val card by viewModel.creditCard.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.credit_card), style = MaterialTheme.typography.labelMedium)
                        Text(card?.cardNumber ?: "", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        card?.let { card ->
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E4E1)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoRow(stringResource(R.string.credit_limit), currencyFormatter.format(card.creditLimit).replace("€", ""))
                            InfoRow(stringResource(R.string.used_credit), "-" + currencyFormatter.format(card.usedCredit).replace("€", ""), color = Color(0xFFBA1A1A))
                            InfoRow(stringResource(R.string.interest_rate, "12,5"), "-120,00") 
                            InfoRow(stringResource(R.string.pending_authorizations), "-" + currencyFormatter.format(card.pendingAuthorizations).replace("€", ""), color = Color(0xFFB06000))
                            
                            val available = card.creditLimit - card.usedCredit - card.pendingAuthorizations - 120.0
                            InfoRow(stringResource(R.string.available_amount), currencyFormatter.format(available).replace("€", ""), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { onSimulatePurchase(card.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4B44F),
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.simulate_purchase))
                    }
                }

                item {
                    Text(stringResource(R.string.blocked), style = MaterialTheme.typography.labelLarge)
                }
                item {
                    CreditTransactionItem(
                        merchant = "Store",
                        detail = stringResource(R.string.reserved) + " | MC ***-4242",
                        amount = -2500.0,
                        isBlocked = true
                    )
                }

                item {
                    Text(stringResource(R.string.latest_transactions), style = MaterialTheme.typography.labelLarge)
                }
                item {
                    CreditTransactionItem(
                        merchant = "H&M",
                        detail = stringResource(R.string.credit_card_purchase),
                        date = "April 21st, 2026",
                        amount = -2500.0
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = color, fontWeight = fontWeight)
    }
}

@Composable
fun CreditTransactionItem(
    merchant: String,
    detail: String,
    date: String? = null,
    amount: Double,
    isBlocked: Boolean = false
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) Color(0xFFF2E6E1) else Color(0xFFF2D9D9).copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(merchant, style = MaterialTheme.typography.headlineSmall)
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
