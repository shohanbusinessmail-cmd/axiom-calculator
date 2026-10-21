package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError

object EverydayFormulas {

    val speed = object : Formula {
        override val id = "FOR-EVY-001"
        override val title = "Speed, distance, time"
        override val category = FormulaCategory.EVERYDAY
        override val description = "Speed = distance / time."
        override val inputs = listOf(FormulaInputSpec("distance", "Distance", "km"), FormulaInputSpec("time", "Time", "hours"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val d = requireValue(values, "distance"); val t = requireValue(values, "time")
            if (t == 0.0) throw AxMathError.Domain("Speed is undefined for zero time.")
            return FormulaResult(listOf(FormulaOutput("Speed", d / t, "km/h")))
        }
    }

    val unitPrice = object : Formula {
        override val id = "FOR-EVY-002"
        override val title = "Unit price"
        override val category = FormulaCategory.EVERYDAY
        override val description = "Unit price = total price / quantity."
        override val inputs = listOf(FormulaInputSpec("totalPrice", "Total price"), FormulaInputSpec("quantity", "Quantity"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val price = requireValue(values, "totalPrice"); val qty = requireValue(values, "quantity")
            if (qty == 0.0) throw AxMathError.Domain("Unit price is undefined for zero quantity.")
            return FormulaResult(listOf(FormulaOutput("Unit price", price / qty)))
        }
    }

    val weightedAverage = object : Formula {
        override val id = "FOR-EVY-003"
        override val title = "Weighted average of two values"
        override val category = FormulaCategory.EVERYDAY
        override val description = "Weighted average = (v1*w1 + v2*w2) / (w1 + w2)."
        override val inputs = listOf(
            FormulaInputSpec("v1", "Value 1"), FormulaInputSpec("w1", "Weight 1"),
            FormulaInputSpec("v2", "Value 2"), FormulaInputSpec("w2", "Weight 2")
        )
        override fun compute(values: Map<String, Double>): FormulaResult {
            val v1 = requireValue(values, "v1"); val w1 = requireValue(values, "w1")
            val v2 = requireValue(values, "v2"); val w2 = requireValue(values, "w2")
            if (w1 + w2 == 0.0) throw AxMathError.Domain("Weighted average is undefined when the total weight is zero.")
            return FormulaResult(listOf(FormulaOutput("Weighted average", (v1 * w1 + v2 * w2) / (w1 + w2))))
        }
    }

    val workRate = object : Formula {
        override val id = "FOR-EVY-004"
        override val title = "Combined work rate (two workers)"
        override val category = FormulaCategory.EVERYDAY
        override val description = "Time to complete a job together, given the time each worker takes alone: 1 / (1/t1 + 1/t2)."
        override val inputs = listOf(FormulaInputSpec("t1", "Time for worker 1 alone"), FormulaInputSpec("t2", "Time for worker 2 alone"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val t1 = requireValue(values, "t1"); val t2 = requireValue(values, "t2")
            if (t1 <= 0.0 || t2 <= 0.0) throw AxMathError.InvalidInput("Both work times must be positive.")
            val combined = 1.0 / (1.0 / t1 + 1.0 / t2)
            return FormulaResult(listOf(FormulaOutput("Combined time", combined)))
        }
    }

    val all: List<Formula> = listOf(speed, unitPrice, weightedAverage, workRate)
}
