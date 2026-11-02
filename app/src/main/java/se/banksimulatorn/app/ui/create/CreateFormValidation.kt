package se.banksimulatorn.app.ui.create

/**
 * Validation for the "Open new" forms. Kept free of Android and Compose types so the
 * rules can be unit tested on the JVM.
 */
enum class FieldError {
    REQUIRED,
    INVALID_NUMBER,
    MUST_BE_POSITIVE,
    MUST_NOT_BE_NEGATIVE,
    RATE_OUT_OF_RANGE,
    DAY_OUT_OF_RANGE,
    MONTHS_OUT_OF_RANGE
}

object CreateFormValidation {
    const val MIN_DAY = 1

    // The time machine bills on a fixed day of the month, so days that don't exist
    // in every month (29-31) would silently skip February.
    const val MAX_DAY = 28
    const val MAX_RATE = 100.0
    const val MAX_LOAN_MONTHS = 600

    /**
     * Parses an amount the way people type it: "1 000,50", "1000.50" and "1000" all
     * work. Returns null for blank or unparsable input.
     */
    fun parseAmount(input: String): Double? {
        val cleaned = input
            .replace(" ", "")
            .replace(" ", "")
            .replace(" ", "")
            .replace(",", ".")
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    fun name(input: String): FieldError? =
        if (input.isBlank()) FieldError.REQUIRED else null

    /** A blank value is allowed and means zero. */
    fun optionalNonNegativeAmount(input: String): FieldError? {
        if (input.isBlank()) return null
        val value = parseAmount(input) ?: return FieldError.INVALID_NUMBER
        return if (value < 0) FieldError.MUST_NOT_BE_NEGATIVE else null
    }

    fun positiveAmount(input: String): FieldError? {
        if (input.isBlank()) return FieldError.REQUIRED
        val value = parseAmount(input) ?: return FieldError.INVALID_NUMBER
        return if (value <= 0) FieldError.MUST_BE_POSITIVE else null
    }

    /** A blank value is allowed and means 0 %. */
    fun optionalRate(input: String): FieldError? {
        if (input.isBlank()) return null
        val value = parseAmount(input) ?: return FieldError.INVALID_NUMBER
        return if (value < 0 || value > MAX_RATE) FieldError.RATE_OUT_OF_RANGE else null
    }

    fun dayOfMonth(input: String): FieldError? {
        if (input.isBlank()) return FieldError.REQUIRED
        val value = input.trim().toIntOrNull() ?: return FieldError.INVALID_NUMBER
        return if (value < MIN_DAY || value > MAX_DAY) FieldError.DAY_OUT_OF_RANGE else null
    }

    fun loanMonths(input: String): FieldError? {
        if (input.isBlank()) return FieldError.REQUIRED
        val value = input.trim().toIntOrNull() ?: return FieldError.INVALID_NUMBER
        return if (value < 1 || value > MAX_LOAN_MONTHS) FieldError.MONTHS_OUT_OF_RANGE else null
    }
}
