package se.banksimulatorn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cardNumber: String,
    val linkedAccountId: Int? = null,
    val linkedCreditAccountId: Int? = null,
    val type: CardType
)

enum class CardType {
    DEBIT, CREDIT
}
