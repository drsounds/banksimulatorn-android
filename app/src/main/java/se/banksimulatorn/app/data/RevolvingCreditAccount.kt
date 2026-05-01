package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revolving_credit_accounts")
data class RevolvingCreditAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val creditLimit: Double,
    val usedCredit: Double = 0.0,
    val interestRate: Double,
    val pendingAuthorizations: Double = 0.0,
    val invoiceCycleDay: Int = 1,
    val isBnplMode: Boolean = false,
    val pendingInterest: Double = 0.0,
    val deletedAt: Long? = null,
    val closedAt: Long? = null
)
