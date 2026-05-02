package se.banksimulatorn.app.ui.timemachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.*
import java.util.Calendar

class TimeMachineViewModel(private val bankDao: BankDao) : ViewModel() {

    val virtualCurrentTime: StateFlow<Long> = bankDao.getTimeSettings()
        .map { it?.virtualCurrentTime ?: System.currentTimeMillis() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = System.currentTimeMillis()
        )

    fun moveForwardOneDay() {
        viewModelScope.launch {
            val nextTime = virtualCurrentTime.value + 86400000
            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = nextTime))

            val calendar = Calendar.getInstance().apply { timeInMillis = nextTime }
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // 1. Account Interest
            val accounts = bankDao.getAllAccountsSync()
            accounts.forEach { account ->
                val rate = if (account.balance >= 0) account.positiveInterestRate else account.overdraftInterestRate
                val dailyInterest = account.balance * (rate / 100.0 / 365.0)
                val updatedAccount = account.copy(pendingInterest = account.pendingInterest + dailyInterest)
                if (dayOfMonth == account.interestCapitalizationDay) {
                    val totalToCapitalize = updatedAccount.pendingInterest
                    if (Math.abs(totalToCapitalize) >= 0.01) {
                        bankDao.performTransaction(
                            Transaction(
                                accountId = account.id,
                                amount = totalToCapitalize,
                                timestamp = nextTime,
                                description = "Interest Capitalization",
                                type = TransactionType.INTEREST,
                                status = TransactionStatus.COMPLETED
                            ),
                            updatedAccount.copy(balance = updatedAccount.balance + totalToCapitalize, pendingInterest = 0.0)
                        )
                    } else {
                        bankDao.updateAccount(updatedAccount.copy(pendingInterest = 0.0))
                    }
                } else {
                    bankDao.updateAccount(updatedAccount)
                }
            }

            // 2. Revolving Credit Invoicing & Statements
            val credits = bankDao.getAllRevolvingCreditsSync()
            credits.forEach { credit ->
                var dailyInterest = 0.0
                if (credit.interestRate > 0) {
                    if (credit.isBnplMode) {
                        val transactions = bankDao.getUnreconciledCreditTransactions(credit.id)
                        transactions.forEach { t ->
                            val debt = t.remainingDebt ?: Math.abs(t.amount)
                            dailyInterest += debt * (credit.interestRate / 100.0 / 365.0)
                        }
                    } else {
                        if (credit.usedCredit > 0) {
                            dailyInterest = credit.usedCredit * (credit.interestRate / 100.0 / 365.0)
                        }
                    }
                }
                var updatedCredit = credit.copy(pendingInterest = credit.pendingInterest + dailyInterest)

                if (dayOfMonth == credit.statementDay) {
                    val totalDebt = updatedCredit.usedCredit + updatedCredit.pendingInterest
                    if (totalDebt >= 0.01) {
                        val minPayment = Math.max(credit.minPaymentAmount, totalDebt * credit.minPaymentPercent / 100.0)
                        bankDao.insertInvoice(
                            Invoice(
                                parentId = credit.id,
                                parentType = "CREDIT",
                                amount = totalDebt,
                                minimumAmount = minPayment,
                                issuedDate = nextTime,
                                dueDate = nextTime + (credit.dueDays.toLong() * 86400000L),
                                status = InvoiceStatus.PENDING,
                                overdueInterestRate = credit.lateInterestRate,
                                reminderFee = credit.lateFee
                            )
                        )
                        updatedCredit = updatedCredit.copy(pendingInterest = 0.0)
                    }
                }

                if (dayOfMonth == credit.invoiceCycleDay && dayOfMonth != credit.statementDay) {
                     val totalToBill = updatedCredit.pendingInterest
                     if (totalToBill >= 0.01) {
                         bankDao.performRevolvingCreditTransaction(
                             Transaction(
                                 revolvingCreditAccountId = credit.id,
                                 amount = -totalToBill,
                                 timestamp = nextTime,
                                 description = "Monthly Interest",
                                 type = TransactionType.INTEREST,
                                 status = TransactionStatus.COMPLETED
                             ),
                             updatedCredit.copy(usedCredit = updatedCredit.usedCredit + totalToBill, pendingInterest = 0.0)
                         )
                     } else {
                         bankDao.updateRevolvingCredit(updatedCredit.copy(pendingInterest = 0.0))
                     }
                } else {
                    bankDao.updateRevolvingCredit(updatedCredit)
                }
            }

