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
                        ?: throw IllegalStateException("Empty response")
                    importGeneratedBundle(bundle)

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
            try {
                bankDao.seedDefaultData()
                _uiEvent.emit(OnboardingUiEvent.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit(OnboardingUiEvent.Error("Could not create default accounts: ${e.message}"))
            }
        }
    }

    /**
     * Gson ignores Kotlin defaults and nullability, so any field the model leaves out
     * arrives as null (lists, account numbers, loan types, dates...). Inserting those
     * as-is violates NOT NULL constraints, so fill in sensible defaults first.
     * The `as T?` casts stop the compiler from treating the elvis fallbacks as dead code.
     */
    private suspend fun importGeneratedBundle(bundle: BankDataBundle) {
        val now = System.currentTimeMillis()

        val accounts = (bundle.accounts as List<Account?>? ?: emptyList()).filterNotNull().mapIndexed { index, a ->
            a.copy(
                name = a.name as String? ?: "Account ${index + 1}",
                accountNumber = a.accountNumber as String? ?: "9999-%04d".format(index + 1),
                type = a.type as AccountType? ?: AccountType.CHECKING
            )
        }
        val revolvingCredits = (bundle.revolvingCredits as List<RevolvingCreditAccount?>? ?: emptyList()).filterNotNull().mapIndexed { index, c ->
            c.copy(name = c.name as String? ?: "Credit ${index + 1}")
        }
        val loans = (bundle.loans as List<Loan?>? ?: emptyList()).filterNotNull().mapIndexed { index, l ->
            l.copy(
                name = l.name as String? ?: "Loan ${index + 1}",
                type = l.type as String? ?: "Loan",
                nextPaymentDate = l.nextPaymentDate as String? ?: ""
            )
        }
        val assets = (bundle.assets as List<Asset?>? ?: emptyList()).filterNotNull().mapIndexed { index, a ->
            a.copy(
                name = a.name as String? ?: "Asset ${index + 1}",
                type = a.type as AssetType? ?: AssetType.OTHER,
                initialValue = if (a.initialValue == 0.0) a.currentValue else a.initialValue,
                purchaseDate = if (a.purchaseDate == 0L) now else a.purchaseDate
            )
        }
        val budgetItems = (bundle.budgetItems as List<BudgetItem?>? ?: emptyList()).filterNotNull().mapIndexed { index, b ->
            b.copy(
                name = b.name as String? ?: "Budget item ${index + 1}",
                type = b.type as BudgetType? ?: BudgetType.EXPENSE,
                frequency = b.frequency as BudgetFrequency? ?: BudgetFrequency.MONTHLY,
                paymentMethod = b.paymentMethod as PaymentMethod? ?: PaymentMethod.DIRECT_DEBIT,
                startDate = if (b.startDate == 0L) now else b.startDate
            )
        }
        val settings = bundle.globalSettings as GlobalSettings?
        val globalSettings = GlobalSettings(
            currency = settings?.currency as String? ?: "SEK",
            country = settings?.country as String? ?: "Sweden"
        )

        // The prompt asks for these to be empty; drop them rather than risk rows
        // that reference accounts the model never created.
        bankDao.replaceAllData(
            accounts = accounts,
            transactions = emptyList(),
            loans = loans,
            creditCards = emptyList(),
            revolvingCredits = revolvingCredits,
            invoices = emptyList(),
            recurringTasks = emptyList(),
            assets = assets,
            budgetItems = budgetItems,
            globalSettings = globalSettings
        )
    }
}

sealed class OnboardingUiEvent {
    data object Loading : OnboardingUiEvent()
    data object Success : OnboardingUiEvent()
    data class Error(val message: String) : OnboardingUiEvent()
}
