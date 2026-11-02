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
    /** Fee charged when the balance drops below -[overdraftFeeThreshold]. 0 disables it. */
    val overdraftFee: Double = 0.0,
    /** How far below zero the balance may go before an overdraft fee is charged. */
    val overdraftFeeThreshold: Double = 0.0,
    /** Minimum number of days between two overdraft fees (cooling period). */
    val overdraftFeeCoolingDays: Int = 30,
    /** Virtual time of the last overdraft fee, used for the cooling period. */
    val lastOverdraftFeeAt: Long? = null,
    val deletedAt: Long? = null,
    val closedAt: Long? = null
)

enum class AccountType {
    CHECKING, SAVINGS
}
