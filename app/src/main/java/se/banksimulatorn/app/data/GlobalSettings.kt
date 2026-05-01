package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_settings")
data class GlobalSettings(
    @PrimaryKey val id: Int = 1,
    val currency: String = "EUR"
)
