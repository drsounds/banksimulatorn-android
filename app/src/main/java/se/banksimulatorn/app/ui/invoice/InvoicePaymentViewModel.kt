package se.banksimulatorn.app.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.*

class InvoicePaymentViewModel(
    private val invoiceId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _uiEvent = MutableSharedFlow<InvoiceUiEvent>()
    val uiEvent: SharedFlow<InvoiceUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _invoice.value = bankDao.getInvoiceById(invoiceId)
            bankDao.getAllAccounts().collect {
                _accounts.value = it
            }
        }
    }

    fun payInvoice(accountId: Int, amount: Double) {
        viewModelScope.launch {
            val inv = _invoice.value ?: return@launch
            try {
                // Book the payment on the simulated date, not the phone's clock.
                val now = bankDao.getTimeSettings().first()?.virtualCurrentTime ?: System.currentTimeMillis()
                bankDao.payInvoice(inv.id, accountId, amount, now)
                _invoice.value = bankDao.getInvoiceById(inv.id)
                _uiEvent.emit(InvoiceUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.emit(InvoiceUiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class InvoiceUiEvent {
    data object Success : InvoiceUiEvent()
    data class Error(val message: String) : InvoiceUiEvent()
}