            // 3. Loan Invoicing
            val loans = bankDao.getAllLoansSync()
            loans.forEach { loan ->
                val dailyInterest = loan.balance * (0.05 / 100.0 / 365.0) 
                val updatedLoan = loan.copy(pendingInterest = loan.pendingInterest + dailyInterest)
                if (dayOfMonth == loan.invoiceCycleDay) {
                    val interestToCapitalize = updatedLoan.pendingInterest
                    val fee = loan.loanFee
                    if (interestToCapitalize > 0 || fee > 0) {
                        bankDao.insertTransaction(
                            Transaction(
                                amount = -(interestToCapitalize + fee),
                                timestamp = nextTime,
                                description = "Loan Invoice",
                                type = TransactionType.INTEREST,
                                status = TransactionStatus.COMPLETED
                            )
                        )
                        bankDao.updateLoan(updatedLoan.copy(balance = updatedLoan.balance + interestToCapitalize + fee, pendingInterest = 0.0))
                    }
                } else {
                    bankDao.updateLoan(updatedLoan)
                }
            }

            // 4. E-Invoicing Engine (Overdue & Collection)
            val allActiveInvoices = bankDao.getAllInvoices().first().filter { it.status != InvoiceStatus.PAID && it.deletedAt == null }
            allActiveInvoices.forEach { invoice ->
                var updatedInvoice = invoice
                if (invoice.status == InvoiceStatus.OVERDUE || invoice.status == InvoiceStatus.COLLECTION) {
                    val dailyLateInterest = updatedInvoice.amount * (invoice.overdueInterestRate / 100.0 / 365.0)
                    updatedInvoice = updatedInvoice.copy(amount = updatedInvoice.amount + dailyLateInterest)
                }
                if (nextTime > invoice.dueDate && (invoice.status == InvoiceStatus.PENDING || invoice.status == InvoiceStatus.REMINDER)) {
                    updatedInvoice = updatedInvoice.copy(status = InvoiceStatus.OVERDUE, amount = updatedInvoice.amount + invoice.reminderFee)
                    bankDao.insertInvoice(
                        Invoice(
                            parentId = invoice.parentId,
                            parentType = invoice.parentType,
                            amount = updatedInvoice.amount,
                            minimumAmount = invoice.minimumAmount,
                            issuedDate = nextTime,
                            dueDate = nextTime + (7L * 86400000L),
                            status = InvoiceStatus.REMINDER,
                            isReminder = true,
                            reminderFee = invoice.reminderFee,
                            overdueInterestRate = invoice.overdueInterestRate
                        )
                    )
                }
                if (invoice.status == InvoiceStatus.OVERDUE) {
                    val dueCal = Calendar.getInstance().apply { timeInMillis = invoice.dueDate }
                    if (calendar.get(Calendar.MONTH) != dueCal.get(Calendar.MONTH) || calendar.get(Calendar.YEAR) != dueCal.get(Calendar.YEAR)) {
                        if (invoice.parentType == "CREDIT") {
                            val credit = bankDao.getRevolvingCreditById(invoice.parentId)
                            if (credit != null) {
                                updatedInvoice = updatedInvoice.copy(status = InvoiceStatus.COLLECTION, amount = updatedInvoice.amount + credit.collectionFee)
                            }
                        }
                    }
                }
                if (updatedInvoice != invoice) bankDao.updateInvoice(updatedInvoice)
            }

