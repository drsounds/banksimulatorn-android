package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_settings")
data class GlobalSettings(
    @PrimaryKey val id: Int = 1,
    val currency: String = "EUR",
    val country: String = "Sweden"
) {
    fun getCurrencyForCountry(country: String): String {
        return when (country) {
            "Sweden" -> "SEK"
            "Norway" -> "NOK"
            "Denmark" -> "DKK"
            "USA" -> "USD"
            "Finland", "Germany", "France" -> "EUR"
            else -> "EUR"
        }
    }
}
