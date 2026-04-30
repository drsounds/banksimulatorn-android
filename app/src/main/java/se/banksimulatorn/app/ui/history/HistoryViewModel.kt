package se.banksimulatorn.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Transaction

class HistoryViewModel(private val bankDao: BankDao) : ViewModel() {

    val accounts: StateFlow<List<Account>> = bankDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedAccountId = MutableStateFlow<Int?>(null)
    val selectedAccountId: StateFlow<Int?> = _selectedAccountId

    val transactions: StateFlow<List<Transaction>> = _selectedAccountId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else bankDao.getTransactionsForAccount(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectAccount(accountId: Int?) {
        _selectedAccountId.value = accountId
    }
}
