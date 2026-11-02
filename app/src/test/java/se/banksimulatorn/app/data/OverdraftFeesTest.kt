package se.banksimulatorn.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverdraftFeesTest {

    private val day = 86_400_000L

    private fun account(
        balance: Double,
        fee: Double = 50.0,
        threshold: Double = 100.0,
        cooling: Int = 30,
        lastFee: Long? = null
    ) = Account(
        name = "Test", balance = balance, type = AccountType.CHECKING,
        overdraftFee = fee, overdraftFeeThreshold = threshold,
        overdraftFeeCoolingDays = cooling, lastOverdraftFeeAt = lastFee
    )

    @Test
    fun noFeeWhenDisabledOrWithinThreshold() {
        assertFalse(OverdraftFees.shouldCharge(account(balance = -500.0, fee = 0.0), 0))
        assertFalse(OverdraftFees.shouldCharge(account(balance = -100.0), 0))
        assertFalse(OverdraftFees.shouldCharge(account(balance = 20.0), 0))
    }

    @Test
    fun feeWhenBelowThreshold() {
        assertTrue(OverdraftFees.shouldCharge(account(balance = -100.01), 0))
        assertTrue(OverdraftFees.shouldCharge(account(balance = -0.01, threshold = 0.0), 0))
    }

    @Test
    fun coolingPeriodIsRespected() {
        val now = 100 * day
        assertFalse(OverdraftFees.shouldCharge(account(balance = -500.0, lastFee = now - 29 * day), now))
        assertTrue(OverdraftFees.shouldCharge(account(balance = -500.0, lastFee = now - 30 * day), now))
        assertTrue(OverdraftFees.shouldCharge(account(balance = -500.0, cooling = 0, lastFee = now), now))
    }
}
