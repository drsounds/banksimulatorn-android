package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_tasks")
data class RecurringTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: RecurringType,
    val frequency: RecurringFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val targetAccountId: Int? = null, // for incomes
    val billingAccountId: Int? = null, // for expenses
    val lastTriggeredDate: Long? = null,
    val deletedAt: Long? = null
)

enum class RecurringType {
    INCOME, EXPENSE
}

enum class RecurringFrequency {
    DAILY, WEEKLY, MONTHLY
}
