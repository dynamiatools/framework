/**
 * Exact decimal numbers (an integer and a scale), the counterpart of Java's BigDecimal for the few operations the
 * template language needs. Floating point is never used for arithmetic, so `0.1 + 0.2` is `0.3` as in Java.
 */

export type RoundingMode = 'HALF_UP' | 'HALF_EVEN';

const TEN = 10n;

function pow10(n: number): bigint {
  return TEN ** BigInt(n);
}

function abs(n: bigint): bigint {
  return n < 0n ? -n : n;
}

/** Divides and rounds the quotient to an integer. */
function divRound(num: bigint, den: bigint, mode: RoundingMode): bigint {
  const q = num / den;
  const r = num % den;
  if (r === 0n) {
    return q;
  }
  const twice = abs(r) * 2n;
  const negative = (num < 0n) !== (den < 0n);
  const away = negative ? q - 1n : q + 1n;
  const cmp = twice === abs(den) ? 0 : twice > abs(den) ? 1 : -1;
  if (cmp > 0) {
    return away;
  }
  if (cmp < 0) {
    return q;
  }
  if (mode === 'HALF_UP') {
    return away;
  }
  return q % 2n === 0n ? q : away;
}

const PLAIN = /^[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?$/;

export class Decimal {
  /** value = unscaled / 10^scale, scale >= 0 */
  private constructor(
    readonly unscaled: bigint,
    readonly scale: number,
  ) {}

  static readonly ZERO = new Decimal(0n, 0);

  static of(unscaled: bigint, scale = 0): Decimal {
    return new Decimal(unscaled, scale);
  }

  /** Parses what Java's `new BigDecimal(String)` accepts (after trim); null when it is not a number. */
  static parse(text: string): Decimal | null {
    const t = text.trim();
    if (!PLAIN.test(t)) {
      return null;
    }
    const m = /^([+-]?)(\d*)(?:\.(\d*))?(?:[eE]([+-]?\d+))?$/.exec(t);
    if (!m) {
      return null;
    }
    const frac = m[3] ?? '';
    let unscaled = BigInt((m[2] ?? '') + frac || '0');
    let scale = frac.length - Number(m[4] ?? 0);
    if (scale < 0) {
      unscaled *= pow10(-scale);
      scale = 0;
    }
    return new Decimal(m[1] === '-' ? -unscaled : unscaled, scale);
  }

  /** The shortest text of the number, as Java's `Double.toString` feeds `new BigDecimal(n.toString())`. */
  static fromNumber(n: number): Decimal | null {
    return Number.isFinite(n) ? Decimal.parse(String(n)) : null;
  }

  /** The exact binary value of a double, which is what Java's DecimalFormat rounds. */
  static exact(n: number): Decimal | null {
    if (!Number.isFinite(n)) {
      return null;
    }
    if (Number.isInteger(n) && Math.abs(n) < Number.MAX_SAFE_INTEGER) {
      return new Decimal(BigInt(n), 0);
    }
    const view = new DataView(new ArrayBuffer(8));
    view.setFloat64(0, n);
    const bits = view.getBigUint64(0);
    const negative = bits >> 63n === 1n;
    const exponent = Number((bits >> 52n) & 0x7ffn);
    let mantissa = bits & 0xfffffffffffffn;
    let e2: number;
    if (exponent === 0) {
      e2 = -1074;
    } else {
      mantissa |= 1n << 52n;
      e2 = exponent - 1075;
    }
    let value: Decimal;
    if (e2 >= 0) {
      value = new Decimal(mantissa << BigInt(e2), 0);
    } else {
      value = new Decimal(mantissa * 5n ** BigInt(-e2), -e2).strip();
    }
    return negative ? value.negate() : value;
  }

  signum(): number {
    return this.unscaled === 0n ? 0 : this.unscaled < 0n ? -1 : 1;
  }

  negate(): Decimal {
    return new Decimal(-this.unscaled, this.scale);
  }

  private align(o: Decimal): [bigint, bigint, number] {
    const scale = Math.max(this.scale, o.scale);
    return [this.unscaled * pow10(scale - this.scale), o.unscaled * pow10(scale - o.scale), scale];
  }

  add(o: Decimal): Decimal {
    const [a, b, s] = this.align(o);
    return new Decimal(a + b, s);
  }

  subtract(o: Decimal): Decimal {
    const [a, b, s] = this.align(o);
    return new Decimal(a - b, s);
  }

  multiply(o: Decimal): Decimal {
    return new Decimal(this.unscaled * o.unscaled, this.scale + o.scale);
  }

  /** Quotient with exactly `scale` decimals; the divisor must not be zero. */
  divide(o: Decimal, scale: number, mode: RoundingMode): Decimal {
    const num = this.unscaled * pow10(o.scale + scale);
    const den = o.unscaled * pow10(this.scale);
    return new Decimal(divRound(num, den, mode), scale);
  }

  compareTo(o: Decimal): number {
    const [a, b] = this.align(o);
    return a === b ? 0 : a < b ? -1 : 1;
  }

  /** Rounds to `scale` decimals (never adds precision beyond the current one: it pads with zeros). */
  setScale(scale: number, mode: RoundingMode): Decimal {
    if (scale >= this.scale) {
      return new Decimal(this.unscaled * pow10(scale - this.scale), scale);
    }
    return new Decimal(divRound(this.unscaled, pow10(this.scale - scale), mode), scale);
  }

  /** Without trailing zeros: 10, 1.5 (Java's stripTrailingZeros followed by setScale(0) when the scale is negative). */
  strip(): Decimal {
    if (this.unscaled === 0n) {
      return Decimal.ZERO;
    }
    let u = this.unscaled;
    let s = this.scale;
    while (s > 0 && u % TEN === 0n) {
      u /= TEN;
      s--;
    }
    return new Decimal(u, s);
  }

  /** Digits with a point and no exponent. */
  toPlainString(): string {
    const digits = abs(this.unscaled).toString().padStart(this.scale + 1, '0');
    const int = digits.slice(0, digits.length - this.scale);
    const frac = digits.slice(digits.length - this.scale);
    return (this.unscaled < 0n ? '-' : '') + int + (this.scale > 0 ? '.' + frac : '');
  }

  /** The same text as Java's `BigDecimal.toString()`, which switches to an exponent for very small values. */
  toString(): string {
    const digits = abs(this.unscaled).toString();
    const adjusted = -this.scale + (digits.length - 1);
    if (this.scale === 0 || adjusted >= -6) {
      return this.toPlainString();
    }
    const mantissa = digits.length > 1 ? digits[0] + '.' + digits.slice(1) : digits;
    return (this.unscaled < 0n ? '-' : '') + mantissa + 'E' + (adjusted >= 0 ? '+' : '') + adjusted;
  }
}
