"""
Reference implementation of the Axiom Calculator mathematical engine.

PURPOSE
-------
This module is NOT part of the shipped Android application. It exists purely
as an independently executable algorithmic reference: the sandboxed build
environment used to author this repository has no JDK/Android SDK/Gradle and
no network access to Maven Central or Google's Maven repository (see
docs/BUILD_AND_RELEASE.md), so the Kotlin sources cannot be compiled or
unit-tested in this environment.

To avoid shipping mathematical logic that was never actually executed, the
tokenizer / parser / evaluator / solvers below are written in plain Python
(always available in this sandbox) using the exact same grammar, precedence
rules, and numeric policy documented in docs/MATHEMATICAL_SEMANTICS.md. The
Kotlin sources under app/src/main/kotlin/com/axiom/calculator/core/ are a
line-by-line structural port of this logic. Running
`python3 scripts/verification/run_vectors.py` exercises every mandatory test
vector from SECTION 17 of the engineering spec against this reference and
reports pass/fail counts -- genuine, reproducible evidence for the algorithm
itself, even though it cannot substitute for a real Kotlin/JUnit run.
"""
from __future__ import annotations

import math
from dataclasses import dataclass, field
from decimal import Decimal, getcontext, ROUND_HALF_UP, InvalidOperation
from fractions import Fraction
from typing import List, Optional, Union

getcontext().prec = 50


class MathError(Exception):
    """Base type mirroring the sealed AxMathError hierarchy in Kotlin."""


class SyntaxErrorAx(MathError):
    pass


class DivisionByZeroError(MathError):
    pass


class DomainError(MathError):
    pass


class OverflowErrorAx(MathError):
    pass


class ResourceLimitError(MathError):
    pass


# ---------------------------------------------------------------------------
# Tokenizer
# ---------------------------------------------------------------------------

@dataclass
class Token:
    kind: str
    text: str
    value: Optional[float] = None


FUNCTIONS = {
    "sin", "cos", "tan", "asin", "acos", "atan",
    "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
    "sqrt", "cbrt", "abs", "floor", "ceil", "round", "trunc",
    "ln", "log10", "log2", "log", "exp",
}
CONSTANTS = {"pi": math.pi, "e": math.e}

MAX_EXPR_LEN = 500
MAX_NEST = 64


def tokenize(src: str) -> List[Token]:
    if len(src) > MAX_EXPR_LEN:
        raise ResourceLimitError("Expression exceeds maximum supported length.")
    tokens: List[Token] = []
    i = 0
    n = len(src)
    depth = 0
    while i < n:
        c = src[i]
        if c.isspace():
            i += 1
            continue
        if c.isdigit() or (c == "." and i + 1 < n and src[i + 1].isdigit()):
            j = i
            seen_dot = False
            while j < n and (src[j].isdigit() or (src[j] == "." and not seen_dot)):
                if src[j] == ".":
                    seen_dot = True
                j += 1
            # scientific notation suffix, e.g. 1.2e-3
            if j < n and src[j] in "eE" and (j + 1 < n and (src[j + 1].isdigit() or src[j + 1] in "+-")):
                k = j + 1
                if src[k] in "+-":
                    k += 1
                start_digits = k
                while k < n and src[k].isdigit():
                    k += 1
                if k > start_digits:
                    j = k
            text = src[i:j]
            tokens.append(Token("NUMBER", text, float(text)))
            i = j
            continue
        if c.isalpha() or c == "_":
            j = i
            while j < n and (src[j].isalnum() or src[j] == "_"):
                j += 1
            name = src[i:j]
            tokens.append(Token("IDENT", name))
            i = j
            continue
        if c == "²":
            tokens.append(Token("SUP2", c))
            i += 1
            continue
        if c in "+-*/^!%(),":
            kind = {
                "+": "PLUS", "-": "MINUS", "*": "STAR", "/": "SLASH",
                "^": "CARET", "!": "BANG", "%": "PERCENT",
                "(": "LPAREN", ")": "RPAREN", ",": "COMMA",
            }[c]
            if kind == "LPAREN":
                depth += 1
                if depth > MAX_NEST:
                    raise ResourceLimitError("Expression nesting exceeds the supported limit.")
            if kind == "RPAREN":
                depth -= 1
            tokens.append(Token(kind, c))
            i += 1
            continue
        raise SyntaxErrorAx(f"Unexpected character '{c}' at position {i}.")
    if depth != 0:
        raise SyntaxErrorAx("Mismatched parentheses.")
    tokens.append(Token("END", ""))
    return tokens


