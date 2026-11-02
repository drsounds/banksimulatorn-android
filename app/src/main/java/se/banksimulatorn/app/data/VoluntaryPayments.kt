package se.banksimulatorn.app.data

object VoluntaryPayments {
    fun remainingDebtAfterPayment(debt: Double, payment: Double): Double = debt - payment
}
