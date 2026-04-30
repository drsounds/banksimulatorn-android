package se.banksimulatorn.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {
    @Serializable
    data object Dashboard : Destination

    @Serializable
    data class TransactionSimulator(val accountId: Int) : Destination

    @Serializable
    data class AccountDetail(val accountId: Int) : Destination

    @Serializable
    data class LoanDetail(val loanId: Int) : Destination

    @Serializable
    data class CreditDetail(val cardId: Int) : Destination

    @Serializable
    data class PurchaseSimulator(val cardId: Int) : Destination
}
