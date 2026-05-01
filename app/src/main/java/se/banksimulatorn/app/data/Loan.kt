package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val balance: Double,
    val pendingInterest: Double,
    val nextPaymentAmount: Double,
    val nextPaymentDate: String,
    val invoiceCycleDay: Int = 1,
    val loanFee: Double = 0.0,
    val deletedAt: Long? = null,
    val closedAt: Long? = null
)
