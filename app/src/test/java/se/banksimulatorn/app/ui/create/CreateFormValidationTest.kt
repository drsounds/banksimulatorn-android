package se.banksimulatorn.app.ui.create

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class CreateFormValidationTest {

    @Test
    fun parseAmount_acceptsSwedishAndEnglishFormats() {
        assertEquals(1000.5, CreateFormValidation.parseAmount("1 000,50")!!, 0.0001)
        assertEquals(1000.5, CreateFormValidation.parseAmount("1000.50")!!, 0.0001)
        assertEquals(1000.0, CreateFormValidation.parseAmount("1 000")!!, 0.0001)
        assertNull(CreateFormValidation.parseAmount(""))
        assertNull(CreateFormValidation.parseAmount("abc"))
    }

    @Test
    fun name_isRequired() {
        assertEquals(FieldError.REQUIRED, CreateFormValidation.name("  "))
        assertNull(CreateFormValidation.name("Sparkonto"))
    }

    @Test
    fun positiveAmount_rejectsBlankZeroAndNegative() {
        assertEquals(FieldError.REQUIRED, CreateFormValidation.positiveAmount(""))
        assertEquals(FieldError.MUST_BE_POSITIVE, CreateFormValidation.positiveAmount("0"))
        assertEquals(FieldError.MUST_BE_POSITIVE, CreateFormValidation.positiveAmount("-5"))
        assertEquals(FieldError.INVALID_NUMBER, CreateFormValidation.positiveAmount("1,2,3"))
        assertNull(CreateFormValidation.positiveAmount("25 000"))
    }

    @Test
    fun optionalAmount_allowsBlankButNotNegative() {
        assertNull(CreateFormValidation.optionalNonNegativeAmount(""))
        assertNull(CreateFormValidation.optionalNonNegativeAmount("0"))
        assertEquals(FieldError.MUST_NOT_BE_NEGATIVE, CreateFormValidation.optionalNonNegativeAmount("-1"))
    }

    @Test
    fun optionalRate_mustBeBetweenZeroAndHundred() {
        assertNull(CreateFormValidation.optionalRate(""))
        assertNull(CreateFormValidation.optionalRate("15,5"))
        assertEquals(FieldError.RATE_OUT_OF_RANGE, CreateFormValidation.optionalRate("101"))
        assertEquals(FieldError.RATE_OUT_OF_RANGE, CreateFormValidation.optionalRate("-1"))
    }

    @Test
    fun dayOfMonth_mustBeOneToTwentyEight() {
        assertNull(CreateFormValidation.dayOfMonth("1"))
        assertNull(CreateFormValidation.dayOfMonth("28"))
        assertEquals(FieldError.DAY_OUT_OF_RANGE, CreateFormValidation.dayOfMonth("0"))
        assertEquals(FieldError.DAY_OUT_OF_RANGE, CreateFormValidation.dayOfMonth("31"))
        assertEquals(FieldError.INVALID_NUMBER, CreateFormValidation.dayOfMonth("x"))
        assertEquals(FieldError.REQUIRED, CreateFormValidation.dayOfMonth(""))
    }

    @Test
    fun loanMonths_mustBeInRange() {
        assertNull(CreateFormValidation.loanMonths("60"))
        assertEquals(FieldError.MONTHS_OUT_OF_RANGE, CreateFormValidation.loanMonths("0"))
        assertEquals(FieldError.MONTHS_OUT_OF_RANGE, CreateFormValidation.loanMonths("601"))
    }

    @Test
    fun nextDateOnDay_rollsOverToNextMonthWhenDayHasPassed() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val nov2 = format.parse("2026-11-02")!!.time
        assertEquals("2026-11-27", nextDateOnDay(nov2, 27))
        assertEquals("2026-11-02", nextDateOnDay(nov2, 2))
        assertEquals("2026-12-01", nextDateOnDay(nov2, 1))
        val dec30 = format.parse("2026-12-30")!!.time
        assertEquals("2027-01-25", nextDateOnDay(dec30, 25))
    }

    @Test
    fun generatedNumbers_haveExpectedShape() {
        val random = Random(42)
        assertTrue(Regex("""8\d{3}-\d \d{3} \d{3} \d{3}-\d""").matches(generateAccountNumber(random)))
        assertTrue(Regex("""4242-\d{4}-\d{4}-\d{4}""").matches(generateCardNumber(random)))
    }
}
