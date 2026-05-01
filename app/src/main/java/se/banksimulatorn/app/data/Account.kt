package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val accountNumber: String = "9 9999-9999 0",
    val balance: Double,
    val blockedAmount: Double = 0.0,
    val type: AccountType,
    val interestRate: Double = 0.0,
    val positiveInterestRate: Double = 0.0,
    val overdraftInterestRate: Double = 0.0,
    val interestCapitalizationDay: Int = 1,
    val pendingInterest: Double = 0.0,
    val deletedAt: Long? = null,
    val closedAt: Long? = null
)

enum class AccountType {
    CHECKING, SAVINGS
}
