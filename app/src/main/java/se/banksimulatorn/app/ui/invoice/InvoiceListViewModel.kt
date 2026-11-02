package se.banksimulatorn.app.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.Invoice
import se.banksimulatorn.app.data.InvoiceChains

/** All unpaid invoices, soonest due first. */
class InvoiceListViewModel(bankDao: BankDao) : ViewModel() {

    val invoices: StateFlow<List<Invoice>> = bankDao.getOpenInvoices()
        .map { InvoiceChains.outstanding(it).sortedBy { invoice -> invoice.dueDate } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
