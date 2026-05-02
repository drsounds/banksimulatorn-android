package se.banksimulatorn.app.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onCreateBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.budgetItems.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Budgeting") },
            actions = {
                IconButton(onClick = onCreateBudget) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Budget Item")
                }
            }
        )
        
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No budget items. Start planning your expenses!", color = Color.Gray)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text("${item.type.name} - ${item.frequency.name}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Amount: ${currencyFormatter.format(item.amount).replace("€", "")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { viewModel.deleteBudgetItem(item) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
