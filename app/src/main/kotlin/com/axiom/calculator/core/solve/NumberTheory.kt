package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError
import java.math.BigInteger

data class PrimeFactor(val prime: Long, val exponent: Int)

object NumberTheory {
    private const val MAX_INPUT = 1_000_000_000_000L

    fun gcd(a: Long, b: Long): Long = BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).toLong()

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return Math.abs(a / gcd(a, b) * b)
    }

    fun isPrime(n: Long): Boolean {
        if (n < 2) return false
        if (n < 4) return true
        if (n % 2 == 0L) return false
        var i = 3L
        while (i * i <= n) {
            if (n % i == 0L) return false
            i += 2
        }
        return true
    }

    fun primeFactorize(nInput: Long): List<PrimeFactor> {
        if (nInput < 2) throw AxMathError.Domain("Prime factorization requires an integer of 2 or greater.")
        if (nInput > MAX_INPUT) throw AxMathError.ResourceLimit("The number is too large for factorization in this engine.")
        var n = nInput
        val factors = mutableListOf<PrimeFactor>()
        var d = 2L
        while (d * d <= n) {
            var count = 0
            while (n % d == 0L) { n /= d; count++ }
            if (count > 0) factors += PrimeFactor(d, count)
            d++
        }
        if (n > 1) factors += PrimeFactor(n, 1)
        return factors
    }

    fun divisors(n: Long): List<Long> {
        if (n < 1) throw AxMathError.Domain("Divisors require a positive integer.")
        val result = sortedSetOf<Long>()
        var i = 1L
        while (i * i <= n) {
            if (n % i == 0L) { result += i; result += n / i }
            i++
        }
        return result.toList()
    }
}
