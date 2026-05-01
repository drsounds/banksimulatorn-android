package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cardNumber: String,
    val creditLimit: Double,
    val usedCredit: Double,
    val interestRate: Double,
    val pendingAuthorizations: Double,
    val invoiceCycleDay: Int = 1,
    val isBnplMode: Boolean = false,
    val pendingInterest: Double = 0.0
)
