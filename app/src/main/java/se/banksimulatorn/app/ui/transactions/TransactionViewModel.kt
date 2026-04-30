package se.banksimulatorn.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.Account
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionType

class TransactionViewModel(
    private val accountId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _allAccounts = MutableStateFlow<List<Account>>(emptyList())
    val allAccounts: StateFlow<List<Account>> = _allAccounts.asStateFlow()

    private val _uiEvent = MutableSharedFlow<TransactionUiEvent>()
    val uiEvent: SharedFlow<TransactionUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _account.value = bankDao.getAccountById(accountId)
            bankDao.getAllAccounts().collect {
                _allAccounts.value = it
            }
        }
    }

    fun performTransaction(type: TransactionType, amount: Double, description: String, targetAccountId: Int? = null) {
        viewModelScope.launch {
            val currentAccount = _account.value ?: return@launch
            
            if (amount <= 0) {
                _uiEvent.emit(TransactionUiEvent.Error("Amount must be greater than zero"))
                return@launch
            }

            when (type) {
                TransactionType.WITHDRAWAL, TransactionType.TRANSFER -> {
                    if (currentAccount.balance < amount) {
                        _uiEvent.emit(TransactionUiEvent.Error("Insufficient funds"))
                        return@launch
                    }
                }
                else -> {}
            }

            try {
                if (type == TransactionType.TRANSFER && targetAccountId != null) {
                    val targetAccount = bankDao.getAccountById(targetAccountId) ?: return@launch
                    
                    // Outgoing
                    bankDao.performTransaction(
                        Transaction(
                            accountId = accountId,
                            amount = -amount,
                            timestamp = System.currentTimeMillis(),
                            description = "Transfer to ${targetAccount.name}: $description",
                            type = TransactionType.TRANSFER
                        ),
                        currentAccount.copy(balance = currentAccount.balance - amount)
                    )

                    // Incoming
                    bankDao.performTransaction(
                        Transaction(
                            accountId = targetAccountId,
                            amount = amount,
                            timestamp = System.currentTimeMillis(),
                            description = "Transfer from ${currentAccount.name}: $description",
                            type = TransactionType.TRANSFER
                        ),
                        targetAccount.copy(balance = targetAccount.balance + amount)
                    )
                } else {
                    val finalAmount = if (type == TransactionType.WITHDRAWAL) -amount else amount
                    bankDao.performTransaction(
                        Transaction(
                            accountId = accountId,
                            amount = finalAmount,
                            timestamp = System.currentTimeMillis(),
                            description = description,
                            type = type
                        ),
                        currentAccount.copy(balance = currentAccount.balance + finalAmount)
                    )
                }
                
                _account.value = bankDao.getAccountById(accountId)
                _uiEvent.emit(TransactionUiEvent.Success("Transaction completed successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(TransactionUiEvent.Error("Transaction failed: ${e.message}"))
            }
        }
    }
}

sealed class TransactionUiEvent {
    data class Success(val message: String) : TransactionUiEvent()
    data class Error(val message: String) : TransactionUiEvent()
}
