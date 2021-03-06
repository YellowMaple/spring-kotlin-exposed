package com.example.testutils.assertions

import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldEqual
import org.assertj.core.api.Assertions
import org.assertj.core.util.BigDecimalComparator
import org.assertj.core.util.DoubleComparator
import org.junit.Assert
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.min

infix fun Instant.shouldBeGreaterThan(expected: Instant) = this.apply { Assert.assertTrue("Expected $this to be greater than $expected", this > expected) }
infix fun Instant.`should be greater than`(expected: Instant) = this.shouldBeGreaterThan(expected)

infix fun Instant.shouldBeBetween(expected: Pair<Instant, Instant>) = this.apply { Assert.assertTrue("Expected $this to be between $expected", this >= expected.first && this <= expected.second) }
infix fun Instant.shouldEqualInstant(other: Instant?) {
    this.truncatedTo(ChronoUnit.MILLIS) shouldEqual other?.truncatedTo(ChronoUnit.MILLIS)
}

infix fun Any?.shouldEqualRecursively(other: Any?) = Assertions
        .assertThat(this)
        .usingComparatorForType(DoubleComparator(0.01), Double::class.java)
        .usingComparatorForType({ o1, o2 ->
            o1.truncatedTo(ChronoUnit.MILLIS).compareTo(o2.truncatedTo(ChronoUnit.MILLIS))
        }, Instant::class.java)
        .usingComparatorForType(object : BigDecimalComparator() {
            private val roundingMode = RoundingMode.HALF_EVEN // banker's rounding," - the rounding policy used for {@code float} and {@code double}
            override fun compareNonNull(number1: BigDecimal, number2: BigDecimal): Int {
                val scale = min(number1.scale(), number2.scale())

                val n1 = number1.rounded(scale = scale, roundingMode = roundingMode)
                val n2 = number2.rounded(scale = scale, roundingMode = roundingMode)
                val r2 = n1.compareTo(n2)
                return r2
            }
        }, BigDecimal::class.java)
        .isEqualToComparingFieldByFieldRecursively(other)

inline fun <reified T : Any> T?.shouldNotNull(): T {
    Assert.assertTrue("Expected ${T::class.java.canonicalName} to be not null", this != null)
    return this!!
}

private fun BigDecimal.rounded(scale: Int, roundingMode: RoundingMode = RoundingMode.HALF_EVEN): BigDecimal {
    return setScale(scale, roundingMode)
}

fun softAssertAll(heading: String?, assertions: List<() -> Any?>) {
    val executables: List<() -> Unit> = assertions.map {
        val exe: () -> Unit = { it() }
        exe
    }
    assertAll(heading, executables.stream())
}

infix fun Double.shouldBeInRange(r: ClosedFloatingPointRange<Double>): Double = shouldBeInRange(r.start, r.endInclusive)

