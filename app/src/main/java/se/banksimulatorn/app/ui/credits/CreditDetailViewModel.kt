package se.banksimulatorn.app.ui.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.CreditCard
import se.banksimulatorn.app.data.Invoice
import se.banksimulatorn.app.data.RevolvingCreditAccount
import se.banksimulatorn.app.data.Transaction

class CreditDetailViewModel(
    private val revolvingAccountId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    val creditCard: StateFlow<CreditCard?> = bankDao.getAllCreditCards()
        .map { cards -> cards.find { it.linkedCreditAccountId == revolvingAccountId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Observed rather than loaded once, so used credit, interest and the available
    // amount follow date changes, purchases and payments without leaving the screen.
    val revolvingAccount: StateFlow<RevolvingCreditAccount?> = bankDao.observeRevolvingCreditById(revolvingAccountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val invoices: StateFlow<List<Invoice>> = bankDao.getInvoicesForAccount(revolvingAccountId)
        .map { list -> list.filter { it.parentType == "CREDIT" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val paymentAccounts = bankDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _payAllEvents = MutableSharedFlow<PayAllInvoicesEvent>()
    val payAllEvents = _payAllEvents.asSharedFlow()

    val transactions: StateFlow<List<Transaction>> = bankDao.getTransactionsForRevolvingCredit(revolvingAccountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun payAllInvoices(accountId: Int, invoiceIds: List<Int>) {
        viewModelScope.launch {
            try {
                val timestamp = bankDao.getTimeSettings().first()?.virtualCurrentTime ?: System.currentTimeMillis()
                bankDao.payInvoices(invoiceIds, accountId, timestamp)
                _payAllEvents.emit(PayAllInvoicesEvent.Success)
            } catch (e: Exception) {
                _payAllEvents.emit(PayAllInvoicesEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class PayAllInvoicesEvent {
    data object Success : PayAllInvoicesEvent()
    data class Error(val message: String) : PayAllInvoicesEvent()
}
