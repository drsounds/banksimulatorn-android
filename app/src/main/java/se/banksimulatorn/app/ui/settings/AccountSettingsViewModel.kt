package se.banksimulatorn.app.ui.settings

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
import se.banksimulatorn.app.data.RevolvingCreditAccount
import se.banksimulatorn.app.data.Loan
import se.banksimulatorn.app.navigation.AccountSettingsType

class AccountSettingsViewModel(
    private val id: Int,
    private val type: AccountSettingsType,
    private val bankDao: BankDao
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _revolvingAccount = MutableStateFlow<RevolvingCreditAccount?>(null)
    val revolvingAccount: StateFlow<RevolvingCreditAccount?> = _revolvingAccount.asStateFlow()

    private val _loan = MutableStateFlow<Loan?>(null)
    val loan: StateFlow<Loan?> = _loan.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AccountSettingsUiEvent>()
    val uiEvent: SharedFlow<AccountSettingsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            when (type) {
                AccountSettingsType.ACCOUNT -> _account.value = bankDao.getAccountById(id)
                AccountSettingsType.CREDIT_CARD -> _revolvingAccount.value = bankDao.getRevolvingCreditById(id)
                AccountSettingsType.LOAN -> _loan.value = bankDao.getLoanById(id)
            }
        }
    }

    fun saveAccountSettings(positiveRate: Double, overdraftRate: Double, capitalizationDay: Int) {
        viewModelScope.launch {
            _account.value?.let { acc ->
                val updated = acc.copy(
                    positiveInterestRate = positiveRate,
                    overdraftInterestRate = overdraftRate,
                    interestCapitalizationDay = capitalizationDay
                )
                bankDao.updateAccount(updated)
                _account.value = updated
                _uiEvent.emit(AccountSettingsUiEvent.Success)
            }
        }
    }

    fun saveCreditSettings(cycleDay: Int, bnpl: Boolean, interestRate: Double) {
        viewModelScope.launch {
            _revolvingAccount.value?.let { card ->
                val updated = card.copy(
                    invoiceCycleDay = cycleDay,
                    isBnplMode = bnpl,
                    interestRate = interestRate
                )
                bankDao.updateRevolvingCredit(updated)
                _revolvingAccount.value = updated
                _uiEvent.emit(AccountSettingsUiEvent.Success)
            }
        }
    }

    fun saveLoanSettings(cycleDay: Int, fee: Double) {
        viewModelScope.launch {
            _loan.value?.let { l ->
                val updated = l.copy(
                    invoiceCycleDay = cycleDay,
                    loanFee = fee
                )
                bankDao.updateLoan(updated)
                _loan.value = updated
                _uiEvent.emit(AccountSettingsUiEvent.Success)
            }
        }
    }
}

sealed class AccountSettingsUiEvent {
    data object Success : AccountSettingsUiEvent()
    data class Error(val message: String) : AccountSettingsUiEvent()
}
