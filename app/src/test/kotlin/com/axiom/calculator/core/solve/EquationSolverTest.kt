package com.axiom.calculator.core.solve

import org.junit.Assert.*
import org.junit.Test

class EquationSolverTest {
    @Test
    fun e001_linearEquationFromCanonicalString() {
        val poly = SingleVariableEquationParser.parseEquation("2x + 3 = 11")
        val result = EquationSolver.solveLinear(poly.b, poly.c) as LinearResult.Unique
        assertEquals(4.0, result.x, 1e-9)
    }

    @Test
    fun e002_quadraticTwoRealRoots() {
        val poly = SingleVariableEquationParser.parseEquation("x^2 - 5x + 6 = 0")
        val result = EquationSolver.solveQuadraticReal(poly.a, poly.b, poly.c) as QuadraticRealResult.TwoRoots
        assertEquals(2.0, result.x1, 1e-9)
        assertEquals(3.0, result.x2, 1e-9)
    }

    @Test
    fun e003_noRealRoots() {
        val result = EquationSolver.solveQuadraticReal(1.0, 0.0, 1.0)
        assertTrue(result is QuadraticRealResult.NoRealRoots)
    }

    @Test
    fun e004_complexRoots() {
        val result = EquationSolver.solveQuadraticComplex(1.0, 0.0, 1.0)
        assertEquals(0.0, result.x1.re, 1e-9); assertEquals(-1.0, result.x1.im, 1e-9)
        assertEquals(0.0, result.x2.re, 1e-9); assertEquals(1.0, result.x2.im, 1e-9)
    }

    @Test
    fun e005_differenceOfSquares() {
        val result = EquationSolver.solveQuadraticReal(1.0, 0.0, -4.0) as QuadraticRealResult.TwoRoots
        assertEquals(-2.0, result.x1, 1e-9); assertEquals(2.0, result.x2, 1e-9)
    }

    @Test
    fun e006_noSolution() {
        assertTrue(EquationSolver.solveLinear(0.0, 1.0) is LinearResult.NoSolution)
    }

    @Test
    fun e007_infiniteSolutions() {
        assertTrue(EquationSolver.solveLinear(0.0, 0.0) is LinearResult.InfiniteSolutions)
    }

    @Test
    fun e008_system2x2() {
        val result = EquationSolver.solveSystem2x2(2.0, 1.0, 5.0, 1.0, -1.0, 1.0) as System2x2Result.Unique
        assertEquals(2.0, result.x, 1e-9); assertEquals(1.0, result.y, 1e-9)
    }

    @Test
    fun e009_singularSystem() {
        assertTrue(EquationSolver.solveSystem2x2(1.0, 1.0, 2.0, 2.0, 2.0, 4.0) is System2x2Result.Singular)
    }

    @Test
    fun equationParser_rejectsHigherDegree() {
        var threw = false
        try {
            SingleVariableEquationParser.parseEquation("x^3 + 1 = 0")
        } catch (e: com.axiom.calculator.core.math.AxMathError.Unsupported) {
            threw = true
        }
        assertTrue("Expected Unsupported error for cubic equation", threw)
    }
}
