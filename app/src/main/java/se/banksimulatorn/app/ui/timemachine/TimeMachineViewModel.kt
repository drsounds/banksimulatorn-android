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

            // Interest for Accounts
            val accounts = bankDao.getAllAccountsSync()
            accounts.forEach { account ->
                if (account.interestRate > 0) {
                    val dailyInterest = account.balance * (account.interestRate / 100.0 / 365.0)
                    if (dailyInterest != 0.0) {
                        bankDao.performTransaction(
                            Transaction(
                                accountId = account.id,
                                amount = dailyInterest,
                                timestamp = nextTime,
                                description = "Daily Interest",
                                type = TransactionType.INTEREST
                            ),
                            account.copy(balance = account.balance + dailyInterest)
                        )
                    }
                }
            }

            // Interest for Credit Cards
            val cards = bankDao.getAllCreditCardsSync()
            cards.forEach { card ->
                if (card.interestRate > 0 && card.usedCredit > 0) {
                    val dailyInterest = card.usedCredit * (card.interestRate / 100.0 / 365.0)
                    if (dailyInterest != 0.0) {
                        bankDao.performCreditTransaction(
                            Transaction(
                                creditCardId = card.id,
                                amount = -dailyInterest,
                                timestamp = nextTime,
                                description = "Daily Interest",
                                type = TransactionType.INTEREST
                            ),
                            card.copy(usedCredit = card.usedCredit + dailyInterest)
                        )
                    }
                }
            }

            // Auto-charge Blocked transactions
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
            val currentTime = virtualCurrentTime.value
            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = prevTime))

            // Revert Interest
            val futureInterest = bankDao.getFutureInterestTransactions(prevTime)
            futureInterest.forEach { t ->
                if (t.accountId != null) {
                    bankDao.updateAccountBalance(t.accountId, -t.amount)
                } else if (t.creditCardId != null) {
                    bankDao.updateCreditCardUsed(t.creditCardId, -Math.abs(t.amount)) // amount is negative for card interest
                }
                bankDao.softDeleteTransaction(t.id, System.currentTimeMillis())
            }

            // Revert Completed to Blocked
            val futureCharged = bankDao.getFutureChargedTransactions(prevTime)
            futureCharged.forEach { t ->
                val cardId = t.creditCardId ?: return@forEach
                bankDao.updateTransaction(t.copy(status = TransactionStatus.BLOCKED))
                bankDao.updateCreditCardPending(cardId, Math.abs(t.amount))
            }
        }
    }

    fun resetToNow() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            bankDao.updateTimeSettings(TimeSettings(virtualCurrentTime = now))
        }
    }
}
