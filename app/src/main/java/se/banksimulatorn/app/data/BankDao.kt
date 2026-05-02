package se.banksimulatorn.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    // Accounts
    @Query("SELECT * FROM accounts WHERE deletedAt IS NULL AND closedAt IS NULL")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deletedAt IS NULL AND closedAt IS NULL")
    suspend fun getAccountById(id: Int): Account?

    @Query("SELECT * FROM accounts WHERE deletedAt IS NULL AND closedAt IS NULL")
    suspend fun getAllAccountsSync(): List<Account>

    @Query("SELECT * FROM accounts WHERE closedAt IS NOT NULL")
    fun getClosedAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: Int, amount: Double)

    // Revolving Credit Accounts
    @Query("SELECT * FROM revolving_credit_accounts WHERE deletedAt IS NULL AND closedAt IS NULL")
    fun getAllRevolvingCredits(): Flow<List<RevolvingCreditAccount>>

    @Query("SELECT * FROM revolving_credit_accounts WHERE id = :id AND deletedAt IS NULL AND closedAt IS NULL")
    suspend fun getRevolvingCreditById(id: Int): RevolvingCreditAccount?

    @Query("SELECT * FROM revolving_credit_accounts WHERE deletedAt IS NULL AND closedAt IS NULL")
    suspend fun getAllRevolvingCreditsSync(): List<RevolvingCreditAccount>

    @Query("SELECT * FROM revolving_credit_accounts WHERE closedAt IS NOT NULL")
    fun getClosedRevolvingCredits(): Flow<List<RevolvingCreditAccount>>

    @Insert
    suspend fun insertRevolvingCredit(account: RevolvingCreditAccount)

    @Update
    suspend fun updateRevolvingCredit(account: RevolvingCreditAccount)

    @Query("UPDATE revolving_credit_accounts SET usedCredit = usedCredit + :amount WHERE id = :id")
    suspend fun updateRevolvingCreditUsed(id: Int, amount: Double)

    @Query("UPDATE revolving_credit_accounts SET pendingAuthorizations = pendingAuthorizations + :amount WHERE id = :id")
    suspend fun updateRevolvingCreditPending(id: Int, amount: Double)

    // Loans
    @Query("SELECT * FROM loans WHERE closedAt IS NULL AND deletedAt IS NULL")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :id AND closedAt IS NULL AND deletedAt IS NULL")
    suspend fun getLoanById(id: Int): Loan?

    @Query("SELECT * FROM loans WHERE closedAt IS NULL AND deletedAt IS NULL")
    suspend fun getAllLoansSync(): List<Loan>

    @Query("SELECT * FROM loans WHERE closedAt IS NOT NULL")
    fun getClosedLoans(): Flow<List<Loan>>

    @Insert
    suspend fun insertLoan(loan: Loan)

    @Update
    suspend fun updateLoan(loan: Loan)

    // Credit Cards (Physical/Virtual Cards)
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCreditCardById(id: Int): CreditCard?

    @Insert
    suspend fun insertCreditCard(creditCard: CreditCard)

    @Update
    suspend fun updateCreditCard(creditCard: CreditCard)

    // Invoices
    @Query("SELECT * FROM invoices WHERE deletedAt IS NULL")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE status != 'PAID' AND deletedAt IS NULL")
    fun getOpenInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE parentId = :parentId AND deletedAt IS NULL ORDER BY issuedDate DESC")
    fun getInvoicesForAccount(parentId: Int): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id AND deletedAt IS NULL")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Query("SELECT * FROM invoices WHERE status = 'PENDING' AND deletedAt IS NULL")
    suspend fun getPendingInvoicesSync(): List<Invoice>

    @Insert
    suspend fun insertInvoice(invoice: Invoice)

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    // Recurring Tasks
    @Query("SELECT * FROM recurring_tasks WHERE deletedAt IS NULL")
    fun getAllRecurringTasks(): Flow<List<RecurringTask>>

    @Query("SELECT * FROM recurring_tasks WHERE deletedAt IS NULL")
    suspend fun getAllRecurringTasksSync(): List<RecurringTask>

    @Insert
    suspend fun insertRecurringTask(task: RecurringTask)

    @Update
    suspend fun updateRecurringTask(task: RecurringTask)

    // Global Settings
    @Query("SELECT * FROM global_settings WHERE id = 1")
    fun getGlobalSettings(): Flow<GlobalSettings?>

    @Query("SELECT COUNT(*) FROM global_settings")
    suspend fun hasGlobalSettings(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlobalSettings(settings: GlobalSettings)

    // Transactions
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getTransactionsForAccount(accountId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE revolvingCreditAccountId = :id AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getTransactionsForRevolvingCredit(id: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'BLOCKED' AND deletedAt IS NULL")
    suspend fun getBlockedTransactionsSync(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = 'INTEREST' AND timestamp > :timestamp")
    suspend fun getFutureInterestTransactions(timestamp: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE status = 'COMPLETED' AND (chargedAt > :timestamp OR timestamp > :timestamp)")
    suspend fun getFutureChargedTransactions(timestamp: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id AND deletedAt IS NULL")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE revolvingCreditAccountId = :id AND isReconciled = 0 AND deletedAt IS NULL")
    suspend fun getUnreconciledCreditTransactions(id: Int): List<Transaction>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteTransaction(id: Int, timestamp: Long)

    @RoomTransaction
    suspend fun performTransaction(transaction: Transaction, account: Account) {
        insertTransaction(transaction)
        updateAccount(account)
    }

    @RoomTransaction
    suspend fun performRevolvingCreditTransaction(transaction: Transaction, creditAccount: RevolvingCreditAccount) {
        insertTransaction(transaction)
        updateRevolvingCredit(creditAccount)
    }

    @RoomTransaction
    suspend fun chargeBlockedTransaction(transactionId: Int, revolvingId: Int, amount: Double, timestamp: Long) {
        val transaction = getTransactionById(transactionId) ?: return
        val revolving = getRevolvingCreditById(revolvingId) ?: return
        
        val absAmount = Math.abs(amount)
        updateTransaction(transaction.copy(
            status = TransactionStatus.COMPLETED, 
            chargedAt = timestamp, 
            timestamp = timestamp
        ))
        updateRevolvingCredit(revolving.copy(
            pendingAuthorizations = revolving.pendingAuthorizations - absAmount,
            usedCredit = revolving.usedCredit + absAmount
        ))
    }
    
    @RoomTransaction
    suspend fun releaseBlockedTransaction(transactionId: Int, revolvingId: Int, amount: Double) {
        val revolving = getRevolvingCreditById(revolvingId) ?: return
        
        val absAmount = Math.abs(amount)
        softDeleteTransaction(transactionId, System.currentTimeMillis())
        updateRevolvingCredit(revolving.copy(
            pendingAuthorizations = revolving.pendingAuthorizations - absAmount
        ))
    }

    // Time Settings
    @Query("SELECT * FROM time_settings WHERE id = 1")
    fun getTimeSettings(): Flow<TimeSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTimeSettings(settings: TimeSettings)

    @RoomTransaction
    suspend fun clearAllData() {
        clearAccounts()
        clearTransactions()
        clearLoans()
        clearCreditCards()
        clearRevolvingCredits()
        clearInvoices()
        clearRecurringTasks()
    }

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM credit_cards")
    suspend fun clearCreditCards()

    @Query("DELETE FROM revolving_credit_accounts")
    suspend fun clearRevolvingCredits()

    @Query("DELETE FROM invoices")
    suspend fun clearInvoices()

    @Query("DELETE FROM recurring_tasks")
    suspend fun clearRecurringTasks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<Loan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCards(cards: List<CreditCard>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevolvingCredits(credits: List<RevolvingCreditAccount>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<Invoice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTasks(tasks: List<RecurringTask>)

    @RoomTransaction
    suspend fun seedDefaultData() {
        if (hasGlobalSettings() > 0) return

        val defaultCurrency = "SEK"
        updateGlobalSettings(GlobalSettings(currency = defaultCurrency))
        
        val isNordic = listOf("SEK", "NOK", "DKK").contains(defaultCurrency)
        val scale = if (isNordic) 1.0 else 0.1
        
        val deposit1 = 1000.0 * scale
        val deposit2 = 16000.0 * scale
        val now = System.currentTimeMillis()

        insertAccount(Account(id = 1, name = "Checking 1", accountNumber = "9999-1111", balance = deposit1, type = AccountType.CHECKING))
        insertTransaction(Transaction(
            accountId = 1,
            amount = deposit1,
            timestamp = now,
            description = "Initial Deposit",
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.COMPLETED
        ))

        insertAccount(Account(id = 2, name = "Checking 2", accountNumber = "9999-2222", balance = deposit2, type = AccountType.CHECKING))
        insertTransaction(Transaction(
            accountId = 2,
            amount = deposit2,
            timestamp = now,
            description = "Initial Deposit",
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.COMPLETED
        ))
        
        insertRevolvingCredit(RevolvingCreditAccount(id = 1, name = "Credit Card", creditLimit = 10000.0 * scale, interestRate = 15.5, statementDay = 25))
        
        insertCreditCard(CreditCard(id = 1, name = "Debit Card", cardNumber = "4242-4242-1111-1111", type = CardType.DEBIT, linkedAccountId = 1))
        insertCreditCard(CreditCard(id = 2, name = "Credit Card", cardNumber = "4242-4242-2222-2222", type = CardType.CREDIT, linkedCreditAccountId = 1))
    }
}
