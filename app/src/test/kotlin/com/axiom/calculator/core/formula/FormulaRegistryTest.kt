package com.axiom.calculator.core.formula

import org.junit.Assert.*
import org.junit.Test

class FormulaRegistryTest {
    @Test fun allFormulaIdsAreUnique() {
        val ids = FormulaRegistry.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun everyCategoryHasAtLeastOneFormula() {
        FormulaCategory.entries.forEach { category ->
            assertTrue("Category $category has no formulas", FormulaRegistry.byCategory(category).isNotEmpty())
        }
    }

    // P001: 20% of 250 = 50
    @Test fun p001_percentOf() {
        val r = PercentageFormulas.xPercentOfY.compute(mapOf("x" to 20.0, "y" to 250.0))
        assertEquals(50.0, r.outputs.first().value, 1e-9)
    }

    // P002: 15 is what % of 60 -> 25%
    @Test fun p002_whatPercent() {
        val r = PercentageFormulas.xIsWhatPercentOfY.compute(mapOf("x" to 15.0, "y" to 60.0))
        assertEquals(25.0, r.outputs.first().value, 1e-9)
    }

    // P006: discount 20% on 500 -> 100, 400
    @Test fun p006_discount() {
        val r = PercentageFormulas.discount.compute(mapOf("price" to 500.0, "pct" to 20.0))
        assertEquals(100.0, r.outputs[0].value, 1e-9)
        assertEquals(400.0, r.outputs[1].value, 1e-9)
    }

    // P007: markup 25% on cost 80 -> selling price 100
    @Test fun p007_markup() {
        val r = PercentageFormulas.markup.compute(mapOf("cost" to 80.0, "pct" to 25.0))
        assertEquals(100.0, r.outputs.first().value, 1e-9)
    }

    // P008: margin 20% on selling price 100 -> cost 80
    @Test fun p008_margin() {
        val r = PercentageFormulas.marginToCost.compute(mapOf("sellingPrice" to 100.0, "pct" to 20.0))
        assertEquals(80.0, r.outputs.first().value, 1e-9)
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError.Domain::class)
    fun p009_zeroDenominatorRejected() {
        PercentageFormulas.xIsWhatPercentOfY.compute(mapOf("x" to 10.0, "y" to 0.0))
    }

    // F001: simple interest 1000 @ 5% for 2 years -> 100, 1100
    @Test fun f001_simpleInterest() {
        val r = FinanceFormulas.simpleInterest.compute(mapOf("principal" to 1000.0, "rate" to 5.0, "years" to 2.0))
        assertEquals(100.0, r.outputs[0].value, 1e-9)
        assertEquals(1100.0, r.outputs[1].value, 1e-9)
    }

    // F002: compound interest 1000 @ 10% for 2 years -> total 1210
    @Test fun f002_compoundInterest() {
        val r = FinanceFormulas.compoundInterest.compute(mapOf("principal" to 1000.0, "rate" to 10.0, "years" to 2.0))
        assertEquals(1210.0, r.outputs[1].value, 1e-6)
    }

    // F003: EMI 120000 principal, 1% monthly, 12 payments ~ 10661.85
    @Test fun f003_emi() {
        val r = FinanceFormulas.emi.compute(mapOf("principal" to 120000.0, "monthlyRate" to 0.01, "payments" to 12.0))
        assertEquals(10661.85, r.outputs[0].value, 1.0)
    }

    // F004: zero-rate EMI 12000 / 12 = 1000
    @Test fun f004_zeroRateEmi() {
        val r = FinanceFormulas.emi.compute(mapOf("principal" to 12000.0, "monthlyRate" to 0.0, "payments" to 12.0))
        assertEquals(1000.0, r.outputs[0].value, 1e-9)
    }

    // F005: profit margin, revenue 200, cost 150 -> 25%
    @Test fun f005_profitMargin() {
        val r = FinanceFormulas.profitMargin.compute(mapOf("revenue" to 200.0, "cost" to 150.0))
        assertEquals(25.0, r.outputs[0].value, 1e-9)
    }

    // F006: markup, cost 150, selling price 200 -> ~33.3333%
    @Test fun f006_markupPercent() {
        val r = FinanceFormulas.markupPercent.compute(mapOf("cost" to 150.0, "sellingPrice" to 200.0))
        assertEquals(33.3333, r.outputs[0].value, 1e-3)
    }

    // G001: circle radius 1 -> area pi, circumference 2pi
    @Test fun g001_circle() {
        val r = GeometryFormulas.circle.compute(mapOf("r" to 1.0))
        assertEquals(Math.PI, r.outputs[0].value, 1e-9)
        assertEquals(2 * Math.PI, r.outputs[1].value, 1e-9)
    }

    // G004: triangle 3,4,5 -> area 6
    @Test fun g004_triangleArea() {
        val r = GeometryFormulas.triangle.compute(mapOf("a" to 3.0, "b" to 4.0, "c" to 5.0))
        assertEquals(6.0, r.outputs[0].value, 1e-9)
    }

    // G005: triangle 1,2,3 is invalid
    @Test(expected = com.axiom.calculator.core.math.AxMathError.InvalidInput::class)
    fun g005_invalidTriangleRejected() {
        GeometryFormulas.triangle.compute(mapOf("a" to 1.0, "b" to 2.0, "c" to 3.0))
    }

    // G006: cube side 3 -> volume 27, surface area 54
    @Test fun g006_cube() {
        val r = GeometryFormulas.cube.compute(mapOf("s" to 3.0))
        assertEquals(27.0, r.outputs[0].value, 1e-9)
        assertEquals(54.0, r.outputs[1].value, 1e-9)
    }

    // G007: sphere radius 1 -> volume 4pi/3
    @Test fun g007_sphereVolume() {
        val r = GeometryFormulas.sphere.compute(mapOf("r" to 1.0))
        assertEquals(4.0 / 3.0 * Math.PI, r.outputs[0].value, 1e-9)
    }

    // G008: cylinder radius 2, height 3 -> volume 12pi
    @Test fun g008_cylinderVolume() {
        val r = GeometryFormulas.cylinder.compute(mapOf("r" to 2.0, "h" to 3.0))
        assertEquals(12 * Math.PI, r.outputs[0].value, 1e-9)
    }
}