            // 5. Recurring Engine (Legacy)
            val recurringTasks = bankDao.getAllRecurringTasksSync()
            recurringTasks.forEach { task ->
                if (shouldTriggerTask(task, nextTime)) {
                    if (task.type == RecurringType.INCOME && task.targetAccountId != null) {
                        val acc = bankDao.getAccountById(task.targetAccountId) ?: return@forEach
                        bankDao.performTransaction(
                            Transaction(
                                accountId = task.targetAccountId,
                                amount = task.amount,
                                timestamp = nextTime,
                                description = "Recurring Income: ${task.name}",
                                type = TransactionType.DEPOSIT,
                                status = TransactionStatus.COMPLETED
                            ),
                            acc.copy(balance = acc.balance + task.amount)
                        )
                    } else if (task.type == RecurringType.EXPENSE) {
                        bankDao.insertInvoice(
                            Invoice(
                                parentId = task.id,
                                parentType = "RECURRING",
                                amount = task.amount,
                                issuedDate = nextTime,
                                dueDate = nextTime + (14L * 86400000L),
                                status = InvoiceStatus.PENDING
                            )
                        )
                    }
                    bankDao.updateRecurringTask(task.copy(lastTriggeredDate = nextTime))
                }
            }

            // 6. Budget Engine (New)
            val budgetItems = bankDao.getAllBudgetItemsSync()
            budgetItems.forEach { item ->
                if (shouldTriggerBudget(item, nextTime)) {
                    if (item.type == BudgetType.INCOME && item.targetAccountId != null) {
                        val acc = bankDao.getAccountById(item.targetAccountId) ?: return@forEach
                        bankDao.performTransaction(
                            Transaction(
                                accountId = item.targetAccountId,
                                amount = item.amount,
                                timestamp = nextTime,
                                description = "Budget Income: ${item.name}",
                                type = TransactionType.DEPOSIT,
                                status = TransactionStatus.COMPLETED
                            ),
                            acc.copy(balance = acc.balance + item.amount)
                        )
                    } else if (item.type == BudgetType.EXPENSE) {
                        when (item.paymentMethod) {
                            PaymentMethod.DIRECT_DEBIT -> {
                                item.targetAccountId?.let { accId ->
                                    val acc = bankDao.getAccountById(accId) ?: return@let
                                    bankDao.performTransaction(
                                        Transaction(
                                            accountId = accId,
                                            amount = -item.amount,
                                            timestamp = nextTime,
                                            description = "Budget Expense (Direct Debit): ${item.name}",
                                            type = TransactionType.WITHDRAWAL,
                                            status = TransactionStatus.COMPLETED
                                        ),
                                        acc.copy(balance = acc.balance - item.amount)
                                    )
                                }
                            }
                            PaymentMethod.CREDIT_CARD -> {
                                item.linkedCreditAccountId?.let { creditId ->
                                    val credit = bankDao.getRevolvingCreditById(creditId) ?: return@let
                                    bankDao.performRevolvingCreditTransaction(
                                        Transaction(
                                            revolvingCreditAccountId = creditId,
                                            amount = -item.amount,
                                            timestamp = nextTime,
                                            description = "Budget Expense (Credit Card): ${item.name}",
                                            type = TransactionType.WITHDRAWAL,
                                            status = TransactionStatus.COMPLETED
                                        ),
                                        credit.copy(usedCredit = credit.usedCredit + item.amount)
                                    )
                                }
                            }
                            PaymentMethod.E_INVOICE -> {
                                bankDao.insertInvoice(
                                    Invoice(
                                        parentId = item.id,
                                        parentType = "BUDGET",
                                        amount = item.amount,
                                        issuedDate = nextTime,
                                        dueDate = nextTime + (14L * 86400000L),
                                        status = InvoiceStatus.PENDING
                                    )
                                )
                            }
                        }
                    }
                    bankDao.updateBudgetItem(item.copy(nextTriggerDate = calculateNextTrigger(item, nextTime)))
                }
            }

            // 7. Asset Growth/Depreciation
            val assets = bankDao.getAllAssets().first()
            assets.forEach { asset ->
                if (dayOfMonth == 1) { // Monthly update
                    val growth = asset.currentValue * (asset.monthlyGrowthRate / 100.0)
                    bankDao.updateAsset(asset.copy(currentValue = asset.currentValue + growth))
                }
            }

