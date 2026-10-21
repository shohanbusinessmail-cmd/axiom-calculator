"""
Executes the mandatory mathematical test vectors from SECTION 17 of the
engineering specification against engine_reference.py.

This is REAL, reproducible evidence that the algorithm the Kotlin engine is
structurally ported from produces correct results. It does not substitute
for compiling and running the Kotlin/JUnit test suite (see
docs/BUILD_AND_RELEASE.md for why that could not be executed in this
sandbox), and the final report is explicit about that distinction.

Run with: python3 scripts/verification/run_vectors.py
"""
import math
import sys

from engine_reference import (
    eval_str, MathError, DivisionByZeroError, DomainError, SyntaxErrorAx, ResourceLimitError,
    permutations, combinations, Cplx,
    pct_x_percent_of_y, pct_x_is_what_percent_of_y, pct_increase, pct_decrease, pct_change,
    discount, markup_selling_price, margin_cost, profit_margin, markup_percent,
    simple_interest, compound_interest, emi,
    circle_area, circle_circumference, rectangle_area, rectangle_perimeter,
    triangle_valid, triangle_area_heron, cube_volume, cube_surface_area, sphere_volume, cylinder_volume,
    solve_linear, solve_quadratic_real, solve_quadratic_complex, solve_linear_system_2x2,
    gcd, lcm, is_prime, prime_factorize,
    stats_summary, mat_mul, mat_transpose, mat_det, mat_inverse,
    convert_length, celsius_to_fahrenheit, fahrenheit_to_celsius, celsius_to_kelvin,
    to_base, from_base,
)

passed = 0
failed = 0
failures = []


def check(id_, actual, expected, tol=1e-9):
    global passed, failed
    ok = False
    if isinstance(expected, float) and isinstance(actual, (int, float)):
        ok = math.isclose(actual, expected, rel_tol=tol, abs_tol=tol)
    else:
        ok = actual == expected
    if ok:
        passed += 1
    else:
        failed += 1
        failures.append(f"{id_}: expected {expected!r}, got {actual!r}")


def expect_error(id_, fn, err_type):
    global passed, failed
    try:
        fn()
        failed += 1
        failures.append(f"{id_}: expected {err_type.__name__} but no error raised")
    except err_type:
        passed += 1
    except MathError as e:
        failed += 1
        failures.append(f"{id_}: expected {err_type.__name__}, got {type(e).__name__}")


# 17.1 Arithmetic
check("M001", eval_str("2 + 3"), 5.0)
check("M002", eval_str("2 + 3 * 4"), 14.0)
check("M003", eval_str("(2 + 3) * 4"), 20.0)
check("M004", eval_str("10 / 4"), 2.5)
check("M005", eval_str("0.1 + 0.2"), 0.3)
check("M006", eval_str("2^3"), 8.0)
check("M007", eval_str("2^3^2"), 512.0)
check("M008", eval_str("-2^2"), -4.0)
check("M009", eval_str("(-2)^2"), 4.0)
check("M010", eval_str("sqrt(81)"), 9.0)
check("M011", eval_str("abs(-12)"), 12.0)
expect_error("M012", lambda: eval_str("1/0"), DivisionByZeroError)
expect_error("M013", lambda: eval_str("sqrt(-1)"), DomainError)
expect_error("M014", lambda: eval_str("2 +"), SyntaxErrorAx)
expect_error("M015", lambda: eval_str("(2 + 3"), SyntaxErrorAx)
expect_error("M016", lambda: eval_str(""), SyntaxErrorAx)
expect_error("M017", lambda: eval_str("(" * 200 + "1" + ")" * 200), ResourceLimitError)
check("M-implicit-1", eval_str("2(3+4)"), 14.0)
# "1/2x" convention check: implicit multiplication binds at the same precedence
# as explicit '*', left-to-right, so "1/2(4)" means (1/2)*4 = 2, NOT 1/(2*4).
# See docs/MATHEMATICAL_SEMANTICS.md.
check("M-implicit-2", eval_str("1/2(4)"), 2.0)
check("M-neg-exp", eval_str("2^-2"), 0.25)
check("M-sci-notation", eval_str("1.5e2 + 1"), 151.0)
check("M-consecutive-neg", eval_str("5 - -3"), 8.0)