# ---------------------------------------------------------------------------
# Parser -> small AST, recursive-descent / precedence-climbing.
# Grammar (low to high precedence):
#   expr    := addsub
#   addsub  := muldiv (('+'|'-') muldiv)*
#   muldiv  := unary ( ('*'|'/'|IMPLICIT) unary )*
#   unary   := ('+'|'-') unary | power
#   power   := postfix ('^' unary)?           // right-associative
#   postfix := primary ('!' | '%' | SUP2)*
#   primary := NUMBER | CONST | FUNC '(' args ')' | '(' expr ')'
# ---------------------------------------------------------------------------

Node = Union[tuple]


class Parser:
    def __init__(self, tokens: List[Token]):
        self.toks = tokens
        self.pos = 0

    def peek(self) -> Token:
        return self.toks[self.pos]

    def advance(self) -> Token:
        t = self.toks[self.pos]
        self.pos += 1
        return t

    def expect(self, kind: str) -> Token:
        if self.peek().kind != kind:
            raise SyntaxErrorAx(f"Expected {kind} but found '{self.peek().text}'.")
        return self.advance()

    def parse(self):
        node = self.addsub()
        if self.peek().kind != "END":
            raise SyntaxErrorAx(f"Unexpected token '{self.peek().text}'.")
        return node

    def addsub(self):
        node = self.muldiv()
        while self.peek().kind in ("PLUS", "MINUS"):
            op = self.advance().kind
            rhs = self.muldiv()
            node = ("bin", "+" if op == "PLUS" else "-", node, rhs)
        return node

    def _starts_implicit_factor(self) -> bool:
        k = self.peek().kind
        if k in ("NUMBER", "LPAREN", "IDENT"):
            return True
        return False

    def muldiv(self):
        node = self.unary()
        while True:
            k = self.peek().kind
            if k in ("STAR", "SLASH"):
                op = self.advance().kind
                rhs = self.unary()
                node = ("bin", "*" if op == "STAR" else "/", node, rhs)
            elif self._starts_implicit_factor():
                rhs = self.unary()
                node = ("bin", "*", node, rhs)
            else:
                break
        return node

    def unary(self):
        if self.peek().kind in ("PLUS", "MINUS"):
            op = self.advance().kind
            operand = self.unary()
            if op == "MINUS":
                return ("neg", operand)
            return operand
        return self.power()

    def power(self):
        base = self.postfix()
        if self.peek().kind == "CARET":
            self.advance()
            exp = self.unary()
            return ("bin", "^", base, exp)
        return base

    def postfix(self):
        node = self.primary()
        while self.peek().kind in ("BANG", "PERCENT", "SUP2"):
            k = self.advance().kind
            if k == "BANG":
                node = ("fact", node)
            elif k == "SUP2":
                node = ("bin", "^", node, ("num", 2.0))
            else:
                node = ("pct", node)
        return node

    def primary(self):
        t = self.peek()
        if t.kind == "NUMBER":
            self.advance()
            return ("num", t.value)
        if t.kind == "LPAREN":
            self.advance()
            node = self.addsub()
            self.expect("RPAREN")
            return node
        if t.kind == "IDENT":
            name = t.text
            self.advance()
            if name in CONSTANTS:
                return ("const", name)
            if self.peek().kind == "LPAREN":
                self.advance()
                args = [self.addsub()]
                while self.peek().kind == "COMMA":
                    self.advance()
                    args.append(self.addsub())
                self.expect("RPAREN")
                if name not in FUNCTIONS and name not in ("log",):
                    raise SyntaxErrorAx(f"Unknown function '{name}'.")
                return ("call", name, args)
            raise SyntaxErrorAx(f"Unknown identifier '{name}'.")
        if t.kind in ("PLUS", "MINUS"):
            return self.unary()
        if t.kind == "END":
            raise SyntaxErrorAx("Unexpected end of expression.")
        raise SyntaxErrorAx(f"Unexpected token '{t.text}'.")


MAX_FACTORIAL = 170  # beyond this, double overflows; BigInteger path used separately in Kotlin


def factorial_exact(n: int) -> int:
    if n < 0:
        raise DomainError("Factorial is undefined for negative numbers.")
    if n > 5000:
        raise ResourceLimitError("Factorial input exceeds the supported limit.")
    result = 1
    for k in range(2, n + 1):
        result *= k
    return result


