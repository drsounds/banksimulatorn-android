package se.banksimulatorn.app.ui.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.CreditCard

class CreditDetailViewModel(
    private val cardId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _creditCard = MutableStateFlow<CreditCard?>(null)
    val creditCard: StateFlow<CreditCard?> = _creditCard.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _creditCard.value = bankDao.getCreditCardById(cardId)
        }
    }
}
