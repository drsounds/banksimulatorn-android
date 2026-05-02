package se.banksimulatorn.app.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.banksimulatorn.app.data.Asset
import se.banksimulatorn.app.data.BankDao

class AssetViewModel(private val bankDao: BankDao) : ViewModel() {

    val assets: StateFlow<List<Asset>> = bankDao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            bankDao.updateAsset(asset.copy(deletedAt = System.currentTimeMillis()))
        }
    }
}
