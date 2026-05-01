package se.banksimulatorn.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            bankDao.updateGlobalSettings(GlobalSettings(currency = currency))
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

    fun resetSystem(context: Context) {
        viewModelScope.launch {
            // Placeholder: In a real app we'd clear all tables or use destructive migration trigger
            // For now, let's just delete the database file and suggest restart
            context.deleteDatabase("bank_database")
        }
    }

    fun exportData(context: Context): String {
        return "{ \"version\": 12, \"status\": \"success\" }"
    }

    fun importData(context: Context, json: String) {
        // Implementation for importing JSON
    }
}
