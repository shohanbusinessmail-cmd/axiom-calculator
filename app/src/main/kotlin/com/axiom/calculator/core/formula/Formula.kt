package com.axiom.calculator.core.formula

/** Top-level grouping shown as sections inside the Formulas tab (SECTION 9). */
enum class FormulaCategory(val displayName: String) {
    PERCENTAGE("Percentage"),
    RATIO("Ratios & Proportions"),
    FINANCE("Business & Finance"),
    EVERYDAY("Everyday"),
    GEOMETRY("Geometry"),
    PHYSICS("Physics & Engineering")
}

/** Declarative description of one input field for a formula. */
data class FormulaInputSpec(
    val key: String,
    val label: String,
    val unit: String? = null,
    val helpText: String? = null
)

/** Output of a successful formula computation. */
data class FormulaOutput(
    val label: String,
    val value: Double,
    val unit: String? = null
)

data class FormulaResult(
    val outputs: List<FormulaOutput>,
    val steps: List<String> = emptyList()
)

/**
 * A single formula calculator: stable [id], human metadata, a declared input
 * schema, and a pure [compute] function. UI composables read [inputs] to
 * render fields and call [compute]; they never implement calculation logic
 * themselves (SECTION 3 architecture boundary).
 */
interface Formula {
    val id: String
    val title: String
    val category: FormulaCategory
    val description: String
    val inputs: List<FormulaInputSpec>

    /** @throws com.axiom.calculator.core.math.AxMathError on invalid/undefined input. */
    fun compute(values: Map<String, Double>): FormulaResult

    fun requireValue(values: Map<String, Double>, key: String): Double =
        values[key] ?: throw com.axiom.calculator.core.math.AxMathError.InvalidInput("The field '$key' is required.")
}
