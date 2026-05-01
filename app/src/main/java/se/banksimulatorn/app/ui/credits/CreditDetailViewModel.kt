package se.banksimulatorn.app.ui.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.data.CreditCard
import se.banksimulatorn.app.data.RevolvingCreditAccount
import se.banksimulatorn.app.data.Transaction

class CreditDetailViewModel(
    private val cardId: Int,
    private val bankDao: BankDao
) : ViewModel() {

    private val _creditCard = MutableStateFlow<CreditCard?>(null)
    val creditCard: StateFlow<CreditCard?> = _creditCard.asStateFlow()

    private val _revolvingAccount = MutableStateFlow<RevolvingCreditAccount?>(null)
    val revolvingAccount: StateFlow<RevolvingCreditAccount?> = _revolvingAccount.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _revolvingAccount
        .flatMapLatest { account ->
            if (account == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else bankDao.getTransactionsForRevolvingCredit(account.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val card = bankDao.getCreditCardById(cardId)
            _creditCard.value = card
            card?.linkedCreditAccountId?.let { id ->
                _revolvingAccount.value = bankDao.getRevolvingCreditById(id)
            }
        }
    }
}
