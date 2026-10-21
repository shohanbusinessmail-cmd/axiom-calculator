package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError
import com.axiom.calculator.core.solve.NumberTheory

object RatioFormulas {

    val simplifyRatio = object : Formula {
        override val id = "FOR-RATIO-001"
        override val title = "Simplify a ratio"
        override val category = FormulaCategory.RATIO
        override val description = "Reduces a:b to lowest terms using the greatest common divisor."
        override val inputs = listOf(FormulaInputSpec("a", "A"), FormulaInputSpec("b", "B"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val a = requireValue(values, "a"); val b = requireValue(values, "b")
            if (a != Math.floor(a) || b != Math.floor(b)) throw AxMathError.InvalidInput("Ratio simplification requires whole numbers.")
            if (a == 0.0 && b == 0.0) throw AxMathError.Domain("A 0:0 ratio cannot be simplified.")
            val g = NumberTheory.gcd(a.toLong(), b.toLong()).let { if (it == 0L) 1L else it }
            return FormulaResult(listOf(FormulaOutput("Simplified A", a.toLong() / g.toDouble()), FormulaOutput("Simplified B", b.toLong() / g.toDouble())))
        }
    }

    val solveProportion = object : Formula {
        override val id = "FOR-RATIO-002"
        override val title = "Solve a proportion (a/b = c/x)"
        override val category = FormulaCategory.RATIO
        override val description = "Solves for the unknown x in a/b = c/x."
        override val inputs = listOf(FormulaInputSpec("a", "A"), FormulaInputSpec("b", "B"), FormulaInputSpec("c", "C"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val a = requireValue(values, "a"); val b = requireValue(values, "b"); val c = requireValue(values, "c")
            if (a == 0.0) throw AxMathError.Domain("A proportion a/b = c/x is undefined when a = 0.")
            val x = c * b / a
            return FormulaResult(listOf(FormulaOutput("X", x)))
        }
    }

    val splitByRatio = object : Formula {
        override val id = "FOR-RATIO-003"
        override val title = "Split a quantity by ratio"
        override val category = FormulaCategory.RATIO
        override val description = "Splits a total quantity into two parts according to a ratio a:b."
        override val inputs = listOf(FormulaInputSpec("total", "Total quantity"), FormulaInputSpec("a", "Ratio part A"), FormulaInputSpec("b", "Ratio part B"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val total = requireValue(values, "total"); val a = requireValue(values, "a"); val b = requireValue(values, "b")
            if (a + b == 0.0) throw AxMathError.Domain("The ratio parts cannot sum to zero.")
            val shareA = total * a / (a + b)
            return FormulaResult(listOf(FormulaOutput("Part A", shareA), FormulaOutput("Part B", total - shareA)))
        }
    }

    val scaleRatio = object : Formula {
        override val id = "FOR-RATIO-004"
        override val title = "Scale a ratio"
        override val category = FormulaCategory.RATIO
        override val description = "Scales a:b by a given factor."
        override val inputs = listOf(FormulaInputSpec("a", "A"), FormulaInputSpec("b", "B"), FormulaInputSpec("factor", "Scale factor"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val a = requireValue(values, "a"); val b = requireValue(values, "b"); val factor = requireValue(values, "factor")
            return FormulaResult(listOf(FormulaOutput("Scaled A", a * factor), FormulaOutput("Scaled B", b * factor)))
        }
    }

    val all: List<Formula> = listOf(simplifyRatio, solveProportion, splitByRatio, scaleRatio)
}
