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
import se.banksimulatorn.app.data.CreditCard
import se.banksimulatorn.app.data.Loan
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

    val loans: StateFlow<List<Loan>> = bankDao.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val creditCards: StateFlow<List<CreditCard>> = bankDao.getAllCreditCards()
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

    init {
        // Seed initial data if empty
        viewModelScope.launch {
            if (bankDao.getAccountById(1) == null) {
                // Accounts
                bankDao.insertAccount(
                    Account(
                        id = 1,
                        name = "Checking",
                        accountNumber = "9 9999-9999 0",
                        balance = 1000.0,
                        blockedAmount = 500.0,
                        type = AccountType.CHECKING
                    )
                )
                bankDao.insertAccount(
                    Account(
                        id = 2,
                        name = "Service",
                        accountNumber = "9 8888-8888 1",
                        balance = 28842.0,
                        blockedAmount = 0.0,
                        type = AccountType.CHECKING
                    )
                )
                bankDao.insertAccount(
                    Account(
                        id = 3,
                        name = "Savings",
                        accountNumber = "9 7777-7777 2",
                        balance = 5842.0,
                        blockedAmount = 0.0,
                        type = AccountType.SAVINGS
                    )
                )

                // Loans
                bankDao.insertLoan(
                    Loan(
                        id = 1,
                        name = "Mortgage",
                        type = "Mortgage loan",
                        balance = 1526289.0,
                        pendingInterest = 6500.0,
                        nextPaymentAmount = 12526.0,
                        nextPaymentDate = "June 30th, 2026"
                    )
                )

                // Credit Cards
                bankDao.insertCreditCard(
                    CreditCard(
                        id = 1,
                        name = "MasterCard",
                        cardNumber = "XXXX-XXXX-XXXX-4242",
                        creditLimit = 10000.0,
                        usedCredit = 1499.0,
                        interestRate = 12.5,
                        pendingAuthorizations = 2500.0
                    )
                )

                // Transactions for History (Mock)
                bankDao.insertTransaction(
                    Transaction(
                        accountId = 1,
                        creditCardId = 1,
                        amount = -250.0,
                        timestamp = System.currentTimeMillis(),
                        description = "Reserved",
                        merchant = "ICA",
                        status = TransactionStatus.BLOCKED,
                        cardNumber = "MC ***-5195",
                        type = TransactionType.WITHDRAWAL
                    )
                )
                bankDao.insertTransaction(
                    Transaction(
                        accountId = 1,
                        creditCardId = 1,
                        amount = -1289.0,
                        timestamp = System.currentTimeMillis() - 86400000,
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
