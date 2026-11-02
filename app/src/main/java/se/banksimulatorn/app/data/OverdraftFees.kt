package se.banksimulatorn.app.data

/** Rules for the overdraft fee the time machine charges once a day. */
object OverdraftFees {

    private const val DAY_MS = 86_400_000L

    /**
     * True when [account] should be charged its overdraft fee at [now]: a fee is set,
     * the balance is more than the threshold below zero, and the cooling period since
     * the last fee has passed.
     */
    fun shouldCharge(account: Account, now: Long): Boolean {
        if (account.overdraftFee <= 0.0) return false
        if (account.balance >= -account.overdraftFeeThreshold.coerceAtLeast(0.0)) return false
        val last = account.lastOverdraftFeeAt ?: return true
        val cooling = account.overdraftFeeCoolingDays.coerceAtLeast(0).toLong() * DAY_MS
        return now - last >= cooling
    }
}
