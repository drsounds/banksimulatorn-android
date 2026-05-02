package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parentId: Int, // Link to Account/Credit/Loan/Recurring
    val parentType: String, // "ACCOUNT", "CREDIT", "LOAN", "RECURRING"
    val amount: Double,
    val paidAmount: Double = 0.0,
    val minimumAmount: Double = 0.0,
    val dueDate: Long,
    val issuedDate: Long,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val reminderFee: Double = 0.0,
    val overdueInterestRate: Double = 0.0,
    val isReminder: Boolean = false,
    val deletedAt: Long? = null
)

enum class InvoiceStatus {
    PENDING, PAID, OVERDUE, REMINDER, COLLECTION
}
