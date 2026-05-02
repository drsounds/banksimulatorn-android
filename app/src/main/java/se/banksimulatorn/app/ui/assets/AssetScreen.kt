package se.banksimulatorn.app.ui.assets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.HomeWork
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
fun AssetScreen(
    viewModel: AssetViewModel,
    onCreateAsset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Assets") },
            actions = {
                IconButton(onClick = onCreateAsset) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Asset")
                }
            }
        )
        
        if (assets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No assets yet. Add your house or car!", color = Color.Gray)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assets) { asset ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.HomeWork, contentDescription = null, modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text(asset.name, style = MaterialTheme.typography.titleMedium)
                            Text(asset.type.name, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Value: ${currencyFormatter.format(asset.currentValue).replace("€", "")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { viewModel.deleteAsset(asset) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
