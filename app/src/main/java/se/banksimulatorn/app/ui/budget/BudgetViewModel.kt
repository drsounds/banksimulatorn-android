package se.banksimulatorn.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.BudgetItem
import se.banksimulatorn.app.data.BankDao

class BudgetViewModel(private val bankDao: BankDao) : ViewModel() {

    val budgetItems: StateFlow<List<BudgetItem>> = bankDao.getAllBudgetItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteBudgetItem(item: BudgetItem) {
        viewModelScope.launch {
            bankDao.updateBudgetItem(item.copy(deletedAt = System.currentTimeMillis()))
        }
    }
}
