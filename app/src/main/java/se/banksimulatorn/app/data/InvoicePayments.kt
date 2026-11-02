package se.banksimulatorn.app.data

import java.util.Locale

/** Plain arithmetic behind invoice payments, kept apart so it can be unit tested. */
object InvoicePayments {

    /** Amounts closer than this are treated as equal (half an öre/cent). */
    private const val EPSILON = 0.005

    /** The payment actually taken: never more than what is left on the invoice. */
    fun cappedPayment(invoice: Invoice, requested: Double): Double =
        requested.coerceAtMost(InvoiceChains.remaining(invoice)).coerceAtLeast(0.0)

    /**
     * How much a payment lowers a credit line's used credit. Late fees and late
     * interest are added to the invoice but not to the used credit, so a payment can
     * be larger than the debt; the debt must then stop at zero instead of going negative.
     */
    fun creditReduction(usedCredit: Double, payment: Double): Double =
        payment.coerceAtMost(usedCredit.coerceAtLeast(0.0)).coerceAtLeast(0.0)

    fun isFullyPaid(amount: Double, paidAmount: Double): Boolean = paidAmount >= amount - EPSILON

    /**
     * The text pre-filled in the payment field. Double.toString() gives values like
     * "1234.5600000000002" or "1.2E7", which the field then parsed into a different amount.
     */
    fun formatAmountInput(amount: Double): String = String.format(Locale.ROOT, "%.2f", amount)
}
