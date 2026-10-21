package com.axiom.calculator.feature.quicksolve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.core.math.AxMathError
import com.axiom.calculator.core.solve.*
import com.axiom.calculator.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QuickSolveMode(val label: String) {
    LINEAR("Linear equation"),
    QUADRATIC("Quadratic equation"),
    SYSTEM_2X2("2-variable system"),
    NUMBER_THEORY("GCD / LCM / Primes"),
    SEQUENCE("Sequences"),
    STATISTICS("Statistics"),
    MATRIX("Matrix (2x2)")
}

data class QuickSolveUiState(
    val mode: QuickSolveMode = QuickSolveMode.LINEAR,
    val fields: Map<String, String> = emptyMap(),
    val resultLines: List<String> = emptyList(),
    val errorMessage: String? = null
)

class QuickSolveViewModel(private val historyRepository: HistoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickSolveUiState())
    val uiState: StateFlow<QuickSolveUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: QuickSolveMode) {
        _uiState.value = QuickSolveUiState(mode = mode)
    }

    fun onFieldChange(key: String, value: String) {
        _uiState.value = _uiState.value.copy(fields = _uiState.value.fields + (key to value))
    }

    private fun num(key: String): Double =
        _uiState.value.fields[key]?.toDoubleOrNull() ?: throw AxMathError.InvalidInput("Please enter a valid number for '$key'.")

    private fun intNum(key: String): Long =
        _uiState.value.fields[key]?.toLongOrNull() ?: throw AxMathError.InvalidInput("Please enter a valid whole number for '$key'.")

    fun onSolve() {
        try {
            val lines = when (_uiState.value.mode) {
                QuickSolveMode.LINEAR -> solveLinear()
                QuickSolveMode.QUADRATIC -> solveQuadratic()
                QuickSolveMode.SYSTEM_2X2 -> solveSystem()
                QuickSolveMode.NUMBER_THEORY -> solveNumberTheory()
                QuickSolveMode.SEQUENCE -> solveSequence()
                QuickSolveMode.STATISTICS -> solveStatistics()
                QuickSolveMode.MATRIX -> solveMatrix()
            }
            _uiState.value = _uiState.value.copy(resultLines = lines, errorMessage = null)
            viewModelScope.launch {
                historyRepository.record(
                    input = "${_uiState.value.mode.label}: ${_uiState.value.fields}",
                    result = lines.joinToString(" | "),
                    calculationType = "SOLVE:${_uiState.value.mode.name}"
                )
            }
        } catch (e: AxMathError) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message, resultLines = emptyList())
        }
    }

    private fun solveLinear(): List<String> {
        val a = num("a"); val b = num("b")
        return when (val r = EquationSolver.solveLinear(a, b)) {
            is LinearResult.Unique -> listOf("x = ${r.x}")
            LinearResult.NoSolution -> listOf("No solution exists (the equation is contradictory).")
            LinearResult.InfiniteSolutions -> listOf("Infinitely many solutions (every real number satisfies the equation).")
        }
    }

    private fun solveQuadratic(): List<String> {
        val a = num("a"); val b = num("b"); val c = num("c")
        val real = EquationSolver.solveQuadraticReal(a, b, c)
        return when (real) {
            is QuadraticRealResult.TwoRoots -> listOf("x1 = ${real.x1}", "x2 = ${real.x2}")
            is QuadraticRealResult.OneRoot -> listOf("x = ${real.x} (repeated root)")
            QuadraticRealResult.NoRealRoots -> {
                val complex = EquationSolver.solveQuadraticComplex(a, b, c)
                listOf(
                    "No real roots.",
                    "Complex roots: x1 = ${complex.x1.format()}, x2 = ${complex.x2.format()}"
                )
            }
        }
    }

    private fun solveSystem(): List<String> {
        val a1 = num("a1"); val b1 = num("b1"); val c1 = num("c1")
        val a2 = num("a2"); val b2 = num("b2"); val c2 = num("c2")
        return when (val r = EquationSolver.solveSystem2x2(a1, b1, c1, a2, b2, c2)) {
            is System2x2Result.Unique -> listOf("x = ${r.x}", "y = ${r.y}")
            System2x2Result.Singular -> listOf("The system is singular: no unique solution exists (no solution or infinitely many).")
        }
    }

    private fun solveNumberTheory(): List<String> {
        val a = intNum("a"); val b = intNum("b")
        val gcd = NumberTheory.gcd(a, b)
        val lcm = NumberTheory.lcm(a, b)
        val lines = mutableListOf("GCD($a, $b) = $gcd", "LCM($a, $b) = $lcm")
        if (a in 2..1_000_000_000) {
            val factors = NumberTheory.primeFactorize(a)
            lines += "Prime factorization of $a: " + factors.joinToString(" * ") { if (it.exponent > 1) "${it.prime}^${it.exponent}" else "${it.prime}" }
            lines += "$a is ${if (NumberTheory.isPrime(a)) "prime" else "not prime"}."
        }
        return lines
    }

    private fun solveSequence(): List<String> {
        val a1 = num("a1"); val d = num("d"); val n = intNum("n").toInt()
        val isGeometric = _uiState.value.fields["type"] == "geometric"
        return if (isGeometric) {
            listOf(
                "Term $n = ${Sequences.geometricNthTerm(a1, d, n)}",
                "Sum of first $n terms = ${Sequences.geometricSum(a1, d, n)}"
            )
        } else {
            listOf(
                "Term $n = ${Sequences.arithmeticNthTerm(a1, d, n)}",
                "Sum of first $n terms = ${Sequences.arithmeticSum(a1, d, n)}"
            )
        }
    }

    private fun solveStatistics(): List<String> {
        val raw = _uiState.value.fields["data"] ?: throw AxMathError.InvalidInput("Enter comma-separated numbers.")
        val data = raw.split(",", " ").filter { it.isNotBlank() }.map {
            it.trim().toDoubleOrNull() ?: throw AxMathError.InvalidInput("'$it' is not a valid number.")
        }
        val s = Statistics.summarize(data)
        return listOf(
            "Count = ${s.count}", "Sum = ${s.sum}", "Mean = ${s.mean}", "Median = ${s.median}",
            "Min = ${s.min}", "Max = ${s.max}", "Range = ${s.range}",
            "Population variance = ${s.populationVariance}", "Population std dev = ${s.populationStdDev}",
            "Sample variance = ${s.sampleVariance ?: "undefined (n=1)"}", "Sample std dev = ${s.sampleStdDev ?: "undefined (n=1)"}",
            "Modes = ${if (s.modes.isEmpty()) "none" else s.modes.joinToString()}",
            "Q1 = ${s.q1}", "Q3 = ${s.q3}"
        )
    }

    private fun solveMatrix(): List<String> {
        val a = AxMatrix.of(doubleArrayOf(num("a11"), num("a12")), doubleArrayOf(num("a21"), num("a22")))
        val det = a.determinant()
        val lines = mutableListOf("Determinant = $det", "Transpose = ${a.transpose().toRows().joinToString()}")
        lines += if (det == 0.0) {
            "The matrix is singular and has no inverse."
        } else {
            "Inverse = ${a.inverse().toRows().joinToString()}"
        }
        return lines
    }
}
