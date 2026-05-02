package se.banksimulatorn.app.ui.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.banksimulatorn.app.ai.GeminiManager
import se.banksimulatorn.app.data.BankDao
import se.banksimulatorn.app.ui.settings.BankDataBundle

class AIChatViewModel(
    private val bankDao: BankDao,
    private val geminiManager: GeminiManager
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<AIChatUiEvent>()
    val uiEvent: SharedFlow<AIChatUiEvent> = _uiEvent.asSharedFlow()

    fun simulateEvent(event: String) {
        viewModelScope.launch {
            _uiEvent.emit(AIChatUiEvent.Loading)
            
            try {
                // 1. Snapshot current state
                val accounts = bankDao.getAllAccountsSync()
                val transactions = bankDao.getAllTransactions().first()
                val loans = bankDao.getAllLoansSync()
                val cards = bankDao.getAllCreditCards().first()
                val credits = bankDao.getAllRevolvingCreditsSync()
                val invoices = bankDao.getAllInvoices().first()
                val recurring = bankDao.getAllRecurringTasksSync()
                val assets = bankDao.getAllAssets().first()
                val budget = bankDao.getAllBudgetItemsSync()
                val settings = bankDao.getGlobalSettings().first() ?: se.banksimulatorn.app.data.GlobalSettings()

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
                
                val currentStateJson = Gson().toJson(bundle)

                // 2. Call Gemini
                val updatedJson = geminiManager.simulateEconomicEvent(event, currentStateJson)
                if (updatedJson != null) {
                    val updatedBundle = Gson().fromJson(updatedJson, BankDataBundle::class.java)
                    
                    // 3. Apply changes (Wipe and re-insert for simplicity in simulation)
                    bankDao.clearAllData()
                    bankDao.insertAccounts(updatedBundle.accounts)
                    bankDao.insertTransactions(updatedBundle.transactions)
                    bankDao.insertLoans(updatedBundle.loans)
                    bankDao.insertCreditCards(updatedBundle.creditCards)
                    bankDao.insertRevolvingCredits(updatedBundle.revolvingCredits)
                    bankDao.insertInvoices(updatedBundle.invoices)
                    bankDao.insertRecurringTasks(updatedBundle.recurringTasks)
                    
                    updatedBundle.assets?.let { assetsList ->
                        assetsList.forEach { bankDao.insertAsset(it) }
                    }
                    updatedBundle.budgetItems?.let { budgetList ->
                        budgetList.forEach { bankDao.insertBudgetItem(it) }
                    }
                    
                    bankDao.updateGlobalSettings(updatedBundle.globalSettings)

                    _uiEvent.emit(AIChatUiEvent.Success("Simulation complete: Event processed."))
                } else {
                    _uiEvent.emit(AIChatUiEvent.Error("AI simulation failed."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit(AIChatUiEvent.Error("Parsing failed: ${e.message}"))
            }
        }
    }
}

sealed class AIChatUiEvent {
    data object Loading : AIChatUiEvent()
    data class Success(val message: String) : AIChatUiEvent()
    data class Error(val message: String) : AIChatUiEvent()
}
