package se.banksimulatorn.app.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
}
