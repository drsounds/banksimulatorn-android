package se.banksimulatorn.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Transaction

class AccountDetailViewModel(
    private val accountId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _account
        .flatMapLatest { acc ->
            if (acc == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else bankDao.getTransactionsForAccount(acc.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _account.value = bankDao.getAccountById(accountId)
        }
    }
}
