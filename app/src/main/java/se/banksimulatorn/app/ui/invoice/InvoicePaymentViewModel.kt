package se.banksimulatorn.app.ui.invoice

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
            val acc = bankDao.getAccountById(accountId) ?: return@launch

            if (acc.balance < amount) {
                _uiEvent.emit(InvoiceUiEvent.Error("Insufficient funds on selected account"))
                return@launch
            }

            try {
                // 1. Deduct from bank account
                bankDao.performTransaction(
                    Transaction(
                        accountId = accountId,
                        amount = -amount,
                        timestamp = System.currentTimeMillis(),
                        description = "Payment of invoice #${inv.id}",
                        type = TransactionType.WITHDRAWAL,
                        status = TransactionStatus.COMPLETED
                    ),
                    acc.copy(balance = acc.balance - amount)
                )

                // 2. Reduce debt if it's a CREDIT invoice
                if (inv.parentType == "CREDIT") {
                    val revolving = bankDao.getRevolvingCreditById(inv.parentId)
                    if (revolving != null) {
                        bankDao.updateRevolvingCredit(revolving.copy(usedCredit = revolving.usedCredit - amount))
                    }
                }

                // 3. Update invoice status (PAID if amount fully covers it)
                if (amount >= inv.amount) {
                    bankDao.updateInvoice(inv.copy(status = InvoiceStatus.PAID))
                } else {
                    bankDao.updateInvoice(inv.copy(amount = inv.amount - amount))
                }

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
