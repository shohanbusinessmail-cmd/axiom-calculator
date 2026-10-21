package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError

/**
 * Percentage formula family (FOR-PCT-xxx). Standard-calculator percentage
 * semantics (DecimalArithmetic.contextualPercent) are intentionally separate
 * from these explicit formulas, per SECTION 6/9's requirement to not confuse
 * the two.
 */
object PercentageFormulas {

    val xPercentOfY = object : Formula {
        override val id = "FOR-PCT-001"
        override val title = "X percent of Y"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes X% of a value Y."
        override val inputs = listOf(
            FormulaInputSpec("x", "Percentage (X)", "%"),
            FormulaInputSpec("y", "Value (Y)")
        )
        override fun compute(values: Map<String, Double>): FormulaResult {
            val x = requireValue(values, "x"); val y = requireValue(values, "y")
            val result = x / 100.0 * y
            return FormulaResult(listOf(FormulaOutput("Result", result)), listOf("$x% of $y = ($x / 100) * $y = $result"))
        }
    }

    val xIsWhatPercentOfY = object : Formula {
        override val id = "FOR-PCT-002"
        override val title = "X is what percent of Y"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes what percentage X represents of Y."
        override val inputs = listOf(FormulaInputSpec("x", "Part (X)"), FormulaInputSpec("y", "Whole (Y)"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val x = requireValue(values, "x"); val y = requireValue(values, "y")
            if (y == 0.0) throw AxMathError.Domain("Percentage of a zero whole is undefined.")
            val result = x / y * 100.0
            return FormulaResult(listOf(FormulaOutput("Percentage", result, "%")), listOf("($x / $y) * 100 = $result%"))
        }
    }

    val percentageIncrease = object : Formula {
        override val id = "FOR-PCT-003"
        override val title = "Percentage increase"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Increases a base value by a given percentage."
        override val inputs = listOf(FormulaInputSpec("base", "Base value"), FormulaInputSpec("pct", "Increase", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val base = requireValue(values, "base"); val pct = requireValue(values, "pct")
            val result = base * (1 + pct / 100.0)
            return FormulaResult(listOf(FormulaOutput("New value", result)))
        }
    }

    val percentageDecrease = object : Formula {
        override val id = "FOR-PCT-004"
        override val title = "Percentage decrease"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Decreases a base value by a given percentage."
        override val inputs = listOf(FormulaInputSpec("base", "Base value"), FormulaInputSpec("pct", "Decrease", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val base = requireValue(values, "base"); val pct = requireValue(values, "pct")
            val result = base * (1 - pct / 100.0)
            return FormulaResult(listOf(FormulaOutput("New value", result)))
        }
    }

    val percentageChange = object : Formula {
        override val id = "FOR-PCT-005"
        override val title = "Percentage change"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes the percentage change between an initial and a final value."
        override val inputs = listOf(FormulaInputSpec("from", "Initial value"), FormulaInputSpec("to", "Final value"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val from = requireValue(values, "from"); val to = requireValue(values, "to")
            if (from == 0.0) throw AxMathError.Domain("Percentage change from zero is undefined.")
            val result = (to - from) / from * 100.0
            return FormulaResult(listOf(FormulaOutput("Change", result, "%")))
        }
    }

    val reversePercentage = object : Formula {
        override val id = "FOR-PCT-006"
        override val title = "Reverse percentage (find original value)"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Given a final value and the percentage change applied to reach it, finds the original value."
        override val inputs = listOf(FormulaInputSpec("final", "Final value"), FormulaInputSpec("pct", "Percentage change applied", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val final = requireValue(values, "final"); val pct = requireValue(values, "pct")
            val factor = 1 + pct / 100.0
            if (factor == 0.0) throw AxMathError.Domain("Reverse percentage is undefined for a -100% change.")
            val original = final / factor
            return FormulaResult(listOf(FormulaOutput("Original value", original)))
        }
    }

    val discount = object : Formula {
        override val id = "FOR-PCT-007"
        override val title = "Discount"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes the discount amount and final price for a given price and discount percentage."
        override val inputs = listOf(FormulaInputSpec("price", "Original price"), FormulaInputSpec("pct", "Discount", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val price = requireValue(values, "price"); val pct = requireValue(values, "pct")
            val amount = price * pct / 100.0
            return FormulaResult(listOf(FormulaOutput("Discount amount", amount), FormulaOutput("Final price", price - amount)))
        }
    }

    val markup = object : Formula {
        override val id = "FOR-PCT-008"
        override val title = "Markup (selling price from cost)"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes the selling price given a cost and a markup percentage over cost. Markup is relative to COST, not selling price (distinct from margin)."
        override val inputs = listOf(FormulaInputSpec("cost", "Cost"), FormulaInputSpec("pct", "Markup", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val cost = requireValue(values, "cost"); val pct = requireValue(values, "pct")
            val sellingPrice = cost * (1 + pct / 100.0)
            return FormulaResult(listOf(FormulaOutput("Selling price", sellingPrice)))
        }
    }

    val marginToCost = object : Formula {
        override val id = "FOR-PCT-009"
        override val title = "Margin (cost from selling price)"
        override val category = FormulaCategory.PERCENTAGE
        override val description = "Computes the cost given a selling price and a profit margin percentage relative to the SELLING PRICE (distinct from markup, which is relative to cost)."
        override val inputs = listOf(FormulaInputSpec("sellingPrice", "Selling price"), FormulaInputSpec("pct", "Margin", "%"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val sp = requireValue(values, "sellingPrice"); val pct = requireValue(values, "pct")
            val cost = sp * (1 - pct / 100.0)
            return FormulaResult(listOf(FormulaOutput("Cost", cost)))
        }
    }

    val all: List<Formula> = listOf(
        xPercentOfY, xIsWhatPercentOfY, percentageIncrease, percentageDecrease,
        percentageChange, reversePercentage, discount, markup, marginToCost
    )
}
