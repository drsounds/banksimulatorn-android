package se.banksimulatorn.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import se.banksimulatorn.app.ai.GeminiManager
import se.banksimulatorn.app.ui.settings.BankDataBundle
import se.banksimulatorn.app.data.*

class OnboardingViewModel(
    private val bankDao: BankDao,
    private val geminiManager: GeminiManager
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<OnboardingUiEvent>()
    val uiEvent: SharedFlow<OnboardingUiEvent> = _uiEvent.asSharedFlow()

    val aiStatusMessage: StateFlow<String> = geminiManager.statusMessage
    val isAiModelReady: StateFlow<Boolean> = geminiManager.isModelReady

    fun generateLife(description: String) {
        viewModelScope.launch {
            val isReady = geminiManager.checkReadiness()
            if (!isReady) {
                _uiEvent.emit(OnboardingUiEvent.Error("AI Model is not ready. Status: ${geminiManager.statusMessage.value}"))
                return@launch
            }

            _uiEvent.emit(OnboardingUiEvent.Loading)
            val json = geminiManager.generateInitialLifeState(description)
            if (json != null) {
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
                    
                    _uiEvent.emit(OnboardingUiEvent.Success)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiEvent.emit(OnboardingUiEvent.Error("Parsing Error: ${e.message}. Please try again."))
                }
            } else {
                _uiEvent.emit(OnboardingUiEvent.Error("AI Generation Failed. Gemini Nano may be unavailable."))
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            bankDao.seedDefaultData()
            _uiEvent.emit(OnboardingUiEvent.Success)
        }
    }
}

sealed class OnboardingUiEvent {
    data object Loading : OnboardingUiEvent()
    data object Success : OnboardingUiEvent()
    data class Error(val message: String) : OnboardingUiEvent()
}
