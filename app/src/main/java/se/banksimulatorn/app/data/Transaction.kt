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
        ),
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int? = null,
    val creditCardId: Int? = null,
    val amount: Double,
    val timestamp: Long,
    val description: String,
    val merchant: String? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val cardNumber: String? = null,
    val type: TransactionType
)

enum class TransactionStatus {
    PENDING, COMPLETED, BLOCKED
}

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER
}