@dataclass
class EvalContext:
    degrees: bool = True  # angle mode: True = DEG, False = RAD


def to_rad(x: float, ctx: EvalContext) -> float:
    return math.radians(x) if ctx.degrees else x


def from_rad(x: float, ctx: EvalContext) -> float:
    return math.degrees(x) if ctx.degrees else x


def evaluate(node, ctx: EvalContext) -> float:
    kind = node[0]
    if kind == "num":
        return node[1]
    if kind == "const":
        return CONSTANTS[node[1]]
    if kind == "neg":
        return -evaluate(node[1], ctx)
    if kind == "fact":
        v = evaluate(node[1], ctx)
        if v != int(v):
            raise DomainError("Factorial requires a non-negative integer.")
        return float(factorial_exact(int(v)))
    if kind == "pct":
        return evaluate(node[1], ctx) / 100.0
    if kind == "bin":
        op = node[1]
        a = evaluate(node[2], ctx)
        b = evaluate(node[3], ctx)
        if op == "+":
            return a + b
        if op == "-":
            return a - b
        if op == "*":
            return a * b
        if op == "/":
            if b == 0:
                raise DivisionByZeroError("Division by zero is undefined.")
            return a / b
        if op == "^":
            try:
                result = math.pow(a, b)
            except (ValueError, OverflowError):
                raise DomainError("Result of exponentiation is undefined or overflowed.")
            if isinstance(result, complex) or (isinstance(a, float) and a < 0 and not float(b).is_integer()):
                raise DomainError("Fractional power of a negative base is undefined in real mode.")
            return result
        raise SyntaxErrorAx(f"Unknown operator {op}")
    if kind == "call":
        name = node[1]
        args = [evaluate(a, ctx) for a in node[2]]
        return call_function(name, args, ctx)
    raise SyntaxErrorAx("Malformed expression.")


def call_function(name: str, args: List[float], ctx: EvalContext) -> float:
    if name in ("sin", "cos", "tan"):
        x = to_rad(args[0], ctx)
        if name == "tan" and abs(math.cos(x)) < 1e-12:
            raise DomainError("Tangent is undefined at this angle (asymptote).")
        return {"sin": math.sin, "cos": math.cos, "tan": math.tan}[name](x)
    if name in ("asin", "acos"):
        x = args[0]
        if x < -1 or x > 1:
            raise DomainError(f"{name} requires an argument between -1 and 1.")
        r = math.asin(x) if name == "asin" else math.acos(x)
        return from_rad(r, ctx)
    if name == "atan":
        return from_rad(math.atan(args[0]), ctx)
    if name == "sinh":
        return math.sinh(args[0])
    if name == "cosh":
        return math.cosh(args[0])
    if name == "tanh":
        return math.tanh(args[0])
    if name == "asinh":
        return math.asinh(args[0])
    if name == "acosh":
        if args[0] < 1:
            raise DomainError("acosh requires an argument >= 1.")
        return math.acosh(args[0])
    if name == "atanh":
        if args[0] <= -1 or args[0] >= 1:
            raise DomainError("atanh is undefined outside (-1, 1); the result diverges at +/-1.")
        return math.atanh(args[0])
    if name == "sqrt":
        if args[0] < 0:
            raise DomainError("Square root of a negative number is undefined in real mode.")
        return math.sqrt(args[0])
    if name == "cbrt":
        x = args[0]
        return math.copysign(abs(x) ** (1 / 3), x)
    if name == "abs":
        return abs(args[0])
    if name == "floor":
        return math.floor(args[0])
    if name == "ceil":
        return math.ceil(args[0])
    if name == "round":
        return float(round(args[0]))
    if name == "trunc":
        return math.trunc(args[0])
    if name == "ln":
        if args[0] <= 0:
            raise DomainError("Natural logarithm requires a positive argument.")
        return math.log(args[0])
    if name == "log10":
        if args[0] <= 0:
            raise DomainError("Base-10 logarithm requires a positive argument.")
        return math.log10(args[0])
    if name == "log2":
        if args[0] <= 0:
            raise DomainError("Base-2 logarithm requires a positive argument.")
        return math.log2(args[0])
    if name == "log":
        if len(args) == 1:
            if args[0] <= 0:
                raise DomainError("Logarithm requires a positive argument.")
            return math.log10(args[0])
        base = args[1]
        if args[0] <= 0 or base <= 0 or base == 1:
            raise DomainError("Logarithm requires a positive argument and a valid base.")
        return math.log(args[0]) / math.log(base)
    if name == "exp":
        return math.exp(args[0])
    raise SyntaxErrorAx(f"Unknown function '{name}'.")


