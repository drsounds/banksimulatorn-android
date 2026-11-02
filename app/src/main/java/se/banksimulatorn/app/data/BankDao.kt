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

    /** Emits again whenever the account changes, so detail screens stay current. */
    @Query("SELECT * FROM accounts WHERE id = :id AND deletedAt IS NULL AND closedAt IS NULL")
    fun observeAccountById(id: Int): Flow<Account?>

    @Query("SELECT * FROM accounts WHERE closedAt IS NOT NULL")
    fun getClosedAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insertAccount(account: Account): Long

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

    @Query("SELECT * FROM revolving_credit_accounts WHERE id = :id AND deletedAt IS NULL AND closedAt IS NULL")
    fun observeRevolvingCreditById(id: Int): Flow<RevolvingCreditAccount?>

    @Query("SELECT * FROM revolving_credit_accounts WHERE closedAt IS NOT NULL")
    fun getClosedRevolvingCredits(): Flow<List<RevolvingCreditAccount>>

    @Insert
    suspend fun insertRevolvingCredit(account: RevolvingCreditAccount): Long

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

    @Query("SELECT * FROM loans WHERE id = :id AND closedAt IS NULL AND deletedAt IS NULL")
    fun observeLoanById(id: Int): Flow<Loan?>

    @Query("SELECT * FROM loans WHERE closedAt IS NOT NULL")
    fun getClosedLoans(): Flow<List<Loan>>

    @Insert
    suspend fun insertLoan(loan: Loan): Long

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

    @Query("SELECT * FROM invoices WHERE parentId = :parentId AND parentType = :parentType AND deletedAt IS NULL")
    suspend fun getInvoicesForParentSync(parentId: Int, parentType: String): List<Invoice>

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

    // Assets
    @Query("SELECT * FROM assets WHERE deletedAt IS NULL")
    fun getAllAssets(): Flow<List<Asset>>

    @Insert
    suspend fun insertAsset(asset: Asset)

    @Update
    suspend fun updateAsset(asset: Asset)

    // Budget Items
    @Query("SELECT * FROM budget_items WHERE deletedAt IS NULL")
    fun getAllBudgetItems(): Flow<List<BudgetItem>>

    @Query("SELECT * FROM budget_items WHERE deletedAt IS NULL")
    suspend fun getAllBudgetItemsSync(): List<BudgetItem>

    @Insert
    suspend fun insertBudgetItem(item: BudgetItem)

    @Update
    suspend fun updateBudgetItem(item: BudgetItem)

    // Personas
    @Query("SELECT * FROM personas")
    fun getAllPersonas(): Flow<List<Persona>>

    @Insert
    suspend fun insertPersona(persona: Persona)

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

    @Query("SELECT * FROM transactions WHERE type IN ('INTEREST', 'FEE') AND timestamp > :timestamp AND deletedAt IS NULL")
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

    /**
     * Pays [amount] of an invoice from a bank account, all in one database transaction.
     * The amount is capped at what is left on the invoice. For a credit card invoice
     * only the part that covers used credit lowers the credit line's debt; the rest
     * covers reminder fees and late interest, which never were part of the used credit.
     * Paying an invoice or reminder in full marks every invoice for the same debt paid.
     *
     * @return the amount actually paid.
     */
    @RoomTransaction
    suspend fun payInvoice(invoiceId: Int, accountId: Int, amount: Double, timestamp: Long): Double {
        val invoice = getInvoiceById(invoiceId) ?: throw IllegalStateException("Invoice not found")
        val account = getAccountById(accountId) ?: throw IllegalStateException("Account not found")
        val payment = InvoicePayments.cappedPayment(invoice, amount)
        if (payment <= 0.0) throw IllegalArgumentException("Nothing to pay")
        if (account.balance < payment) throw IllegalStateException("Insufficient funds on selected account")

        insertTransaction(Transaction(
            accountId = accountId,
            amount = -payment,
            timestamp = timestamp,
            description = "Payment of invoice #${invoice.id}",
            type = TransactionType.WITHDRAWAL,
            status = TransactionStatus.COMPLETED
        ))
        updateAccount(account.copy(balance = account.balance - payment))

        if (invoice.parentType == "CREDIT") {
            val credit = getRevolvingCreditById(invoice.parentId)
            if (credit != null) {
                val reduction = InvoicePayments.creditReduction(credit.usedCredit, payment)
                if (reduction > 0.0) {
                    insertTransaction(Transaction(
                        revolvingCreditAccountId = credit.id,
                        amount = reduction,
                        timestamp = timestamp,
                        description = "Payment received",
                        type = TransactionType.DEPOSIT,
                        status = TransactionStatus.COMPLETED
                    ))
                    updateRevolvingCredit(credit.copy(usedCredit = credit.usedCredit - reduction))
                }
            }
        }

        val paidAmount = invoice.paidAmount + payment
        val fullyPaid = InvoicePayments.isFullyPaid(invoice.amount, paidAmount)
        updateInvoice(invoice.copy(
            paidAmount = paidAmount,
            status = if (fullyPaid) InvoiceStatus.PAID else invoice.status
        ))
        if (fullyPaid) {
            val siblings = getInvoicesForParentSync(invoice.parentId, invoice.parentType)
            InvoiceChains.relatedChain(invoice, siblings)
                .filter { it.id != invoice.id && it.status != InvoiceStatus.PAID }
                .forEach { updateInvoice(it.copy(status = InvoiceStatus.PAID)) }
        }
        return payment
    }

    @RoomTransaction
    suspend fun payInvoices(invoiceIds: List<Int>, accountId: Int, timestamp: Long): Double {
        val invoices = invoiceIds.distinct().map { id ->
            getInvoiceById(id) ?: throw IllegalStateException("Invoice not found")
        }.filter { it.status != InvoiceStatus.PAID }
        val total = invoices.sumOf { InvoiceChains.remaining(it) }
        val account = getAccountById(accountId) ?: throw IllegalStateException("Account not found")
        if (account.balance < total) throw IllegalStateException("Insufficient funds on selected account")

        return invoices.sumOf { invoice ->
            payInvoice(invoice.id, accountId, InvoiceChains.remaining(invoice), timestamp)
        }
    }

    /**
     * Opens a new account from the "Open new" flow. A non-zero opening balance is
     * booked as an initial deposit so the account history matches its balance.
     */
    @RoomTransaction
    suspend fun openAccount(account: Account, openedAt: Long): Int {
        val id = insertAccount(account).toInt()
        if (account.balance != 0.0) {
            insertTransaction(Transaction(
                accountId = id,
                amount = account.balance,
                timestamp = openedAt,
                description = "Initial Deposit",
                type = TransactionType.DEPOSIT,
                status = TransactionStatus.COMPLETED
            ))
        }
        return id
    }

    /**
     * Opens a new credit line and, when [card] is given, issues a credit card
     * linked to it so purchases can be simulated straight away.
     */
    @RoomTransaction
    suspend fun openRevolvingCredit(credit: RevolvingCreditAccount, card: CreditCard?): Int {
        val id = insertRevolvingCredit(credit).toInt()
        if (card != null) {
            insertCreditCard(card.copy(linkedAccountId = null, linkedCreditAccountId = id, type = CardType.CREDIT))
        }
        return id
    }

    /**
     * Takes out a new loan. When [payoutAccountId] points at an open account, the
     * loan amount is paid out to it as a deposit, like a real bank would.
     */
    @RoomTransaction
    suspend fun openLoan(loan: Loan, payoutAccountId: Int?, openedAt: Long): Int {
        val id = insertLoan(loan).toInt()
        val payoutAccount = payoutAccountId?.let { getAccountById(it) }
        if (payoutAccount != null && loan.balance > 0.0) {
            updateAccountBalance(payoutAccount.id, loan.balance)
            insertTransaction(Transaction(
                accountId = payoutAccount.id,
                amount = loan.balance,
                timestamp = openedAt,
                description = "Loan payout: ${loan.name}",
                type = TransactionType.DEPOSIT,
                status = TransactionStatus.COMPLETED
            ))
        }
        return id
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
        clearAssets()
        clearBudgetItems()
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

    @Query("DELETE FROM assets")
    suspend fun clearAssets()

    @Query("DELETE FROM budget_items")
    suspend fun clearBudgetItems()

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

    /**
     * Replaces everything with an onboarding bundle in one transaction, so a failure
     * part-way (e.g. a malformed AI response) leaves no half-created accounts behind.
     */
    @RoomTransaction
    suspend fun replaceAllData(
        accounts: List<Account>,
        transactions: List<Transaction>,
        loans: List<Loan>,
        creditCards: List<CreditCard>,
        revolvingCredits: List<RevolvingCreditAccount>,
        invoices: List<Invoice>,
        recurringTasks: List<RecurringTask>,
        assets: List<Asset>,
        budgetItems: List<BudgetItem>,
        globalSettings: GlobalSettings
    ) {
        clearAllData()
        insertAccounts(accounts)
        insertRevolvingCredits(revolvingCredits)
        insertTransactions(transactions)
        insertLoans(loans)
        insertCreditCards(creditCards)
        insertInvoices(invoices)
        insertRecurringTasks(recurringTasks)
        assets.forEach { insertAsset(it) }
        budgetItems.forEach { insertBudgetItem(it) }
        updateGlobalSettings(globalSettings)
    }

    /**
     * Creates the default set of accounts and cards. Always starts from an empty
     * database: the fixed ids below would otherwise collide with rows left behind
     * by an earlier, failed onboarding attempt, and "Reset system" relies on this
     * running even though global settings already exist.
     */
    @RoomTransaction
    suspend fun seedDefaultData() {
        clearAllData()

        val defaultCurrency = "SEK"
        updateGlobalSettings(GlobalSettings(currency = defaultCurrency, country = "Sweden"))
        
        val isNordic = listOf("SEK", "NOK", "DKK").contains(defaultCurrency)
        val scale = if (isNordic) 1.0 else 0.1
        
        val deposit1 = 1000.0 * scale
        val deposit2 = 16000.0 * scale
        val now = System.currentTimeMillis()

        // Checking 1
        insertAccount(Account(id = 1, name = "Checking 1", accountNumber = "9999-1111", balance = deposit1, type = AccountType.CHECKING))
        insertTransaction(Transaction(
            accountId = 1,
            amount = deposit1,
            timestamp = now,
            description = "Initial Deposit",
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.COMPLETED
        ))

        // Checking 2
        insertAccount(Account(id = 2, name = "Checking 2", accountNumber = "9999-2222", balance = deposit2, type = AccountType.CHECKING))
        insertTransaction(Transaction(
            accountId = 2,
            amount = deposit2,
            timestamp = now,
            description = "Initial Deposit",
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.COMPLETED
        ))
        
        // Credit Card
        insertRevolvingCredit(RevolvingCreditAccount(id = 1, name = "Credit Card", creditLimit = 10000.0 * scale, interestRate = 15.5, statementDay = 25))
        
        // Linking Cards
        insertCreditCard(CreditCard(id = 1, name = "Debit Card", cardNumber = "4242-4242-1111-1111", type = CardType.DEBIT, linkedAccountId = 1))
        insertCreditCard(CreditCard(id = 2, name = "Credit Card", cardNumber = "4242-4242-2222-2222", type = CardType.CREDIT, linkedCreditAccountId = 1))
    }
}
