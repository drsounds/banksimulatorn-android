package se.banksimulatorn.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Transaction

class AccountDetailViewModel(
    private val accountId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    // Observed rather than loaded once, so the balance summary follows date changes,
    // payments and new transactions without leaving the screen.
    val account: StateFlow<Account?> = bankDao.observeAccountById(accountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions: StateFlow<List<Transaction>> = bankDao.getTransactionsForAccount(accountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
