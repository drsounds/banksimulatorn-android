package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: AssetType,
    val initialValue: Double,
    val currentValue: Double,
    val monthlyGrowthRate: Double = 0.0, // Can be negative for depreciation
    val purchaseDate: Long,
    val deletedAt: Long? = null
)

enum class AssetType {
    VILLA, CONDO, CAR, OTHER
}
