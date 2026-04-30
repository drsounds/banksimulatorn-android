package se.banksimulatorn.app.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.CreditCard
import se.banksimulatorn.app.data.Transaction
import se.banksimulatorn.app.data.TransactionStatus
import se.banksimulatorn.app.data.TransactionType

class PurchaseViewModel(
    private val cardId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _creditCard = MutableStateFlow<CreditCard?>(null)
    val creditCard: StateFlow<CreditCard?> = _creditCard.asStateFlow()

    private val _uiEvent = MutableSharedFlow<PurchaseUiEvent>()
    val uiEvent: SharedFlow<PurchaseUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _creditCard.value = bankDao.getCreditCardById(cardId)
        }
    }

    fun charge(merchant: String, transactionName: String, amount: Double) {
        viewModelScope.launch {
            val card = _creditCard.value ?: return@launch
            if (amount <= 0) {
                _uiEvent.emit(PurchaseUiEvent.Error("Amount must be greater than zero"))
                return@launch
            }

            try {
                val updatedCard = card.copy(
                    usedCredit = card.usedCredit + amount,
                    pendingAuthorizations = card.pendingAuthorizations + amount
                )

                val transaction = Transaction(
                    accountId = -1, // Use -1 or similar for card transactions if not linked to a bank account directly
                    amount = -amount,
                    timestamp = System.currentTimeMillis(),
                    description = transactionName,
                    merchant = merchant,
                    status = TransactionStatus.BLOCKED,
                    cardNumber = card.cardNumber.takeLast(4).let { "***-$it" },
                    type = TransactionType.WITHDRAWAL
                )

                bankDao.performCreditTransaction(transaction, updatedCard)
                _uiEvent.emit(PurchaseUiEvent.Success("Charge successful"))
                _creditCard.value = updatedCard
            } catch (e: Exception) {
                _uiEvent.emit(PurchaseUiEvent.Error("Charge failed: ${e.message}"))
            }
        }
    }
}

sealed class PurchaseUiEvent {
    data class Success(val message: String) : PurchaseUiEvent()
    data class Error(val message: String) : PurchaseUiEvent()
}
