package com.axiom.calculator.core.formula

/**
 * Central, searchable registry of every implemented formula (SECTION 9).
 * The Formulas tab UI reads exclusively from this object; it never
 * hardcodes a formula list of its own.
 */
object FormulaRegistry {
    val all: List<Formula> = PercentageFormulas.all + RatioFormulas.all + FinanceFormulas.all +
        EverydayFormulas.all + GeometryFormulas.all + PhysicsFormulas.all

    fun byCategory(category: FormulaCategory): List<Formula> = all.filter { it.category == category }

    fun byId(id: String): Formula? = all.find { it.id == id }

    fun search(query: String): List<Formula> {
        if (query.isBlank()) return all
        val q = query.trim().lowercase()
        return all.filter {
            it.title.lowercase().contains(q) || it.description.lowercase().contains(q) || it.category.displayName.lowercase().contains(q)
        }
    }

    init {
        val ids = all.map { it.id }
        check(ids.size == ids.toSet().size) { "Duplicate formula IDs detected in FormulaRegistry." }
    }
}
