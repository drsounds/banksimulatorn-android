package se.banksimulatorn.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import se.banksimulatorn.app.data.*

class DashboardViewModel(private val bankDao: BankDao) : ViewModel() {

    // Follows the database so it turns false as soon as onboarding has created the
    // initial accounts; a one-shot flag stayed true and reopened onboarding.
    val shouldOnboard: StateFlow<Boolean> = bankDao.getGlobalSettings()
        .map { it == null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

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
