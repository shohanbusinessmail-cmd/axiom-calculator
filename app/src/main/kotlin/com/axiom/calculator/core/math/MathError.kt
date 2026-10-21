package com.axiom.calculator.core.math

/**
 * Typed error model for the Axiom mathematical engine (SECTION 8 of the spec).
 *
 * Every failure mode the engine can produce is represented here instead of
 * leaking raw exceptions or stack traces to the UI. Each variant carries an
 * English, user-presentable [message] with no technical jargon.
 */
sealed class AxMathError(override val message: String) : Exception(message) {
    class Syntax(message: String) : AxMathError(message)
    class DivisionByZero(message: String = "Division by zero is undefined.") : AxMathError(message)
    class Domain(message: String) : AxMathError(message)
    class Overflow(message: String = "The result is too large to represent.") : AxMathError(message)
    class Unsupported(message: String) : AxMathError(message)
    class SingularMatrix(message: String = "The matrix is singular and has no inverse.") : AxMathError(message)
    class NoRealRoots(message: String = "No real roots exist for this equation.") : AxMathError(message)
    class NonConvergent(message: String = "The numerical method did not converge.") : AxMathError(message)
    class ResourceLimit(message: String) : AxMathError(message)
    class InvalidInput(message: String) : AxMathError(message)
}

/** Result wrapper avoiding exceptions crossing architectural boundaries into the UI layer. */
sealed class AxResult<out T> {
    data class Success<T>(val value: T) : AxResult<T>()
    data class Failure(val error: AxMathError) : AxResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): AxResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    fun getOrNull(): T? = (this as? Success)?.value
}

inline fun <T> axCatching(block: () -> T): AxResult<T> = try {
    AxResult.Success(block())
} catch (e: AxMathError) {
    AxResult.Failure(e)
} catch (e: ArithmeticException) {
    AxResult.Failure(AxMathError.DivisionByZero(e.message ?: "Arithmetic error."))
} catch (e: StackOverflowError) {
    AxResult.Failure(AxMathError.ResourceLimit("Expression is too deeply nested."))
}
