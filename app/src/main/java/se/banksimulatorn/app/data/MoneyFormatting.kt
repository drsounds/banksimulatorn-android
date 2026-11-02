package se.banksimulatorn.app.data

import java.text.NumberFormat

fun NumberFormat.formatMoney(amount: Double): String =
    format(if (amount == 0.0) 0.0 else amount)
