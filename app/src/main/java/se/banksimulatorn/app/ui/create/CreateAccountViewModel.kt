package se.banksimulatorn.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.*

class CreateAccountViewModel(private val bankDao: BankDao) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<CreateUiEvent>()
    val uiEvent: SharedFlow<CreateUiEvent> = _uiEvent.asSharedFlow()

    val allAccounts: StateFlow<List<Account>> = bankDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRevolvingCredits: StateFlow<List<RevolvingCreditAccount>> = bankDao.getAllRevolvingCredits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaving = MutableStateFlow(false)

    /** True while a create is being written, so the form can't submit twice. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private fun save(block: suspend () -> Unit) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                block()
                _uiEvent.emit(CreateUiEvent.Success)
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Simulated "now": the time machine's clock if it has been moved, else the real clock. */
    private suspend fun simulatedNow(): Long =
        bankDao.getTimeSettings().first()?.virtualCurrentTime ?: System.currentTimeMillis()

    fun createBankAccount(
        name: String,
        number: String,
        openingBalance: Double,
        type: AccountType,
        interestRate: Double
    ) {
        save {
            val now = simulatedNow()
            bankDao.openAccount(
                Account(
                    name = name.trim(),
                    accountNumber = number.trim().ifEmpty { generateAccountNumber() },
                    balance = openingBalance,
                    type = type,
                    positiveInterestRate = interestRate
                ),
                openedAt = now
            )
        }
    }

    fun createLoan(
        name: String,
        loanType: String,
        amount: Double,
        months: Int,
        monthlyFee: Double,
        cycleDay: Int,
        payoutAccountId: Int?
    ) {
        save {
            val now = simulatedNow()
            bankDao.openLoan(
                Loan(
                    name = name.trim(),
                    type = loanType,
                    balance = amount,
                    pendingInterest = 0.0,
                    nextPaymentAmount = amount / months,
                    nextPaymentDate = nextDateOnDay(now, cycleDay),
                    loanFee = monthlyFee,
                    invoiceCycleDay = cycleDay
                ),
                payoutAccountId = payoutAccountId,
                openedAt = now
            )
        }
    }

    fun createRevolvingCredit(
        name: String,
        limit: Double,
        rate: Double,
        statementDay: Int,
        issueCard: Boolean
    ) {
        save {
            val trimmedName = name.trim()
            bankDao.openRevolvingCredit(
                RevolvingCreditAccount(
                    name = trimmedName,
                    creditLimit = limit,
                    interestRate = rate,
                    statementDay = statementDay,
                    invoiceCycleDay = statementDay
                ),
                card = if (issueCard) {
                    CreditCard(name = trimmedName, cardNumber = generateCardNumber(), type = CardType.CREDIT)
                } else null
            )
        }
    }

    fun createCard(name: String, number: String, type: CardType, linkedAccountId: Int? = null, linkedCreditId: Int? = null) {
        viewModelScope.launch {
            bankDao.insertCreditCard(CreditCard(
                name = name,
                cardNumber = number,
                type = type,
                linkedAccountId = linkedAccountId,
                linkedCreditAccountId = linkedCreditId
            ))
            _uiEvent.emit(CreateUiEvent.Success)
        }
    }

    fun createAsset(name: String, type: AssetType, value: Double, growth: Double) {
        viewModelScope.launch {
            bankDao.insertAsset(Asset(
                name = name,
                type = type,
                initialValue = value,
                currentValue = value,
                monthlyGrowthRate = growth,
                purchaseDate = System.currentTimeMillis()
            ))
            _uiEvent.emit(CreateUiEvent.Success)
        }
    }

    fun createBudgetItem(name: String, amount: Double, type: BudgetType, frequency: BudgetFrequency, method: PaymentMethod, targetId: Int?, creditId: Int?) {
        viewModelScope.launch {
            bankDao.insertBudgetItem(BudgetItem(
                name = name,
                amount = amount,
                type = type,
                frequency = frequency,
                paymentMethod = method,
                targetAccountId = targetId,
                linkedCreditAccountId = creditId,
                startDate = System.currentTimeMillis()
            ))
            _uiEvent.emit(CreateUiEvent.Success)
        }
    }
}

sealed class CreateUiEvent {
    data object Success : CreateUiEvent()
}
