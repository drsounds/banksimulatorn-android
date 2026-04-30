package se.banksimulatorn.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {
    @Serializable
    data object Dashboard : Destination

    @Serializable
    data class TransactionSimulator(val accountId: Int) : Destination

    @Serializable
    data object History : Destination
}
