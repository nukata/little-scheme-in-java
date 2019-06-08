// R01.06.08 by SUZUKI Hisao
package little_arith;

import java.math.BigInteger;

/** Mixed mode arithmetic of int, double and BigInteger */
public class Arith {
    private Arith() {}

    /** Convert a long into an int or a BigInteger. */
    static Number normalize(long x) {
        int i = (int) x;
        return (i == x) ? i : BigInteger.valueOf(x);
    }

    /** Convert a BigInteger into an int if possible. */
    static Number normalize(BigInteger x) {
        return (x.bitLength() < 32) ? x.intValue() : x;
    }

    /** x + y */
    public static Number add(Number x, Number y) {
        if (x instanceof Integer && y instanceof Integer) {
            long a = x.longValue() + y.longValue();
            return normalize(a);
        } else if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() + y.doubleValue();
        } else {
            if (x instanceof Integer)
                x = BigInteger.valueOf(x.longValue());
            else if (y instanceof Integer)
                y = BigInteger.valueOf(y.longValue());
            BigInteger b = ((BigInteger) x).add((BigInteger) y);
            return normalize(b);
        }
    }

    /** x - y */
    public static Number subtract(Number x, Number y) {
        if (x instanceof Integer && y instanceof Integer) {
            long a = x.longValue() - y.longValue();
            return normalize(a);
        } else if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() - y.doubleValue();
        } else {
            if (x instanceof Integer)
                x = BigInteger.valueOf(x.longValue());
            else if (y instanceof Integer)
                y = BigInteger.valueOf(y.longValue());
            BigInteger b = ((BigInteger) x).subtract((BigInteger) y);
            return normalize(b);
        }
    }

    /** x * y */
    public static Number multiply(Number x, Number y) {
        if (x instanceof Integer && y instanceof Integer) {
            long a = x.longValue() * y.longValue();
            return normalize(a);
        } else if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() * y.doubleValue();
        } else {
            if (x instanceof Integer)
                x = BigInteger.valueOf(x.longValue());
            else if (y instanceof Integer)
                y = BigInteger.valueOf(y.longValue());
            BigInteger b = ((BigInteger) x).multiply((BigInteger) y);
            return normalize(b);
        }
    }

    /** Compare x and y.
        @return -1, 0 or 1 as x is less than, equal to, or greater than y.
     */
    public static int compare(Number x, Number y) {
        if (x instanceof Integer && y instanceof Integer) {
            long a = x.longValue() - y.longValue();
            return (a < 0) ? -1 : (a > 0) ? 1 : 0;
        } else if (x instanceof Double || y instanceof Double) {
            double a = x.doubleValue() - y.doubleValue();
            return (a < 0.0) ? -1 : (a > 0.0) ? 1 : 0;
        } else {
            if (x instanceof Integer)
                x = BigInteger.valueOf(x.longValue());
            else if (y instanceof Integer)
                y = BigInteger.valueOf(y.longValue());
            return ((BigInteger) x).compareTo((BigInteger) y);
        }
    }

    /** Parse a string as an int, a BigInteger or a double. */
    public static Number parse(String s) throws NumberFormatException {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex) {}
        try {
            return new BigInteger(s);
        } catch (NumberFormatException ex) {}
        return Double.valueOf(s);
    }
}
