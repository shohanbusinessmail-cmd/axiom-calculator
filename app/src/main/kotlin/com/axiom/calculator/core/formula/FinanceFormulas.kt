package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError
import kotlin.math.pow

/**
 * Finance formulas (FOR-FIN-xxx). Conventions, documented per SECTION 9:
 *  - Interest rates are entered as annual percentages (e.g. 5 means 5%).
 *  - EMI uses a fixed MONTHLY rate (already divided down from an annual
 *    rate by the caller/UI) and `n` monthly payments:
 *    EMI = P * r * (1+r)^n / ((1+r)^n - 1), with the r = 0 special case
 *    EMI = P / n.
 *  - Compound interest compounds once per year unless otherwise noted; this
 *    keeps the UI inputs simple and matches the F002 mandatory vector.
 *  - All monetary outputs are plain numbers; currency symbols are a
 *    presentation concern handled by the UI layer, not the formula engine.
 */
object FinanceFormulas {

    val simpleInterest = object : Formula {
        override val id = "FOR-FIN-001"
        override val title = "Simple interest"
        override val category = FormulaCategory.FINANCE
        override val description = "Computes simple interest and the total amount for a principal, annual rate, and time in years."
        override val inputs = listOf(
            FormulaInputSpec("principal", "Principal"),
            FormulaInputSpec("rate", "Annual rate", "%"),
            FormulaInputSpec("years", "Time", "years")
        )
        override fun compute(values: Map<String, Double>): FormulaResult {
            val p = requireValue(values, "principal"); val r = requireValue(values, "rate"); val t = requireValue(values, "years")
            val interest = p * (r / 100.0) * t
            return FormulaResult(listOf(FormulaOutput("Interest", interest), FormulaOutput("Total amount", p + interest)))
        }
    }

    val compoundInterest = object : Formula {
        override val id = "FOR-FIN-002"
        override val title = "Compound interest (annual compounding)"
        override val category = FormulaCategory.FINANCE
        override val description = "Computes compound interest with annual compounding for a principal, annual rate, and time in years."
        override val inputs = listOf(
            FormulaInputSpec("principal", "Principal"),
            FormulaInputSpec("rate", "Annual rate", "%"),
            FormulaInputSpec("years", "Time", "years")
        )
        override fun compute(values: Map<String, Double>): FormulaResult {
            val p = requireValue(values, "principal"); val r = requireValue(values, "rate"); val t = requireValue(values, "years")
            val total = p * (1 + r / 100.0).pow(t)
            return FormulaResult(listOf(FormulaOutput("Interest", total - p), FormulaOutput("Total amount", total)))
        }
    }

    val emi = object : Formula {
        override val id = "FOR-FIN-003"
        override val title = "Loan EMI"
        override val category = FormulaCategory.FINANCE
        override val description = "Computes the equal monthly installment for a principal, monthly interest rate, and number of payments. EMI = P*r*(1+r)^n / ((1+r)^n - 1); r=0 gives EMI = P/n."
        override val inputs = listOf(
            FormulaInputSpec("principal", "Principal"),
            FormulaInputSpec("monthlyRate", "Monthly rate", "fraction, e.g. 0.01 for 1%"),
            FormulaInputSpec("payments", "Number of monthly payments")
        )
        override fun compute(values: Map<String, Double>): FormulaResult {
            val p = requireValue(values, "principal"); val r = requireValue(values, "monthlyRate"); val n = requireValue(values, "payments")
            if (n <= 0) throw AxMathError.InvalidInput("The number of payments must be positive.")
            val monthly = if (r == 0.0) p / n else {
                val factor = (1 + r).pow(n)
                p * r * factor / (factor - 1)
            }
            return FormulaResult(
                listOf(FormulaOutput("Monthly payment", monthly), FormulaOutput("Total paid", monthly * n), FormulaOutput("Total interest", monthly * n - p))
            )
        }
    }

    val profitMargin = object : Formula {
        override val id = "FOR-FIN-004"
        override val title = "Profit margin"
        override val category = FormulaCategory.FINANCE
        override val description = "Profit margin relative to revenue: (revenue - cost) / revenue * 100."
        override val inputs = listOf(FormulaInputSpec("revenue", "Revenue"), FormulaInputSpec("cost", "Cost"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val revenue = requireValue(values, "revenue"); val cost = requireValue(values, "cost")
            if (revenue == 0.0) throw AxMathError.Domain("Profit margin is undefined for zero revenue.")
            val margin = (revenue - cost) / revenue * 100.0
            return FormulaResult(listOf(FormulaOutput("Profit margin", margin, "%"), FormulaOutput("Profit", revenue - cost)))
        }
    }

    val markupPercent = object : Formula {
        override val id = "FOR-FIN-005"
        override val title = "Markup percentage"
        override val category = FormulaCategory.FINANCE
        override val description = "Markup relative to cost: (sellingPrice - cost) / cost * 100."
        override val inputs = listOf(FormulaInputSpec("cost", "Cost"), FormulaInputSpec("sellingPrice", "Selling price"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val cost = requireValue(values, "cost"); val sp = requireValue(values, "sellingPrice")
            if (cost == 0.0) throw AxMathError.Domain("Markup percentage is undefined for zero cost.")
            val markup = (sp - cost) / cost * 100.0
            return FormulaResult(listOf(FormulaOutput("Markup", markup, "%")))
        }
    }

    val all: List<Formula> = listOf(simpleInterest, compoundInterest, emi, profitMargin, markupPercent)
}
