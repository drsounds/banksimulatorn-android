package se.banksimulatorn.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class VoluntaryPaymentsTest {
    @Test
    fun paymentReducesDebt() {
        assertEquals(75.0, VoluntaryPayments.remainingDebtAfterPayment(100.0, 25.0), 0.0)
    }

    @Test
    fun paymentAboveOutstandingBalancePreservesCreditBalance() {
        assertEquals(-50.0, VoluntaryPayments.remainingDebtAfterPayment(100.0, 150.0), 0.0)
    }
}
