package se.banksimulatorn.app.ui.timemachine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimeMachineBar(
    viewModel: TimeMachineViewModel,
    modifier: Modifier = Modifier
) {
    val virtualTime by viewModel.virtualCurrentTime.collectAsState()
    val calendar = Calendar.getInstance().apply { timeInMillis = virtualTime }
    
    val monthFormat = SimpleDateFormat("MMMM", Locale.US)
    val yearFormat = SimpleDateFormat("yyyy", Locale.US)
    
    val month = monthFormat.format(calendar.time)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = yearFormat.format(calendar.time)
    
    val suffix = when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.moveBackwardOneDay() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Prev Day", modifier = Modifier.size(32.dp))
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { viewModel.resetToNow() }
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Text(
                        text = month,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$day$suffix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = year,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }

                IconButton(onClick = { viewModel.moveForwardOneDay() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Day", modifier = Modifier.size(32.dp))
                }
            }
            // Linear progress indicator at the bottom matching image_9
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFFD4B44F),
                trackColor = Color.LightGray
            )
        }
    }
}
