// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import little_arith.Arith;

/** Reader of Scheme's expression */
public class Read {
    private Read() {}

    /** Split a string into a list of tokens.
        For "(a 1") it returns ["(", "a", "1", ")"].
     */
    public static Deque<String> splitStringIntoTokens(String source) {
        Deque<String> result = new ArrayDeque<>();
        for (String line: source.split("\n")) {
            Deque<String> ss = new ArrayDeque<>(); // to store string literals
            List<String> x = new ArrayList<>();
            int i = 0;
            for (String e: line.split("\"")) {
                if (i % 2 == 0) {
                    x.add(e); 
                } else {
                    ss.addLast("\"" + e); // Store a string literal.
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
                    result.addLast(ss.removeFirst());
                else if (! e.isEmpty())
                    result.addLast(e);
        }
        return result;
    }

    /** Read an expression from tokens.
        Tokens will be left with the rest of token strings, if any.
    */
    public static Object readFromTokens(Deque<String> tokens) {
        String token = tokens.removeFirst();
        switch (token) {
        case "(": {
            Cell z = new Cell(null, null);
            Cell y = z;
            while (! tokens.getFirst().equals(")")) {
                if (tokens.getFirst().equals(".")) {
                    tokens.removeFirst();
                    y.cdr = readFromTokens(tokens);
                    if (! tokens.getFirst().equals(")"))
                        throw new RuntimeException(") is expected");
                    break;
                }
                Object e = readFromTokens(tokens);
                Cell x = new Cell(e, null);
                y.cdr = x;
                y = x;
            }
            tokens.removeFirst();
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
    private static Deque<String> stdInTokens = new ArrayDeque<>();

    /** A buffered reader for the standard-in */
    private static final BufferedReader STDIN = 
        new BufferedReader(new InputStreamReader(System.in));

    /** Read an expression from the standard-in. */
    public static Object readExpression(String prompt1, String prompt2)
        throws IOException
    {
        for (;;) {
            Deque<String> old = new ArrayDeque<>(stdInTokens);
            try {
                return readFromTokens(stdInTokens);
            } catch (NoSuchElementException ex) {
                System.out.print(old.isEmpty() ? prompt1 : prompt2);
                System.out.flush();
                String line = STDIN.readLine();
                if (line == null) // EOF
                    return LS.EOF;
                stdInTokens = old;
                stdInTokens.addAll(splitStringIntoTokens(line));
            } catch (Exception ex) {
                stdInTokens.clear(); // Discard the erroneous tokens.
                throw ex;
            }
        }
    }
}