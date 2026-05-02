package se.banksimulatorn.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.*

class GlobalSettingsViewModel(private val bankDao: BankDao) : ViewModel() {

    val globalSettings: StateFlow<GlobalSettings?> = bankDao.getGlobalSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val closedAccounts: StateFlow<List<Account>> = bankDao.getClosedAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedRevolvingCredits: StateFlow<List<RevolvingCreditAccount>> = bankDao.getClosedRevolvingCredits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedLoans: StateFlow<List<Loan>> = bankDao.getClosedLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            val current = globalSettings.value ?: GlobalSettings()
            bankDao.updateGlobalSettings(current.copy(currency = currency))
        }
    }

    fun updateCountry(country: String) {
        viewModelScope.launch {
            val current = globalSettings.value ?: GlobalSettings()
            val newSettings = current.copy(country = country)
            bankDao.updateGlobalSettings(newSettings.copy(currency = newSettings.getCurrencyForCountry(country)))
        }
    }

    fun reopenAccount(account: Account) {
        viewModelScope.launch {
            bankDao.updateAccount(account.copy(closedAt = null))
        }
    }

    fun reopenRevolvingCredit(account: RevolvingCreditAccount) {
        viewModelScope.launch {
            bankDao.updateRevolvingCredit(account.copy(closedAt = null))
        }
    }

    fun reopenLoan(loan: Loan) {
        viewModelScope.launch {
            bankDao.updateLoan(loan.copy(closedAt = null))
        }
    }

    fun resetSystem() {
        viewModelScope.launch {
            bankDao.clearAllData()
            bankDao.seedDefaultData()
        }
    }

    fun performExport(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val accounts = bankDao.getAllAccountsSync()
            val transactions = bankDao.getAllTransactions().first()
            val loans = bankDao.getAllLoansSync()
            val cards = bankDao.getAllCreditCards().first()
            val credits = bankDao.getAllRevolvingCreditsSync()
            val invoices = bankDao.getAllInvoices().first()
            val recurring = bankDao.getAllRecurringTasksSync()
            val assets = bankDao.getAllAssets().first()
            val budget = bankDao.getAllBudgetItemsSync()
            val settings = bankDao.getGlobalSettings().first() ?: GlobalSettings()

            val bundle = BankDataBundle(
                accounts = accounts,
                transactions = transactions,
                loans = loans,
                creditCards = cards,
                revolvingCredits = credits,
                invoices = invoices,
                recurringTasks = recurring,
                assets = assets,
                budgetItems = budget,
                globalSettings = settings
            )
            
            val json = Gson().toJson(bundle)
            onExported(json)
        }
    }

    fun performImport(json: String) {
        viewModelScope.launch {
            try {
                val bundle = Gson().fromJson(json, BankDataBundle::class.java)
                bankDao.clearAllData()
                bankDao.insertAccounts(bundle.accounts)
                bankDao.insertTransactions(bundle.transactions)
                bankDao.insertLoans(bundle.loans)
                bankDao.insertCreditCards(bundle.creditCards)
                bankDao.insertRevolvingCredits(bundle.revolvingCredits)
                bankDao.insertInvoices(bundle.invoices)
                bankDao.insertRecurringTasks(bundle.recurringTasks)
                
                bundle.assets?.let { assets ->
                    assets.forEach { bankDao.insertAsset(it) }
                }
                bundle.budgetItems?.let { items ->
                    items.forEach { bankDao.insertBudgetItem(it) }
                }
                
                bankDao.updateGlobalSettings(bundle.globalSettings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class BankDataBundle(
    val accounts: List<Account>,
    val transactions: List<Transaction>,
    val loans: List<Loan>,
    val creditCards: List<CreditCard>,
    val revolvingCredits: List<RevolvingCreditAccount>,
    val invoices: List<Invoice>,
    val recurringTasks: List<RecurringTask>,
    val assets: List<Asset>? = emptyList(),
    val budgetItems: List<BudgetItem>? = emptyList(),
    val globalSettings: GlobalSettings
)
