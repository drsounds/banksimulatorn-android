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
    data class CreditDetail(val revolvingAccountId: Int) : Destination

    @Serializable
    data class PurchaseSimulator(val cardId: Int) : Destination

    @Serializable
    data class BlockedTransactionDetail(val transactionId: Int) : Destination

    @Serializable
    data class AccountSettings(val id: Int, val type: AccountSettingsType) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object CreateAccount : Destination

    @Serializable
    data object History : Destination

    @Serializable
    data class InvoicePayment(val invoiceId: Int) : Destination

    @Serializable
    data object Budget : Destination

    @Serializable
    data object Assets : Destination

    @Serializable
    data object AIChat : Destination

    @Serializable
    data object Onboarding : Destination
}

enum class AccountSettingsType {
    ACCOUNT, CREDIT_CARD, LOAN
}
