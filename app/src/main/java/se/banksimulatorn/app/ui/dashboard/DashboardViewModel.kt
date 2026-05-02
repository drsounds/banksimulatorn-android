package se.banksimulatorn.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.*

class DashboardViewModel(private val bankDao: BankDao) : ViewModel() {

    private val _shouldOnboard = MutableStateFlow(false)
    val shouldOnboard: StateFlow<Boolean> = _shouldOnboard.asStateFlow()

    init {
        viewModelScope.launch {
            if (bankDao.hasGlobalSettings() == 0) {
                _shouldOnboard.value = true
            }
        }
    }

    val accounts: StateFlow<List<Account>> = bankDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val loans: StateFlow<List<Loan>> = bankDao.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val revolvingCredits: StateFlow<List<RevolvingCreditAccount>> = bankDao.getAllRevolvingCredits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<Transaction>> = bankDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val openInvoices: StateFlow<List<Invoice>> = bankDao.getOpenInvoices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
