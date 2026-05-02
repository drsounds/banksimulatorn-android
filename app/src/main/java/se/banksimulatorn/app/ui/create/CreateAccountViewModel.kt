package se.banksimulatorn.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    fun createBankAccount(name: String, number: String, balance: Double, type: AccountType) {
        viewModelScope.launch {
            bankDao.insertAccount(Account(name = name, accountNumber = number, balance = balance, type = type))
            _uiEvent.emit(CreateUiEvent.Success)
        }
    }

    fun createLoan(name: String, balance: Double, fee: Double, cycleDay: Int) {
        viewModelScope.launch {
            bankDao.insertLoan(Loan(
                name = name, 
                type = "Loan", 
                balance = balance, 
                pendingInterest = 0.0, 
                nextPaymentAmount = balance / 12, 
                nextPaymentDate = "2026-06-30",
                loanFee = fee,
                invoiceCycleDay = cycleDay
            ))
            _uiEvent.emit(CreateUiEvent.Success)
        }
    }

    fun createRevolvingCredit(name: String, limit: Double, rate: Double, cycleDay: Int) {
        viewModelScope.launch {
            bankDao.insertRevolvingCredit(RevolvingCreditAccount(
                name = name,
                creditLimit = limit,
                interestRate = rate,
                invoiceCycleDay = cycleDay
            ))
            _uiEvent.emit(CreateUiEvent.Success)
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
