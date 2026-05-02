package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_items")
data class BudgetItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: BudgetType,
    val frequency: BudgetFrequency,
    val dayOfMonth: Int = 1,
    val paymentMethod: PaymentMethod = PaymentMethod.DIRECT_DEBIT,
    val targetAccountId: Int? = null,
    val linkedCreditAccountId: Int? = null,
    val startDate: Long,
    val endDate: Long? = null,
    val nextTriggerDate: Long? = null,
    val deletedAt: Long? = null
)

enum class BudgetType {
    INCOME, EXPENSE
}

enum class BudgetFrequency {
    WEEKLY, MONTHLY, YEARLY
}

enum class PaymentMethod {
    DIRECT_DEBIT, CREDIT_CARD, E_INVOICE
}