def eval_str(src: str, degrees: bool = True) -> float:
    tokens = tokenize(src)
    tree = Parser(tokens).parse()
    return evaluate(tree, EvalContext(degrees=degrees))


# ---------------------------------------------------------------------------
# nPr / nCr
# ---------------------------------------------------------------------------

def permutations(n: int, r: int) -> int:
    if r < 0 or n < 0 or r > n:
        raise DomainError("Permutations require 0 <= r <= n.")
    return factorial_exact(n) // factorial_exact(n - r)


def combinations(n: int, r: int) -> int:
    if r < 0 or n < 0 or r > n:
        raise DomainError("Combinations require 0 <= r <= n.")
    return factorial_exact(n) // (factorial_exact(r) * factorial_exact(n - r))


# ---------------------------------------------------------------------------
# Complex numbers
# ---------------------------------------------------------------------------

@dataclass
class Cplx:
    re: float
    im: float

    def __add__(self, o): return Cplx(self.re + o.re, self.im + o.im)
    def __sub__(self, o): return Cplx(self.re - o.re, self.im - o.im)
    def __mul__(self, o): return Cplx(self.re * o.re - self.im * o.im, self.re * o.im + self.im * o.re)

    def div(self, o: "Cplx") -> "Cplx":
        denom = o.re * o.re + o.im * o.im
        if denom == 0:
            raise DivisionByZeroError("Division by zero complex number is undefined.")
        return Cplx((self.re * o.re + self.im * o.im) / denom, (self.im * o.re - self.re * o.im) / denom)

    def abs(self) -> float:
        return math.hypot(self.re, self.im)

    def arg(self) -> float:
        return math.atan2(self.im, self.re)

    def conj(self) -> "Cplx":
        return Cplx(self.re, -self.im)

    def fmt(self) -> str:
        re = round(self.re, 10)
        im = round(self.im, 10)
        if im == 0:
            return f"{re:g}"
        sign = "+" if im >= 0 else "-"
        return f"{re:g}{sign}{abs(im):g}i"


# ---------------------------------------------------------------------------
# Percentage formulas
# ---------------------------------------------------------------------------

def pct_x_percent_of_y(x: float, y: float) -> float:
    return (x / 100.0) * y


def pct_x_is_what_percent_of_y(x: float, y: float) -> float:
    if y == 0:
        raise DomainError("Percentage of zero is undefined.")
    return (x / y) * 100.0


def pct_increase(base: float, pct: float) -> float:
    return base * (1 + pct / 100.0)


def pct_decrease(base: float, pct: float) -> float:
    return base * (1 - pct / 100.0)


def pct_change(frm: float, to: float) -> float:
    if frm == 0:
        raise DomainError("Percentage change from zero is undefined.")
    return (to - frm) / frm * 100.0


def pct_reverse_original(final_value: float, pct_change_value: float) -> float:
    factor = 1 + pct_change_value / 100.0
    if factor == 0:
        raise DomainError("Reverse percentage is undefined for a -100% change.")
    return final_value / factor


def discount(price: float, pct: float) -> tuple:
    amt = price * pct / 100.0
    return amt, price - amt


def markup_selling_price(cost: float, markup_pct: float) -> float:
    return cost * (1 + markup_pct / 100.0)


def margin_cost(selling_price: float, margin_pct: float) -> float:
    return selling_price * (1 - margin_pct / 100.0)


def profit_margin(revenue: float, cost: float) -> float:
    if revenue == 0:
        raise DomainError("Profit margin is undefined for zero revenue.")
    return (revenue - cost) / revenue * 100.0


def markup_percent(cost: float, selling_price: float) -> float:
    if cost == 0:
        raise DomainError("Markup percentage is undefined for zero cost.")
    return (selling_price - cost) / cost * 100.0


# ---------------------------------------------------------------------------
# Finance
# ---------------------------------------------------------------------------

def simple_interest(principal: float, annual_rate_pct: float, years: float) -> tuple:
    interest = principal * (annual_rate_pct / 100.0) * years
    return interest, principal + interest


