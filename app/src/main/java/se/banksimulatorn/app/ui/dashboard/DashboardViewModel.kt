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
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import se.banksimulatorn.app.data.TransactionType

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
                bankDao.insertAccount(
                    Account(
                        id = 1,
                        name = "Private Account",
                        accountNumber = "9 9999-9999 0",
                        balance = 1000.0,
                        blockedAmount = 500.0,
                        type = AccountType.CHECKING
                    )
                )

                // Mock blocked transaction
                bankDao.insertTransaction(
                    Transaction(
                        accountId = 1,
                        amount = 250.0,
                        timestamp = System.currentTimeMillis(),
                        description = "Reserved",
                        merchant = "ICA",
                        status = TransactionStatus.BLOCKED,
                        cardNumber = "MC ***-5195",
                        type = TransactionType.WITHDRAWAL
                    )
                )

                // Mock latest transaction
                bankDao.insertTransaction(
                    Transaction(
                        accountId = 1,
                        amount = 1289.0,
                        timestamp = System.currentTimeMillis() - 86400000, // Yesterday
                        description = "Credit card purchase",
                        merchant = "H&M",
                        status = TransactionStatus.COMPLETED,
                        type = TransactionType.WITHDRAWAL
                    )
                )
            }
        }
    }
}
