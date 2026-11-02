package se.banksimulatorn.app.ui.create

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/* Defaults filled in when a new product is opened from the "Open new" screen. */

/** Swedish-style clearing number + account number, e.g. "8327-9 123 456 789-0". */
internal fun generateAccountNumber(random: Random = Random.Default): String {
    fun digits(n: Int) = (1..n).joinToString("") { random.nextInt(10).toString() }
    return "8${digits(3)}-${digits(1)} ${digits(3)} ${digits(3)} ${digits(3)}-${digits(1)}"
}

internal fun generateCardNumber(random: Random = Random.Default): String {
    fun digits(n: Int) = (1..n).joinToString("") { random.nextInt(10).toString() }
    return "4242-${digits(4)}-${digits(4)}-${digits(4)}"
}

/**
 * The next date, from [now], that falls on [dayOfMonth], formatted as yyyy-MM-dd
 * (today counts if it is that day).
 */
internal fun nextDateOnDay(now: Long, dayOfMonth: Int): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = now }
    if (calendar.get(Calendar.DAY_OF_MONTH) > dayOfMonth) {
        calendar.add(Calendar.MONTH, 1)
    }
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}
