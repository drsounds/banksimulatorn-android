package se.banksimulatorn.app.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Loan

class LoanDetailViewModel(
    private val loanId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    // Observed so the summary updates when the time machine accrues interest.
    val loan: StateFlow<Loan?> = bankDao.observeLoanById(loanId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val paymentAccounts: StateFlow<List<Account>> = bankDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _paymentEvents = MutableSharedFlow<LoanPaymentEvent>()
    val paymentEvents = _paymentEvents.asSharedFlow()

    fun payVoluntarily(accountId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val timestamp = bankDao.getTimeSettings().first()?.virtualCurrentTime
                    ?: System.currentTimeMillis()
                bankDao.payLoanVoluntarily(loanId, accountId, amount, timestamp)
                _paymentEvents.emit(LoanPaymentEvent.Success)
            } catch (e: Exception) {
                _paymentEvents.emit(LoanPaymentEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class LoanPaymentEvent {
    data object Success : LoanPaymentEvent()
    data class Error(val message: String) : LoanPaymentEvent()
}
