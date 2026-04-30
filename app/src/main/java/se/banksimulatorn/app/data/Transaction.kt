package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,
    val amount: Double,
    val timestamp: Long,
    val description: String,
    val type: TransactionType
)

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER
}