def compound_interest(principal: float, annual_rate_pct: float, years: float, compounds_per_year: int = 1) -> tuple:
    r = annual_rate_pct / 100.0
    total = principal * (1 + r / compounds_per_year) ** (compounds_per_year * years)
    return total - principal, total


def emi(principal: float, monthly_rate: float, n_payments: int) -> float:
    if monthly_rate == 0:
        return principal / n_payments
    r = monthly_rate
    factor = (1 + r) ** n_payments
    return principal * r * factor / (factor - 1)


# ---------------------------------------------------------------------------
# Geometry
# ---------------------------------------------------------------------------

def circle_area(r: float) -> float:
    return math.pi * r * r


def circle_circumference(r: float) -> float:
    return 2 * math.pi * r


def rectangle_area(w: float, h: float) -> float:
    return w * h


def rectangle_perimeter(w: float, h: float) -> float:
    return 2 * (w + h)


def triangle_valid(a: float, b: float, c: float) -> bool:
    return a > 0 and b > 0 and c > 0 and a + b > c and a + c > b and b + c > a


def triangle_area_heron(a: float, b: float, c: float) -> float:
    if not triangle_valid(a, b, c):
        raise DomainError("The given side lengths do not form a valid triangle.")
    s = (a + b + c) / 2
    return math.sqrt(s * (s - a) * (s - b) * (s - c))


def cube_volume(s: float) -> float:
    return s ** 3


def cube_surface_area(s: float) -> float:
    return 6 * s * s


def sphere_volume(r: float) -> float:
    return 4 / 3 * math.pi * r ** 3


def cylinder_volume(r: float, h: float) -> float:
    return math.pi * r * r * h


# ---------------------------------------------------------------------------
# Equation solving
# ---------------------------------------------------------------------------

def solve_linear(a: float, b: float) -> dict:
    # ax + b = 0
    if a == 0:
        if b == 0:
            return {"status": "infinite"}
        return {"status": "none"}
    return {"status": "unique", "x": -b / a}


def solve_quadratic_real(a: float, b: float, c: float) -> dict:
    if a == 0:
        return solve_linear(b, c)
    disc = b * b - 4 * a * c
    if disc > 0:
        sq = math.sqrt(disc)
        return {"status": "two_real", "roots": sorted([(-b - sq) / (2 * a), (-b + sq) / (2 * a)])}
    if disc == 0:
        return {"status": "one_real", "roots": [-b / (2 * a)]}
    return {"status": "no_real"}


def solve_quadratic_complex(a: float, b: float, c: float) -> dict:
    disc = b * b - 4 * a * c
    if disc >= 0:
        sq = math.sqrt(disc)
        return {"status": "complex", "roots": [Cplx((-b - sq) / (2 * a), 0), Cplx((-b + sq) / (2 * a), 0)]}
    sq = math.sqrt(-disc)
    return {"status": "complex", "roots": [Cplx(-b / (2 * a), -sq / (2 * a)), Cplx(-b / (2 * a), sq / (2 * a))]}


def solve_linear_system_2x2(a1, b1, c1, a2, b2, c2) -> dict:
    det = a1 * b2 - a2 * b1
    if det == 0:
        return {"status": "singular"}
    x = (c1 * b2 - c2 * b1) / det
    y = (a1 * c2 - a2 * c1) / det
    return {"status": "unique", "x": x, "y": y}


# ---------------------------------------------------------------------------
# Number theory
# ---------------------------------------------------------------------------

def gcd(a: int, b: int) -> int:
    return math.gcd(a, b)


def lcm(a: int, b: int) -> int:
    if a == 0 or b == 0:
        return 0
    return abs(a * b) // gcd(a, b)


def is_prime(n: int) -> bool:
    if n < 2:
        return False
    if n < 4:
        return True
    if n % 2 == 0:
        return False
    i = 3
    while i * i <= n:
        if n % i == 0:
            return False
        i += 2
    return True


def prime_factorize(n: int) -> List[tuple]:
    if n < 2:
        raise DomainError("Prime factorization requires an integer >= 2.")
    factors = []
    d = 2
    while d * d <= n:
        count = 0
        while n % d == 0:
            n //= d
            count += 1
        if count:
            factors.append((d, count))
        d += 1
    if n > 1:
        factors.append((n, 1))
    return factors


# ---------------------------------------------------------------------------
# Statistics
# ---------------------------------------------------------------------------