            // 8. Auto-charge Blocked transactions
            val blocked = bankDao.getBlockedTransactionsSync()
            blocked.forEach { t ->
                if (t.chargedAt != null && t.chargedAt <= nextTime) {
                    val revolvingId = t.revolvingCreditAccountId ?: return@forEach
                    bankDao.chargeBlockedTransaction(t.id, revolvingId, t.amount, t.chargedAt)
                }
            }
        }
    }

    private fun shouldTriggerTask(task: RecurringTask, currentTime: Long): Boolean {
        if (currentTime < task.startDate) return false
        if (task.endDate != null && currentTime > task.endDate) return false
        val lastTriggered = task.lastTriggeredDate ?: return true
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentTime }
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastTriggered }
        return when (task.frequency) {
            RecurringFrequency.DAILY -> currentCal.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR) || currentCal.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)
            RecurringFrequency.WEEKLY -> currentTime - lastTriggered >= 86400000 * 7
            RecurringFrequency.MONTHLY -> currentCal.get(Calendar.MONTH) != lastCal.get(Calendar.MONTH) || currentCal.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)
        }
    }

    private fun shouldTriggerBudget(item: BudgetItem, currentTime: Long): Boolean {
        if (currentTime < item.startDate) return false
        if (item.endDate != null && currentTime > item.endDate) return false
        val nextTrigger = item.nextTriggerDate ?: return true
        return currentTime >= nextTrigger
    }

    private fun calculateNextTrigger(item: BudgetItem, lastTrigger: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = lastTrigger }
        when (item.frequency) {
            BudgetFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            BudgetFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            BudgetFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun moveBackwardOneDay() {
        viewModelScope.launch {
            val prevTime = virtualCurrentTime.value - 86400000
            
            // Reversion logic for interest and settlement
            val futureInterest = bankDao.getFutureInterestTransactions(prevTime)
            futureInterest.forEach { t ->
                if (t.accountId != null) bankDao.updateAccountBalance(t.accountId, -t.amount)
                else if (t.revolvingCreditAccountId != null) bankDao.updateRevolvingCreditUsed(t.revolvingCreditAccountId, -Math.abs(t.amount))
                bankDao.softDeleteTransaction(t.id, System.currentTimeMillis())
            }

            val accounts = bankDao.getAllAccountsSync()
            accounts.forEach { account ->
                val rate = if (account.balance >= 0) account.positiveInterestRate else account.overdraftInterestRate
                val dailyInterest = account.balance * (rate / 100.0 / 365.0)
                bankDao.updateAccount(account.copy(pendingInterest = Math.max(0.0, account.pendingInterest - dailyInterest)))
            }

            val credits = bankDao.getAllRevolvingCreditsSync()
            credits.forEach { credit ->
                var dailyInterest = 0.0
                if (credit.interestRate > 0 && credit.usedCredit > 0) {
                    dailyInterest = credit.usedCredit * (credit.interestRate / 100.0 / 365.0)
                }
                bankDao.updateRevolvingCredit(credit.copy(pendingInterest = Math.max(0.0, credit.pendingInterest - dailyInterest)))
            }

            val futureCharged = bankDao.getFutureChargedTransactions(prevTime)
            futureCharged.forEach { t ->
                val revolvingId = t.revolvingCreditAccountId ?: return@forEach
                val absAmount = Math.abs(t.amount)
                bankDao.updateTransaction(t.copy(status = TransactionStatus.BLOCKED))
                val revolving = bankDao.getRevolvingCreditById(revolvingId) ?: return@forEach
                bankDao.updateRevolvingCredit(revolving.copy(
                    usedCredit = revolving.usedCredit - absAmount,
                    pendingAuthorizations = revolving.pendingAuthorizations + absAmount
                ))
            }

            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = prevTime))
        }
    }

    fun resetToNow() {
        viewModelScope.launch {
            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = System.currentTimeMillis()))
        }
    }
}
