package se.banksimulatorn.app.ui.timemachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.TimeSettings
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import se.banksimulatorn.app.data.TransactionType
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
                            updatedAccount.copy(
                                balance = updatedAccount.balance + totalToCapitalize,
                                pendingInterest = 0.0
                            )
                        )
                    } else {
                        bankDao.updateAccount(updatedAccount.copy(pendingInterest = 0.0))
                    }
                } else {
                    bankDao.updateAccount(updatedAccount)
                }
            }

            // 2. Credit Card Invoicing
            val cards = bankDao.getAllCreditCardsSync()
            cards.forEach { card ->
                var dailyInterest = 0.0
                if (card.interestRate > 0) {
                    if (card.isBnplMode) {
                        val transactions = bankDao.getUnreconciledCreditTransactions(card.id)
                        transactions.forEach { t ->
                            val debt = t.remainingDebt ?: Math.abs(t.amount)
                            dailyInterest += debt * (card.interestRate / 100.0 / 365.0)
                        }
                    } else {
                        if (card.usedCredit > 0) {
                            dailyInterest = card.usedCredit * (card.interestRate / 100.0 / 365.0)
                        }
                    }
                }

                var updatedCard = card.copy(pendingInterest = card.pendingInterest + dailyInterest)

                if (dayOfMonth == card.invoiceCycleDay) {
                    val totalToBill = updatedCard.pendingInterest
                    if (totalToBill >= 0.01) {
                        bankDao.performCreditTransaction(
                            Transaction(
                                creditCardId = card.id,
                                amount = -totalToBill,
                                timestamp = nextTime,
                                description = "Monthly Interest",
                                type = TransactionType.INTEREST,
                                status = TransactionStatus.COMPLETED
                            ),
                            updatedCard.copy(
                                usedCredit = updatedCard.usedCredit + totalToBill,
                                pendingInterest = 0.0
                            )
                        )
                    } else {
                        bankDao.updateCreditCard(updatedCard.copy(pendingInterest = 0.0))
                    }
                } else {
                    bankDao.updateCreditCard(updatedCard)
                }
            }

            // 3. Loan Invoicing
            val loans = bankDao.getAllLoansSync()
            loans.forEach { loan ->
                val dailyInterest = loan.balance * (0.05 / 100.0 / 365.0) // Assume a generic rate if not specified, or use a default
                var updatedLoan = loan.copy(pendingInterest = loan.pendingInterest + dailyInterest)
                
                if (dayOfMonth == loan.invoiceCycleDay) {
                    // Apply interest to balance and add fee
                    val interestToCapitalize = updatedLoan.pendingInterest
                    val fee = loan.loanFee
                    
                    if (interestToCapitalize > 0 || fee > 0) {
                        bankDao.insertTransaction(
                            Transaction(
                                // Note: Loans don't have a dedicated accountId usually, but we track them via type or related entities
                                // For simplicity, we just log the transaction as INTEREST type
                                amount = -(interestToCapitalize + fee),
                                timestamp = nextTime,
                                description = "Loan Invoice (Interest: $interestToCapitalize, Fee: $fee)",
                                type = TransactionType.INTEREST,
                                status = TransactionStatus.COMPLETED
                            )
                        )
                        bankDao.updateLoan(updatedLoan.copy(
                            balance = updatedLoan.balance + interestToCapitalize + fee,
                            pendingInterest = 0.0
                        ))
                    }
                } else {
                    bankDao.updateLoan(updatedLoan)
                }
            }

            // 4. Auto-charge Blocked transactions
            val blocked = bankDao.getBlockedTransactionsSync()
            blocked.forEach { t ->
                if (t.chargedAt != null && t.chargedAt <= nextTime) {
                    val cardId = t.creditCardId ?: return@forEach
                    bankDao.chargeBlockedTransaction(t.id, cardId, t.amount, t.chargedAt)
                }
            }
        }
    }

    fun moveBackwardOneDay() {
        viewModelScope.launch {
            val prevTime = virtualCurrentTime.value - 86400000
            
            // Reverting forward actions is simplified by recalculating the daily delta
            // and checking for any capitalized transactions to delete.
            
            // 1. Revert Interest Transactions
            val futureInterest = bankDao.getFutureInterestTransactions(prevTime)
            futureInterest.forEach { t ->
                if (t.accountId != null) {
                    bankDao.updateAccountBalance(t.accountId, -t.amount)
                } else if (t.creditCardId != null) {
                    bankDao.updateCreditCardUsed(t.creditCardId, -Math.abs(t.amount))
                }
                bankDao.softDeleteTransaction(t.id, System.currentTimeMillis())
            }

            // 2. Revert Pending Interest accumulation (approximate)
            val accounts = bankDao.getAllAccountsSync()
            accounts.forEach { account ->
                val rate = if (account.balance >= 0) account.positiveInterestRate else account.overdraftInterestRate
                val dailyInterest = account.balance * (rate / 100.0 / 365.0)
                bankDao.updateAccount(account.copy(pendingInterest = Math.max(0.0, account.pendingInterest - dailyInterest)))
            }

            val cards = bankDao.getAllCreditCardsSync()
            cards.forEach { card ->
                var dailyInterest = 0.0
                if (card.interestRate > 0 && card.usedCredit > 0) {
                    dailyInterest = card.usedCredit * (card.interestRate / 100.0 / 365.0)
                }
                bankDao.updateCreditCard(card.copy(pendingInterest = Math.max(0.0, card.pendingInterest - dailyInterest)))
            }

            // 3. Revert Completed to Blocked
            val futureCharged = bankDao.getFutureChargedTransactions(prevTime)
            futureCharged.forEach { t ->
                val cardId = t.creditCardId ?: return@forEach
                bankDao.updateTransaction(t.copy(status = TransactionStatus.BLOCKED))
                bankDao.updateCreditCardPending(cardId, Math.abs(t.amount))
            }

            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = prevTime))
        }
    }

    fun resetToNow() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = now))
        }
    }
}
