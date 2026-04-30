package se.banksimulatorn.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.AccountType
import se.banksimulatorn.app.data.BankDao

class DashboardViewModel(private val bankDao: BankDao) : ViewModel() {

    val accounts: StateFlow<List<Account>> = bankDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Seed initial data if empty
        viewModelScope.launch {
            if (bankDao.getAccountById(1) == null) {
                bankDao.insertAccount(Account(name = "Checking Account", balance = 2500.0, type = AccountType.CHECKING))
                bankDao.insertAccount(Account(name = "Savings Account", balance = 15000.0, type = AccountType.SAVINGS))
            }
        }
    }
}
