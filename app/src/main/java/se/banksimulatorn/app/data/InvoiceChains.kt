package se.banksimulatorn.app.data

/**
 * An overdue invoice gets a reminder, and an overdue reminder gets another one.
 * Each reminder carries the remaining debt forward, so the invoices in such a
 * chain all describe the same debt. These helpers keep them consistent.
 */
object InvoiceChains {

    private val overdueStatuses = setOf(InvoiceStatus.OVERDUE, InvoiceStatus.COLLECTION)

    /**
     * Every invoice that belongs to the same debt as [invoice]: the invoices it was
     * issued for and the reminders issued for it, followed in both directions.
     * [invoice] itself is included.
     *
     * Reminders created before reminders were linked have no [Invoice.relatedInvoiceId].
     * For those, every earlier overdue invoice for the same product counts as related,
     * since that is what such a reminder carried forward.
     */
    fun relatedChain(invoice: Invoice, all: List<Invoice>): List<Invoice> {
        val byId = all.associateBy { it.id }
        val result = linkedMapOf(invoice.id to invoice)
        val queue = ArrayDeque<Invoice>().apply { add(invoice) }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val neighbours = buildList {
                current.relatedInvoiceId?.let { byId[it] }?.let { add(it) }
                addAll(all.filter { it.relatedInvoiceId == current.id })
                if (current.isReminder && current.relatedInvoiceId == null) {
                    addAll(all.filter {
                        it.id != current.id &&
                            it.parentId == current.parentId &&
                            it.parentType == current.parentType &&
                            it.status in overdueStatuses &&
                            it.issuedDate < current.issuedDate
                    })
                }
            }
            neighbours.forEach { n ->
                if (n.deletedAt == null && result.putIfAbsent(n.id, n) == null) queue.add(n)
            }
        }
        return result.values.toList()
    }

    /**
     * The open invoices the user actually owes: an invoice that already has an open
     * reminder is left out, because the reminder carries its remaining amount and
     * paying the reminder settles both.
     */
    fun outstanding(openInvoices: List<Invoice>): List<Invoice> {
        val open = openInvoices.filter { it.status != InvoiceStatus.PAID && it.deletedAt == null }
        val superseded = open.mapNotNull { it.relatedInvoiceId }.toSet()
        return open.filter { it.id !in superseded }
    }

    fun forCreditDetails(invoices: List<Invoice>, showPaid: Boolean): List<Invoice> =
        if (showPaid) invoices else invoices.filter { it.status != InvoiceStatus.PAID }

    /** What is left to pay on [invoice], never negative. */
    fun remaining(invoice: Invoice): Double = (invoice.amount - invoice.paidAmount).coerceAtLeast(0.0)
}
