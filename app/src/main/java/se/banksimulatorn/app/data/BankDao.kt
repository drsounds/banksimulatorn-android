package se.banksimulatorn.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT * FROM accounts WHERE deletedAt IS NULL")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deletedAt IS NULL")
    suspend fun getAccountById(id: Int): Account?

    @Insert
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getTransactionsForAccount(accountId: Int): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @RoomTransaction
    suspend fun performTransaction(transaction: Transaction, account: Account) {
        insertTransaction(transaction)
        updateAccount(account)
    }

    // Loans
    @Query("SELECT * FROM loans")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: Int): Loan?

    @Insert
    suspend fun insertLoan(loan: Loan)

    // Credit Cards
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCreditCardById(id: Int): CreditCard?

    @Insert
    suspend fun insertCreditCard(creditCard: CreditCard)

    @Update
    suspend fun updateCreditCard(creditCard: CreditCard)

    @RoomTransaction
    suspend fun performCreditTransaction(transaction: Transaction, card: CreditCard) {
        insertTransaction(transaction)
        updateCreditCard(card)
    }

    @Query("SELECT * FROM transactions WHERE creditCardId = :cardId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getTransactionsForCreditCard(cardId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("UPDATE transactions SET deletedAt = :timestamp WHERE id = :transactionId")
    suspend fun softDeleteTransaction(transactionId: Int, timestamp: Long)

    @Query("SELECT * FROM transactions WHERE id = :transactionId AND deletedAt IS NULL")
    suspend fun getTransactionById(transactionId: Int): Transaction?

    @RoomTransaction
    suspend fun chargeBlockedTransaction(transactionId: Int, cardId: Int, amount: Double) {
        val transaction = getTransactionById(transactionId) ?: return
        val card = getCreditCardById(cardId) ?: return
        
        updateTransaction(transaction.copy(status = TransactionStatus.COMPLETED, chargedAt = System.currentTimeMillis()))
        updateCreditCard(card.copy(pendingAuthorizations = card.pendingAuthorizations - Math.abs(amount)))
    }
    
    @RoomTransaction
    suspend fun releaseBlockedTransaction(transactionId: Int, cardId: Int, amount: Double) {
        val transaction = getTransactionById(transactionId) ?: return
        val card = getCreditCardById(cardId) ?: return
        
        softDeleteTransaction(transactionId, System.currentTimeMillis())
        updateCreditCard(card.copy(
            usedCredit = card.usedCredit - Math.abs(amount),
            pendingAuthorizations = card.pendingAuthorizations - Math.abs(amount)
        ))
    }
}
