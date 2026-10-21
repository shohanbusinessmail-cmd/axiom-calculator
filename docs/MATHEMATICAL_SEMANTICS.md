# Mathematical Semantics

This document is the authoritative contract for how Axiom Calculator
interprets and evaluates expressions (SECTION 8 of the specification).

## 1. Grammar

```
expr    := addsub
addsub  := muldiv (('+'|'-') muldiv)*
muldiv  := unary ( ('*'|'/'|implicit) unary )*
unary   := ('+'|'-') unary | power
power   := postfix ('^' unary)?                 // right-associative
postfix := primary ('!' | '%' | '²' | '³')*
primary := NUMBER | CONST | FUNC '(' args ')' | '(' expr ')'
```
Implemented in `core/math/Tokenizer.kt`, `core/math/ExpressionParser.kt`
(Kotlin) and `scripts/verification/engine_reference.py` (Python reference,
executed; see `docs/QUALITY_REPORT.md`).

## 2. Documented ambiguity resolutions (mandatory per SECTION 8)

| Expression | Result | Reason |
|---|---|---|
| `2^3^2` | `512` | `^` is right-associative: `2^(3^2)` |
| `-2^2` | `-4` | Unary minus binds *looser* than `^`: `-(2^2)` |
| `(-2)^2` | `4` | Parentheses force the base to be `-2` |
| `2(3+4)` | `14` | Implicit multiplication at the same precedence as `*` |
| `1/2x` | `(1/2)*x`, i.e. `x/2` | Implicit multiplication is left-to-right at `*`/`/` precedence, **not** given higher precedence than the preceding `/`. This is a deliberate, documented convention choice — the opposite convention (`1/(2x)`) exists on some calculators and is NOT what this app implements. |

## 3. Numeric policy

Axiom Calculator does **not** use a single numeric type everywhere:

| Context | Type used | Why |
|---|---|---|
| Standard calculator (+, -, *, /, %, parentheses) | `BigDecimal` via `DecimalArithmetic`/`BigDecimalEvaluator` (34 significant digits, `HALF_UP`) | Avoids binary floating-point artifacts (e.g. `0.1 + 0.2` must display as exactly `"0.3"`, not `0.30000000000000004`). |
| Scientific functions (trig, log, exp, roots, ...) | `Double` via `Evaluator` | No exact decimal representation exists for transcendental functions; IEEE-754 double precision with a documented `1e-10` tolerance for mandatory test vectors is used instead. |
| Factorial, nPr, nCr | `BigInteger` via `MathFunctions` | Exact integer arithmetic; a Double would silently lose precision above `2^53`. |
| Complex numbers | `Complex(re: Double, im: Double)` | Documented principal-branch convention: `arg()` returns `(-pi, pi]`; `pow()` uses the principal complex logarithm. |
| Fractions | `AxFraction(BigInteger, BigInteger)` | Exact rational arithmetic with automatic GCD simplification; `fromDecimal()` uses a bounded continued-fraction expansion (max 64 iterations, configurable max denominator) so it always terminates. |
| Matrices | `AxMatrix` (`DoubleArray`-backed) | Standard numerical linear algebra with partial pivoting; a documented `1e-12` tolerance treats near-zero pivots as singular. |
| Programmer tools | `BigInteger`, masked to an explicit `BitWidth` (8/16/32/64) | Large integers are never silently coerced to floating point. |

Display precision is always decoupled from internal precision
(`NumberFormatter`/`DisplayFormatOptions`): a value is computed at full
internal precision and only rounded for display.

## 4. Error model

All failures are typed via the `AxMathError` sealed class
(`Syntax`, `DivisionByZero`, `Domain`, `Overflow`, `Unsupported`,
`SingularMatrix`, `NoRealRoots`, `NonConvergent`, `ResourceLimit`,
`InvalidInput`), each carrying a plain-English message. No stack trace or
raw exception message is ever shown to the user; see
`core/math/MathError.kt` and `AxResult`/`axCatching`.

Explicitly handled domain errors: negative `sqrt`/`ln`/`log`, `asin`/`acos`
outside `[-1, 1]`, `tan` at an odd multiple of 90°, `atanh` outside `(-1, 1)`,
`acosh` below 1, negative/non-integer factorial, division by zero (real and
complex), singular matrix inversion, invalid triangle side lengths, and
resource limits on expression length (500 chars), parenthesis nesting (64),
and factorial input (5000).

## 5. Standard vs. Scientific percentage semantics (kept deliberately distinct)

- **Standard calculator contextual `%`:** mimics mainstream calculator apps —
  for `+`/`-`, `a <op> b%` means "`b`% of `a`"; for `*`/`/`, it means plain
  `b/100`. See `DecimalArithmetic.contextualPercent`.
- **Formula calculators (`core/formula/PercentageFormulas.kt`):** explicit,
  named formulas (X% of Y, X is what % of Y, increase/decrease, change,
  reverse percentage, discount, markup vs. margin) with their own precise
  definitions — markup is relative to **cost**, margin is relative to
  **selling price**; these are never conflated (SECTION 9).

## 6. Angle mode

DEG/RAD is a persisted, explicit `AngleMode` passed into every `EvalContext`;
it is never inferred. The Scientific tab always displays the active mode.
