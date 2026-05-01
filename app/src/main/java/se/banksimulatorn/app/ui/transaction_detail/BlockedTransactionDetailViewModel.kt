package se.banksimulatorn.app.ui.transaction_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.R
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Transaction

class BlockedTransactionDetailViewModel(
    private val transactionId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BlockedUiEvent>()
    val uiEvent: SharedFlow<BlockedUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _transaction.value = bankDao.getTransactionById(transactionId)
        }
    }

    fun chargeNow() {
        viewModelScope.launch {
            val t = _transaction.value ?: return@launch
            val cardId = t.creditCardId ?: return@launch
            try {
                bankDao.chargeBlockedTransaction(t.id, cardId, t.amount)
                _uiEvent.emit(BlockedUiEvent.Success(R.string.success_charge))
                _uiEvent.emit(BlockedUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(BlockedUiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun releaseAmount() {
        viewModelScope.launch {
            val t = _transaction.value ?: return@launch
            val cardId = t.creditCardId ?: return@launch
            try {
                bankDao.releaseBlockedTransaction(t.id, cardId, t.amount)
                _uiEvent.emit(BlockedUiEvent.Success(R.string.success_release))
                _uiEvent.emit(BlockedUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(BlockedUiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class BlockedUiEvent {
    data class Success(val resId: Int) : BlockedUiEvent()
    data class Error(val message: String) : BlockedUiEvent()
    data object NavigateBack : BlockedUiEvent()
}