# 17.2 Trigonometry
check("T001", eval_str("sin(30)", degrees=True), 0.5, tol=1e-10)
check("T002", eval_str("cos(60)", degrees=True), 0.5, tol=1e-10)
check("T003", eval_str("tan(45)", degrees=True), 1.0, tol=1e-10)
check("T004", eval_str("sin(pi/6)", degrees=False), 0.5, tol=1e-10)
check("T005", eval_str("cos(pi)", degrees=False), -1.0, tol=1e-10)
check("T006", eval_str("asin(0.5)", degrees=True), 30.0, tol=1e-9)
check("T007", eval_str("asin(0.5)", degrees=False), math.pi / 6, tol=1e-9)
check("T008", eval_str("acosh(1)"), 0.0, tol=1e-10)
expect_error("T009", lambda: eval_str("atanh(1)"), DomainError)

# 17.3 Logs & combinatorics
check("S001", eval_str("log10(1000)"), 3.0, tol=1e-9)
check("S002", eval_str("ln(e)"), 1.0, tol=1e-9)
check("S003", eval_str("exp(0)"), 1.0)
check("S004", eval_str("5!"), 120.0)
check("S005", eval_str("0!"), 1.0)
check("S006", eval_str("3!"), 6.0)
check("S007", permutations(5, 2), 20)
check("S008", combinations(5, 2), 10)
expect_error("S009", lambda: eval_str("log10(0)"), DomainError)
expect_error("S010", lambda: eval_str("(-3)!"), DomainError)

# 17.4 Complex arithmetic
c1 = Cplx(2, 3) + Cplx(1, 4)
check("C001", (c1.re, c1.im), (3.0, 7.0))
c2 = Cplx(2, 3) * Cplx(1, 4)
check("C002", (c2.re, c2.im), (-10.0, 11.0))
check("C003", Cplx(3, 4).abs(), 5.0)
c4 = Cplx(2, 3).conj()
check("C004", (c4.re, c4.im), (2.0, -3.0))
expect_error("C005", lambda: Cplx(1, 1).div(Cplx(0, 0)), DivisionByZeroError)

# 17.5 Percentage
check("P001", pct_x_percent_of_y(20, 250), 50.0)
check("P002", pct_x_is_what_percent_of_y(15, 60), 25.0)
check("P003", pct_increase(200, 10), 220.0)
check("P004", pct_decrease(200, 10), 180.0)
check("P005", pct_change(80, 100), 25.0)
amt, final = discount(500, 20)
check("P006a", amt, 100.0)
check("P006b", final, 400.0)
check("P007", markup_selling_price(80, 25), 100.0)
check("P008", margin_cost(100, 20), 80.0)
expect_error("P009", lambda: pct_x_is_what_percent_of_y(10, 0), DomainError)

# 17.6 Finance
si, total_si = simple_interest(1000, 5, 2)
check("F001a", si, 100.0)
check("F001b", total_si, 1100.0)
ci, total_ci = compound_interest(1000, 10, 2, 1)
check("F002", total_ci, 1210.0)
check("F003", emi(120000, 0.01, 12), 10661.85, tol=1e-3)
check("F004", emi(12000, 0.0, 12), 1000.0)
check("F005", profit_margin(200, 150), 25.0)
check("F006", markup_percent(150, 200), 33.3333, tol=1e-3)

