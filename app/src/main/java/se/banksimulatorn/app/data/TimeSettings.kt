package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_settings")
data class TimeSettings(
    @PrimaryKey val id: Int = 1,
    val virtualCurrentTime: Long
)
