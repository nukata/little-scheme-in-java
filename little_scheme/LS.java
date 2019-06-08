// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.util.ArrayList;
import java.util.List;
import little_arith.Arith;

/** Little Scheme's common values and functions */
public class LS {
    private LS() {}

    /** A unique value which means the expression has no value */
    public static final Object NONE = new Object();

    /** A unique value which means the End Of File */
    public static final Object EOF = new Object();

    /** Convert an expression to a string (with quotes). */
    public static String stringify(Object exp) {
        return stringify(exp, true);
    }

    /** Convert an expression to a string. */
    public static String stringify(Object exp, boolean quote) {
        if (exp == Boolean.TRUE) {
            return "#t";
        } else if (exp == Boolean.FALSE) {
            return "#f";
        } else if (exp == NONE) {
            return "#<VOID>";
        } else if (exp == EOF) {
            return "#<EOF>";
        } else if (exp == null) {
            return "()";
        } else if (exp  instanceof Cell) {
            List<String> ss = new ArrayList<>();
            try {
                for (Object e: (Cell) exp)
                    ss.add(stringify(e, quote));
            } catch (Cell.ImproperListException ex) {
                ss.add(".");
                ss.add(stringify(ex.tail, quote));
            }
            return "(" + String.join(" ", ss) + ")";
        } else if (exp instanceof Env) {
            List<String> ss = new ArrayList<>();
            for (Env env: (Env) exp) {
                if (env == GLOBAL_ENV) {
                    ss.add("GloblEnv");
                    break;
                } else if (env.sym == null) { // marker of the frame top
                    ss.add("|");
                } else {
                    ss.add(env.sym.toString());
                }
            }
            return "#<" + String.join(" ", ss) + ">";
        } else if (exp instanceof Continuation) {
            List<String> ss = new ArrayList<>();
            for (Step step: (Continuation) exp)
                ss.add(step.op + " " + stringify(step.val));
            return "#<" + String.join("\n\t ", ss) + ">";
        } else if (exp instanceof Closure) {
            Closure f = (Closure) exp;
            return "#<" + stringify(f.params) +
                ":" + stringify(f.body) +
                ":" + stringify(f.env) + ">";
        } else if ((exp instanceof String) && quote) {
            return "\"" + (String) exp + "\"";
        }
        return exp.toString();
    }

    //----------------------------------------------------------------------

    /** Return a list of symbols of the global environment. */
    private static Cell globals() {
        Cell j = null;
        Env env = GLOBAL_ENV.next; // Skip the marker.
        for (Env e: env)
            j = new Cell(e.sym, j);
        return j;
    }

    private static Env c(String name, int arity, Intrinsic.Body fun,
                                 Env next) {
        return new Env(Sym.of(name), new Intrinsic(name, arity, fun),
                               next);
    }

    private static Env g1 =
        c("+", 2,
          x -> Arith.add((Number) x.car,
                         (Number) ((Cell) x.cdr).car),
          c("-", 2,
            x -> Arith.subtract((Number) x.car,
                                (Number) ((Cell) x.cdr).car),
            c("*", 2,
              x -> Arith.multiply((Number) x.car,
                                  (Number) ((Cell) x.cdr).car),
              c("<", 2,
                x -> Arith.compare((Number) x.car,
                                   (Number) ((Cell) x.cdr).car) < 0,
                c("=", 2,
                  x -> Arith.compare((Number) x.car,
                                     (Number) ((Cell) x.cdr).car) == 0,
                  c("globals", 0, x -> globals(),
                    new Env
                    (Sym.CALLCC,
                     Sym.CALLCC,
                     new Env
                     (Sym.APPLY,
                      Sym.APPLY,
                      null))))))));

    /** Scheme's global environment  */
    public static final Env GLOBAL_ENV = 
        new Env
        (null,                  // marker of the frame top
         null,
         c("car", 1, x -> ((Cell) x.car).car,
           c("cdr", 1, x -> ((Cell) x.car).cdr,
             c("cons", 2, x -> new Cell(x.car, ((Cell) x.cdr).car),
               c("eq?", 2, x -> x.car == ((Cell) x.cdr).car,
                 c("eqv?", 2, 
                   x -> {
                       Object a = x.car;
                       Object b = ((Cell) x.cdr).car;
                       if (a == b) {
                           return true;
                       } else if (a instanceof Number && b instanceof Number) {
                           int c = Arith.compare((Number) a, (Number) b);
                           return c == 0;
                       } else {
                           return false;
                       }
                   },
                   c("pair?", 1, x -> x.car instanceof Cell,
                     c("null?", 1, x -> x.car == null,
                       c("not", 1, x -> x.car == Boolean.FALSE,
                         c("list", -1, x -> x,
                           c("display", 1,
                             x -> {
                                 System.out.print(stringify(x.car, false));
                                 return NONE;
                             },
                             c("newline", 0,
                               x -> {
                                   System.out.println();
                                   return NONE;
                               },
                               c("read", 0, x -> Read.readExpression("", ""),
                                 c("eof-object?", 1, x -> x.car == EOF,
                                   c("symbol?", 1, x -> x.car instanceof Sym,
                                     g1)))))))))))))));
}
