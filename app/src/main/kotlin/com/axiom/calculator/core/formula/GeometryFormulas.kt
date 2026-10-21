package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/** Geometry formulas (FOR-GEO-xxx). All dimensions must be positive; triangle inputs are validated against the strict triangle inequality. */
object GeometryFormulas {

    private fun requirePositive(v: Double, name: String) {
        if (v <= 0.0) throw AxMathError.InvalidInput("$name must be a positive number.")
    }

    val circle = object : Formula {
        override val id = "FOR-GEO-001"
        override val title = "Circle"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area and circumference of a circle from its radius."
        override val inputs = listOf(FormulaInputSpec("r", "Radius"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val r = requireValue(values, "r"); requirePositive(r, "Radius")
            return FormulaResult(listOf(FormulaOutput("Area", PI * r * r), FormulaOutput("Circumference", 2 * PI * r)))
        }
    }

    val rectangle = object : Formula {
        override val id = "FOR-GEO-002"
        override val title = "Rectangle"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area and perimeter of a rectangle."
        override val inputs = listOf(FormulaInputSpec("w", "Width"), FormulaInputSpec("h", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val w = requireValue(values, "w"); val h = requireValue(values, "h")
            requirePositive(w, "Width"); requirePositive(h, "Height")
            return FormulaResult(listOf(FormulaOutput("Area", w * h), FormulaOutput("Perimeter", 2 * (w + h))))
        }
    }

    val square = object : Formula {
        override val id = "FOR-GEO-003"
        override val title = "Square"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area and perimeter of a square."
        override val inputs = listOf(FormulaInputSpec("s", "Side"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val s = requireValue(values, "s"); requirePositive(s, "Side")
            return FormulaResult(listOf(FormulaOutput("Area", s * s), FormulaOutput("Perimeter", 4 * s)))
        }
    }

    val triangle = object : Formula {
        override val id = "FOR-GEO-004"
        override val title = "Triangle (by three sides)"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area of a triangle from its three side lengths (Heron's formula). Validates the strict triangle inequality."
        override val inputs = listOf(FormulaInputSpec("a", "Side a"), FormulaInputSpec("b", "Side b"), FormulaInputSpec("c", "Side c"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val a = requireValue(values, "a"); val b = requireValue(values, "b"); val c = requireValue(values, "c")
            requirePositive(a, "Side a"); requirePositive(b, "Side b"); requirePositive(c, "Side c")
            if (!(a + b > c && a + c > b && b + c > a)) {
                throw AxMathError.InvalidInput("These three side lengths do not satisfy the triangle inequality and cannot form a triangle.")
            }
            val s = (a + b + c) / 2
            val area = sqrt(s * (s - a) * (s - b) * (s - c))
            return FormulaResult(listOf(FormulaOutput("Area", area), FormulaOutput("Perimeter", a + b + c)))
        }
    }

    val trapezoid = object : Formula {
        override val id = "FOR-GEO-005"
        override val title = "Trapezoid"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area of a trapezoid from its two parallel sides and height."
        override val inputs = listOf(FormulaInputSpec("a", "Parallel side a"), FormulaInputSpec("b", "Parallel side b"), FormulaInputSpec("h", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val a = requireValue(values, "a"); val b = requireValue(values, "b"); val h = requireValue(values, "h")
            requirePositive(a, "Side a"); requirePositive(b, "Side b"); requirePositive(h, "Height")
            return FormulaResult(listOf(FormulaOutput("Area", (a + b) / 2 * h)))
        }
    }

    val cube = object : Formula {
        override val id = "FOR-GEO-006"
        override val title = "Cube"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and surface area of a cube."
        override val inputs = listOf(FormulaInputSpec("s", "Side"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val s = requireValue(values, "s"); requirePositive(s, "Side")
            return FormulaResult(listOf(FormulaOutput("Volume", s.pow(3)), FormulaOutput("Surface area", 6 * s * s)))
        }
    }

    val cuboid = object : Formula {
        override val id = "FOR-GEO-007"
        override val title = "Cuboid"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and surface area of a rectangular box."
        override val inputs = listOf(FormulaInputSpec("l", "Length"), FormulaInputSpec("w", "Width"), FormulaInputSpec("h", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val l = requireValue(values, "l"); val w = requireValue(values, "w"); val h = requireValue(values, "h")
            requirePositive(l, "Length"); requirePositive(w, "Width"); requirePositive(h, "Height")
            return FormulaResult(listOf(FormulaOutput("Volume", l * w * h), FormulaOutput("Surface area", 2 * (l * w + l * h + w * h))))
        }
    }

    val sphere = object : Formula {
        override val id = "FOR-GEO-008"
        override val title = "Sphere"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and surface area of a sphere."
        override val inputs = listOf(FormulaInputSpec("r", "Radius"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val r = requireValue(values, "r"); requirePositive(r, "Radius")
            return FormulaResult(listOf(FormulaOutput("Volume", 4.0 / 3.0 * PI * r.pow(3)), FormulaOutput("Surface area", 4 * PI * r * r)))
        }
    }

    val hemisphere = object : Formula {
        override val id = "FOR-GEO-009"
        override val title = "Hemisphere"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and total surface area (curved + base) of a hemisphere."
        override val inputs = listOf(FormulaInputSpec("r", "Radius"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val r = requireValue(values, "r"); requirePositive(r, "Radius")
            return FormulaResult(listOf(FormulaOutput("Volume", 2.0 / 3.0 * PI * r.pow(3)), FormulaOutput("Surface area", 3 * PI * r * r)))
        }
    }

    val cylinder = object : Formula {
        override val id = "FOR-GEO-010"
        override val title = "Cylinder"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and surface area of a right circular cylinder."
        override val inputs = listOf(FormulaInputSpec("r", "Radius"), FormulaInputSpec("h", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val r = requireValue(values, "r"); val h = requireValue(values, "h")
            requirePositive(r, "Radius"); requirePositive(h, "Height")
            return FormulaResult(listOf(FormulaOutput("Volume", PI * r * r * h), FormulaOutput("Surface area", 2 * PI * r * (r + h))))
        }
    }

    val cone = object : Formula {
        override val id = "FOR-GEO-011"
        override val title = "Cone"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Volume and lateral/total surface area of a right circular cone."
        override val inputs = listOf(FormulaInputSpec("r", "Radius"), FormulaInputSpec("h", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val r = requireValue(values, "r"); val h = requireValue(values, "h")
            requirePositive(r, "Radius"); requirePositive(h, "Height")
            val slant = sqrt(r * r + h * h)
            return FormulaResult(
                listOf(
                    FormulaOutput("Volume", 1.0 / 3.0 * PI * r * r * h),
                    FormulaOutput("Total surface area", PI * r * (r + slant))
                )
            )
        }
    }

    val annulus = object : Formula {
        override val id = "FOR-GEO-012"
        override val title = "Annulus (ring)"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area of the ring between two concentric circles."
        override val inputs = listOf(FormulaInputSpec("rOuter", "Outer radius"), FormulaInputSpec("rInner", "Inner radius"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val rOuter = requireValue(values, "rOuter"); val rInner = requireValue(values, "rInner")
            requirePositive(rOuter, "Outer radius"); requirePositive(rInner, "Inner radius")
            if (rInner >= rOuter) throw AxMathError.InvalidInput("The inner radius must be smaller than the outer radius.")
            return FormulaResult(listOf(FormulaOutput("Area", PI * (rOuter * rOuter - rInner * rInner))))
        }
    }

    val parallelogram = object : Formula {
        override val id = "FOR-GEO-013"
        override val title = "Parallelogram"
        override val category = FormulaCategory.GEOMETRY
        override val description = "Area of a parallelogram from its base and height."
        override val inputs = listOf(FormulaInputSpec("base", "Base"), FormulaInputSpec("height", "Height"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val base = requireValue(values, "base"); val height = requireValue(values, "height")
            requirePositive(base, "Base"); requirePositive(height, "Height")
            return FormulaResult(listOf(FormulaOutput("Area", base * height)))
        }
    }

    val all: List<Formula> = listOf(
        circle, rectangle, square, triangle, trapezoid, cube, cuboid,
        sphere, hemisphere, cylinder, cone, annulus, parallelogram
    )
}
