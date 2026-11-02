package se.banksimulatorn.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceLogicTest {

    private fun invoice(
        id: Int,
        amount: Double = 1000.0,
        paid: Double = 0.0,
        status: InvoiceStatus = InvoiceStatus.PENDING,
        reminder: Boolean = false,
        related: Int? = null,
        issued: Long = id.toLong(),
        parentId: Int = 1
    ) = Invoice(
        id = id, parentId = parentId, parentType = "CREDIT", amount = amount, paidAmount = paid,
        dueDate = issued + 10, issuedDate = issued, status = status,
        isReminder = reminder, relatedInvoiceId = related
    )

    @Test
    fun creditReductionNeverPushesDebtBelowZero() {
        // Paying an invoice that includes a late fee must not make used credit negative.
        assertEquals(500.0, InvoicePayments.creditReduction(usedCredit = 500.0, payment = 560.0), 0.0)
        assertEquals(200.0, InvoicePayments.creditReduction(usedCredit = 500.0, payment = 200.0), 0.0)
        assertEquals(0.0, InvoicePayments.creditReduction(usedCredit = -12.0, payment = 100.0), 0.0)
    }

    @Test
    fun paymentIsCappedAtRemainingAmount() {
        val inv = invoice(1, amount = 1000.0, paid = 400.0)
        assertEquals(600.0, InvoicePayments.cappedPayment(inv, 5000.0), 0.0)
        assertEquals(100.0, InvoicePayments.cappedPayment(inv, 100.0), 0.0)
        assertEquals(0.0, InvoicePayments.cappedPayment(inv, -50.0), 0.0)
    }

    @Test
    fun amountInputRoundTripsWithoutFloatingPointNoise() {
        val text = InvoicePayments.formatAmountInput(1234.5600000000002)
        assertEquals("1234.56", text)
        assertEquals("12345678.90", InvoicePayments.formatAmountInput(12345678.9))
    }

    @Test
    fun fullyPaidToleratesRounding() {
        assertTrue(InvoicePayments.isFullyPaid(100.0, 99.999))
        assertFalse(InvoicePayments.isFullyPaid(100.0, 99.9))
    }

    @Test
    fun reminderChainIsFollowedBothWays() {
        val original = invoice(1, status = InvoiceStatus.OVERDUE)
        val reminder1 = invoice(2, status = InvoiceStatus.OVERDUE, reminder = true, related = 1)
        val reminder2 = invoice(3, status = InvoiceStatus.REMINDER, reminder = true, related = 2)
        val unrelated = invoice(4, status = InvoiceStatus.PENDING)
        val all = listOf(original, reminder1, reminder2, unrelated)

        assertEquals(setOf(1, 2, 3), InvoiceChains.relatedChain(reminder2, all).map { it.id }.toSet())
        assertEquals(setOf(1, 2, 3), InvoiceChains.relatedChain(original, all).map { it.id }.toSet())
        assertEquals(setOf(4), InvoiceChains.relatedChain(unrelated, all).map { it.id }.toSet())
    }

    @Test
    fun unlinkedReminderFallsBackToEarlierOverdueInvoices() {
        val overdue = invoice(1, status = InvoiceStatus.OVERDUE, issued = 10)
        val pendingLater = invoice(2, status = InvoiceStatus.PENDING, issued = 30)
        val legacyReminder = invoice(3, status = InvoiceStatus.REMINDER, reminder = true, issued = 20)
        val otherCredit = invoice(4, status = InvoiceStatus.OVERDUE, issued = 5, parentId = 2)
        val all = listOf(overdue, pendingLater, legacyReminder, otherCredit)

        assertEquals(setOf(1, 3), InvoiceChains.relatedChain(legacyReminder, all).map { it.id }.toSet())
    }

    @Test
    fun outstandingHidesInvoicesCarriedForwardByAReminder() {
        val original = invoice(1, status = InvoiceStatus.OVERDUE)
        val reminder = invoice(2, status = InvoiceStatus.REMINDER, reminder = true, related = 1)
        val other = invoice(3)
        val paid = invoice(4, status = InvoiceStatus.PAID)

        assertEquals(listOf(2, 3), InvoiceChains.outstanding(listOf(original, reminder, other, paid)).map { it.id })
    }

    @Test
    fun creditDetailsHidePaidInvoicesUnlessRequested() {
        val unpaid = invoice(1)
        val paid = invoice(2, status = InvoiceStatus.PAID)
        val invoices = listOf(unpaid, paid)

        assertEquals(listOf(unpaid), InvoiceChains.forCreditDetails(invoices, showPaid = false))
        assertEquals(invoices, InvoiceChains.forCreditDetails(invoices, showPaid = true))
    }
}