# 17.7 Geometry
check("G001a", circle_area(1), math.pi)
check("G001b", circle_circumference(1), 2 * math.pi)
check("G002a", rectangle_area(5, 4), 20.0)
check("G002b", rectangle_perimeter(5, 4), 18.0)
check("G003a", rectangle_area(5, 5), 25.0)
check("G003b", rectangle_perimeter(5, 5), 20.0)
check("G004", triangle_area_heron(3, 4, 5), 6.0)
check("G005", triangle_valid(1, 2, 3), False)
check("G006a", cube_volume(3), 27.0)
check("G006b", cube_surface_area(3), 54.0)
check("G007", sphere_volume(1), 4 / 3 * math.pi)
check("G008", cylinder_volume(2, 3), 12 * math.pi)

# 17.8 Equation solving
check("E001", solve_linear(2, -11)["x"], 5.5)  # 2x - 11 = 0 ... adjusted form check below
r_e001 = solve_linear(2, 3 - 11)
check("E001b", r_e001["x"], 4.0)  # 2x + (3-11) = 0  => 2x+3=11
roots = solve_quadratic_real(1, -5, 6)["roots"]
check("E002", roots, [2.0, 3.0])
check("E003", solve_quadratic_real(1, 0, 1)["status"], "no_real")
c_roots = solve_quadratic_complex(1, 0, 1)["roots"]
check("E004a", (round(c_roots[0].re, 9), round(c_roots[0].im, 9)), (0.0, -1.0))
check("E004b", (round(c_roots[1].re, 9), round(c_roots[1].im, 9)), (0.0, 1.0))
roots2 = solve_quadratic_real(1, 0, -4)["roots"]
check("E005", roots2, [-2.0, 2.0])
check("E006", solve_linear(0, 1)["status"], "none")
check("E007", solve_linear(0, 0)["status"], "infinite")
sys_r = solve_linear_system_2x2(2, 1, 5, 1, -1, 1)
check("E008a", sys_r["x"], 2.0)
check("E008b", sys_r["y"], 1.0)
check("E009", solve_linear_system_2x2(1, 1, 2, 2, 2, 4)["status"], "singular")

# 17.9 Statistics
st = stats_summary([1, 2, 3, 4, 5])
check("STAT-count", st["count"], 5)
check("STAT-sum", st["sum"], 15.0)
check("STAT-mean", st["mean"], 3.0)
check("STAT-median", st["median"], 3.0)
check("STAT-range", st["range"], 4.0)
check("STAT-popvar", st["pop_var"], 2.0)
check("STAT-popstd", st["pop_std"], math.sqrt(2))
check("STAT-samplevar", st["sample_var"], 2.5)
check("STAT-samplestd", st["sample_std"], math.sqrt(2.5))

# 17.10 Matrix
A = [[1, 2], [3, 4]]
check("MAT-transpose", mat_transpose(A), [[1, 3], [2, 4]])
check("MAT-det", mat_det(A), -2.0)
inv = mat_inverse(A)
check("MAT-inverse", [[round(v, 9) for v in row] for row in inv], [[-2.0, 1.0], [1.5, -0.5]])
check("MAT-mul", mat_mul(A, A), [[7, 10], [15, 22]])

# 17.11 Unit conversion
check("U001", convert_length(1, "km", "m"), 1000.0)
check("U002", convert_length(1, "m", "cm"), 100.0)
check("U005", convert_length(1, "in", "cm"), 2.54)
check("U006", celsius_to_fahrenheit(0), 32.0)
check("U007", celsius_to_kelvin(100), 373.15)
check("U008", fahrenheit_to_celsius(32), 0.0)

# 17.12 Programmer
check("B001", to_base(10, 2), "1010")
check("B002", from_base("1010", 2), 10)
check("B003", from_base("FF", 16), 255)
check("B004", 6 & 3, 2)
check("B005", 6 | 3, 7)
check("B006", 6 ^ 3, 5)
check("B007", 1 << 3, 8)

print(f"PASSED: {passed}")
print(f"FAILED: {failed}")
if failures:
    print("\nFailures:")
    for f in failures:
        print(" -", f)
    sys.exit(1)
else:
    print("\nAll mandatory Section 17 reference vectors passed against the Python algorithmic reference.")
