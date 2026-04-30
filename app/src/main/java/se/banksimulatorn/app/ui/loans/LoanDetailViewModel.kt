package se.banksimulatorn.app.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Loan

class LoanDetailViewModel(
    private val loanId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _loan = MutableStateFlow<Loan?>(null)
    val loan: StateFlow<Loan?> = _loan.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _loan.value = bankDao.getLoanById(loanId)
        }
    }
}
