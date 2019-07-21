// R01.06.08/R01.07.21 by SUZUKI Hisao
package little_scheme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
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
                    ss.add("GlobalEnv");
                    break;
                } else if (env.sym == null) { // frame marker
                    ss.add("|");
                } else {
                    ss.add(env.sym.toString());
                }
            }
            return "#<" + String.join(" ", ss) + ">";
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
        Env env = GLOBAL_ENV.next; // Skip the frame marker.
        for (Env e: env)
            j = new Cell(e.sym, j);
        return j;
    }

    private static Env c(String name, int arity, Intrinsic.Body fun,
                                 Env next) {
        return new Env(Sym.of(name), new Intrinsic(name, arity, fun),
                               next);
    }

    private static Env G1 =
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
                  c("error", 2,
                    x -> {
                        throw new ErrorException(x.car,
                                                 ((Cell) x.cdr).car);
                    },
                    c("globals", 0, x -> globals(),
                      new Env
                      (Sym.CALLCC,
                       Sym.CALLCC,
                       new Env
                       (Sym.APPLY,
                        Sym.APPLY,
                        null)))))))));

    /** Scheme's global environment  */
    public static final Env GLOBAL_ENV = 
        new Env
        (null,                  // frame marker
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
                               c("read", 0, x -> readExpression("", ""),
                                 c("eof-object?", 1, x -> x.car == EOF,
                                   c("symbol?", 1, x -> x.car instanceof Sym,
                                     G1)))))))))))))));

    //----------------------------------------------------------------------

    /** Split a string into a list of tokens.
        For "(a 1)" it returns ["(", "a", "1", ")"].
     */
    public static Queue<String> splitStringIntoTokens(String source) {
        Queue<String> result = new ArrayDeque<>();
        for (String line: source.split("\n")) {
            Queue<String> ss = new ArrayDeque<>(); // to store string literals
            List<String> x = new ArrayList<>();
            int i = 0;
            for (String e: line.split("\"")) {
                if (i % 2 == 0) {
                    x.add(e); 
                } else {
                    ss.add("\"" + e); // Store a string literal.
                    x.add("#s");
                }
                i++;
            }
            String s = String.join(" ", x).split(";")[0]; // Ignore ;-comment.
            s = s.replaceAll("'", " ' ");
            s = s.replaceAll("\\)", " ) ");
            s = s.replaceAll("\\(", " ( ");
            for (String e: s.split("\\s+")) 
                if (e.equals("#s"))
                    result.add(ss.remove());
                else if (! e.isEmpty())
                    result.add(e);
        }
        return result;
    }

    /** Read an expression from tokens.
        Tokens will be left with the rest of token strings, if any.
    */
    public static Object readFromTokens(Queue<String> tokens) {
        String token = tokens.remove();
        switch (token) {
        case "(": {
            Cell z = new Cell(null, null);
            Cell y = z;
            while (! tokens.element().equals(")")) {
                if (tokens.element().equals(".")) {
                    tokens.remove();
                    y.cdr = readFromTokens(tokens);
                    if (! tokens.element().equals(")"))
                        throw new RuntimeException(") is expected");
                    break;
                }
                Object e = readFromTokens(tokens);
                Cell x = new Cell(e, null);
                y.cdr = x;
                y = x;
            }
            tokens.remove();
            return z.cdr;
        }
        case ")":
            throw new RuntimeException("unexpected )");
        case "'": {
            Object e = readFromTokens(tokens);
            return new Cell(Sym.QUOTE, new Cell(e, null)); // (quote e)
        }
        case "#f":
            return Boolean.FALSE;
        case "#t":
            return Boolean.TRUE;
        }
        if (token.charAt(0) == '"')
            return token.substring(1);
        try {
            return Arith.parse(token);
        } catch (NumberFormatException ex) {}
        return Sym.of(token);
    }

    //----------------------------------------------------------------------

    /** Tokens from the standard-in */
    private static Queue<String> stdInTokens = new ArrayDeque<>();

    /** A buffered reader for the standard-in */
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    /** Read an expression from the standard-in. */
    public static Object readExpression(String prompt1, String prompt2)
        throws IOException
    {
        for (;;) {
            Queue<String> old = new ArrayDeque<>(stdInTokens);
            try {
                return readFromTokens(stdInTokens);
            } catch (NoSuchElementException ex) {
                System.out.print(old.isEmpty() ? prompt1 : prompt2);
                System.out.flush();
                String line = STDIN.readLine();
                if (line == null) // EOF
                    return EOF;
                stdInTokens = old;
                stdInTokens.addAll(splitStringIntoTokens(line));
            } catch (Exception ex) {
                stdInTokens.clear(); // Discard the erroneous tokens.
                throw ex;
            }
        }
    }

    //----------------------------------------------------------------------

    /** Repeat Read-Eval-Print until End-Of-File. */
    public static void readEvalPrintLoop() throws IOException {
        for (;;) {
            try {
                Object exp = readExpression("> ", "| ");
                if (exp == EOF) {
                    System.out.println("Goodbye");
                    return;
                }
                Object result = new Eval(exp, GLOBAL_ENV).evaluate();
                if (result != NONE)
                    System.out.println(stringify(result));
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /** Load a source code from a file. */
    public static void load(String fileName) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(fileName)),
                                   StandardCharsets.UTF_8);
        Queue<String> tokens = splitStringIntoTokens(source);
        while (! tokens.isEmpty()) {
            Object exp = readFromTokens(tokens);
            new Eval(exp, GLOBAL_ENV).evaluate();
        }
    }
}