def stats_summary(data: List[float]) -> dict:
    if not data:
        raise DomainError("Statistics require a non-empty dataset.")
    n = len(data)
    s = sum(data)
    mean = s / n
    sorted_d = sorted(data)
    if n % 2 == 1:
        median = sorted_d[n // 2]
    else:
        median = (sorted_d[n // 2 - 1] + sorted_d[n // 2]) / 2
    pop_var = sum((x - mean) ** 2 for x in data) / n
    sample_var = sum((x - mean) ** 2 for x in data) / (n - 1) if n > 1 else float("nan")
    counts = {}
    for x in data:
        counts[x] = counts.get(x, 0) + 1
    max_count = max(counts.values())
    modes = sorted([k for k, v in counts.items() if v == max_count]) if max_count > 1 else []
    return {
        "count": n, "sum": s, "mean": mean, "median": median,
        "min": min(data), "max": max(data), "range": max(data) - min(data),
        "pop_var": pop_var, "pop_std": math.sqrt(pop_var),
        "sample_var": sample_var,
        "sample_std": math.sqrt(sample_var) if n > 1 else float("nan"),
        "modes": modes,
    }


# ---------------------------------------------------------------------------
# Matrix (square, via Gauss-Jordan)
# ---------------------------------------------------------------------------

def mat_mul(a: List[List[float]], b: List[List[float]]) -> List[List[float]]:
    ra, ca = len(a), len(a[0])
    rb, cb = len(b), len(b[0])
    if ca != rb:
        raise DomainError("Matrix dimensions are incompatible for multiplication.")
    result = [[0.0] * cb for _ in range(ra)]
    for i in range(ra):
        for j in range(cb):
            result[i][j] = sum(a[i][k] * b[k][j] for k in range(ca))
    return result


def mat_transpose(a: List[List[float]]) -> List[List[float]]:
    return [list(row) for row in zip(*a)]


def mat_det(a: List[List[float]]) -> float:
    n = len(a)
    m = [row[:] for row in a]
    det = 1.0
    for col in range(n):
        pivot = max(range(col, n), key=lambda r: abs(m[r][col]))
        if abs(m[pivot][col]) < 1e-12:
            return 0.0
        if pivot != col:
            m[col], m[pivot] = m[pivot], m[col]
            det *= -1
        det *= m[col][col]
        for r in range(col + 1, n):
            factor = m[r][col] / m[col][col]
            for c in range(col, n):
                m[r][c] -= factor * m[col][c]
    return det


def mat_inverse(a: List[List[float]]) -> List[List[float]]:
    n = len(a)
    aug = [row[:] + [1.0 if i == j else 0.0 for j in range(n)] for i, row in enumerate(a)]
    for col in range(n):
        pivot = max(range(col, n), key=lambda r: abs(aug[r][col]))
        if abs(aug[pivot][col]) < 1e-12:
            raise DomainError("Matrix is singular and cannot be inverted.")
        aug[col], aug[pivot] = aug[pivot], aug[col]
        pv = aug[col][col]
        aug[col] = [v / pv for v in aug[col]]
        for r in range(n):
            if r != col:
                factor = aug[r][col]
                aug[r] = [aug[r][k] - factor * aug[col][k] for k in range(2 * n)]
    return [row[n:] for row in aug]


# ---------------------------------------------------------------------------
# Unit conversion
# ---------------------------------------------------------------------------

LENGTH_TO_METERS = {
    "mm": 0.001, "cm": 0.01, "m": 1.0, "km": 1000.0,
    "in": 0.0254, "ft": 0.3048, "yd": 0.9144, "mi": 1609.344,
}


def convert_length(value: float, frm: str, to: str) -> float:
    return value * LENGTH_TO_METERS[frm] / LENGTH_TO_METERS[to]


def celsius_to_fahrenheit(c: float) -> float:
    return c * 9 / 5 + 32


def fahrenheit_to_celsius(f: float) -> float:
    return (f - 32) * 5 / 9


def celsius_to_kelvin(c: float) -> float:
    return c + 273.15


# ---------------------------------------------------------------------------
# Programmer tools
# ---------------------------------------------------------------------------

def to_base(n: int, base: int) -> str:
    if n == 0:
        return "0"
    digits = "0123456789ABCDEF"
    neg = n < 0
    n = abs(n)
    out = []
    while n:
        out.append(digits[n % base])
        n //= base
    if neg:
        out.append("-")
    return "".join(reversed(out))


def from_base(s: str, base: int) -> int:
    return int(s, base)
