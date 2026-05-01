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

    fun charge(
        merchant: String,
        transactionName: String,
        amount: Double,
        authorizedAt: Long,
        chargedAt: Long?
    ) {
        viewModelScope.launch {
            val card = _creditCard.value ?: return@launch
            if (amount <= 0) {
                _uiEvent.emit(PurchaseUiEvent.ErrorRes(se.banksimulatorn.app.R.string.error_amount_zero))
                return@launch
            }

            try {
                val updatedCard = card.copy(
                    usedCredit = card.usedCredit + amount,
                    pendingAuthorizations = card.pendingAuthorizations + amount
                )

                val effectiveChargedAt = chargedAt ?: authorizedAt

                val transaction = Transaction(
                    creditCardId = card.id,
                    amount = -amount,
                    timestamp = effectiveChargedAt,
                    description = transactionName,
                    merchant = merchant,
                    status = TransactionStatus.BLOCKED,
                    cardNumber = card.cardNumber.takeLast(4).let { "***-$it" },
                    type = TransactionType.WITHDRAWAL,
                    authorizedAt = authorizedAt,
                    chargedAt = effectiveChargedAt
                )

                bankDao.performCreditTransaction(transaction, updatedCard)
                _uiEvent.emit(PurchaseUiEvent.SuccessRes(se.banksimulatorn.app.R.string.success_charge))
                _creditCard.value = updatedCard
            } catch (e: Exception) {
                _uiEvent.emit(PurchaseUiEvent.ErrorMsg(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class PurchaseUiEvent {
    data class SuccessRes(val resId: Int) : PurchaseUiEvent()
    data class ErrorRes(val resId: Int) : PurchaseUiEvent()
    data class ErrorMsg(val message: String) : PurchaseUiEvent()
}
