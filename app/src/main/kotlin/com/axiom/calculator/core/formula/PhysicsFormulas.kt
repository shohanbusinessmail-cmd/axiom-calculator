package com.axiom.calculator.core.formula

import com.axiom.calculator.core.math.AxMathError

/** Physics/engineering formulas (FOR-PHY-xxx), SI units unless noted. */
object PhysicsFormulas {

    val density = object : Formula {
        override val id = "FOR-PHY-001"
        override val title = "Density"
        override val category = FormulaCategory.PHYSICS
        override val description = "Density = mass / volume."
        override val inputs = listOf(FormulaInputSpec("mass", "Mass", "kg"), FormulaInputSpec("volume", "Volume", "m3"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val m = requireValue(values, "mass"); val v = requireValue(values, "volume")
            if (v == 0.0) throw AxMathError.Domain("Density is undefined for zero volume.")
            return FormulaResult(listOf(FormulaOutput("Density", m / v, "kg/m3")))
        }
    }

    val force = object : Formula {
        override val id = "FOR-PHY-002"
        override val title = "Force (Newton's second law)"
        override val category = FormulaCategory.PHYSICS
        override val description = "Force = mass * acceleration."
        override val inputs = listOf(FormulaInputSpec("mass", "Mass", "kg"), FormulaInputSpec("acceleration", "Acceleration", "m/s2"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val m = requireValue(values, "mass"); val a = requireValue(values, "acceleration")
            return FormulaResult(listOf(FormulaOutput("Force", m * a, "N")))
        }
    }

    val work = object : Formula {
        override val id = "FOR-PHY-003"
        override val title = "Work"
        override val category = FormulaCategory.PHYSICS
        override val description = "Work = force * distance (force applied along the direction of motion)."
        override val inputs = listOf(FormulaInputSpec("force", "Force", "N"), FormulaInputSpec("distance", "Distance", "m"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val f = requireValue(values, "force"); val d = requireValue(values, "distance")
            return FormulaResult(listOf(FormulaOutput("Work", f * d, "J")))
        }
    }

    val power = object : Formula {
        override val id = "FOR-PHY-004"
        override val title = "Power"
        override val category = FormulaCategory.PHYSICS
        override val description = "Power = work / time."
        override val inputs = listOf(FormulaInputSpec("work", "Work", "J"), FormulaInputSpec("time", "Time", "s"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val w = requireValue(values, "work"); val t = requireValue(values, "time")
            if (t == 0.0) throw AxMathError.Domain("Power is undefined for zero time.")
            return FormulaResult(listOf(FormulaOutput("Power", w / t, "W")))
        }
    }

    val ohmsLaw = object : Formula {
        override val id = "FOR-PHY-005"
        override val title = "Ohm's law"
        override val category = FormulaCategory.PHYSICS
        override val description = "Voltage = current * resistance."
        override val inputs = listOf(FormulaInputSpec("current", "Current", "A"), FormulaInputSpec("resistance", "Resistance", "ohm"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val i = requireValue(values, "current"); val r = requireValue(values, "resistance")
            return FormulaResult(listOf(FormulaOutput("Voltage", i * r, "V")))
        }
    }

    val electricalPower = object : Formula {
        override val id = "FOR-PHY-006"
        override val title = "Electrical power"
        override val category = FormulaCategory.PHYSICS
        override val description = "Electrical power = voltage * current."
        override val inputs = listOf(FormulaInputSpec("voltage", "Voltage", "V"), FormulaInputSpec("current", "Current", "A"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val v = requireValue(values, "voltage"); val i = requireValue(values, "current")
            return FormulaResult(listOf(FormulaOutput("Power", v * i, "W")))
        }
    }

    val kineticEnergy = object : Formula {
        override val id = "FOR-PHY-007"
        override val title = "Kinetic energy"
        override val category = FormulaCategory.PHYSICS
        override val description = "Kinetic energy = 1/2 * mass * velocity^2."
        override val inputs = listOf(FormulaInputSpec("mass", "Mass", "kg"), FormulaInputSpec("velocity", "Velocity", "m/s"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val m = requireValue(values, "mass"); val v = requireValue(values, "velocity")
            return FormulaResult(listOf(FormulaOutput("Kinetic energy", 0.5 * m * v * v, "J")))
        }
    }

    val potentialEnergy = object : Formula {
        override val id = "FOR-PHY-008"
        override val title = "Gravitational potential energy"
        override val category = FormulaCategory.PHYSICS
        override val description = "Potential energy = mass * g * height, with g = 9.80665 m/s^2 (standard gravity)."
        override val inputs = listOf(FormulaInputSpec("mass", "Mass", "kg"), FormulaInputSpec("height", "Height", "m"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val m = requireValue(values, "mass"); val h = requireValue(values, "height")
            return FormulaResult(listOf(FormulaOutput("Potential energy", m * 9.80665 * h, "J")))
        }
    }

    val momentum = object : Formula {
        override val id = "FOR-PHY-009"
        override val title = "Momentum"
        override val category = FormulaCategory.PHYSICS
        override val description = "Momentum = mass * velocity."
        override val inputs = listOf(FormulaInputSpec("mass", "Mass", "kg"), FormulaInputSpec("velocity", "Velocity", "m/s"))
        override fun compute(values: Map<String, Double>): FormulaResult {
            val m = requireValue(values, "mass"); val v = requireValue(values, "velocity")
            return FormulaResult(listOf(FormulaOutput("Momentum", m * v, "kg*m/s")))
        }
    }

    val all: List<Formula> = listOf(density, force, work, power, ohmsLaw, electricalPower, kineticEnergy, potentialEnergy, momentum)
}
