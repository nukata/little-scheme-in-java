// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

public class Main {
    /** Load a source code from a file. */
    private static void load(String fileName) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(fileName)),
                                   StandardCharsets.UTF_8);
        Deque<String> tokens = Read.splitStringIntoTokens(source);
        while (! tokens.isEmpty()) {
            Object exp = Read.readFromTokens(tokens);
            Eval.evaluate(exp, LS.GLOBAL_ENV);
        }
    }

    /** Repeat Read-Eval-Print until End-Of-File. */
    private static void readEvalPrintLoop() throws IOException {
        for (;;) {
            try {
                Object exp = Read.readExpression("> ", "| ");
                if (exp == LS.EOF) {
                    System.out.println("Goodbye");
                    return;
                }
                Object result = Eval.evaluate(exp, LS.GLOBAL_ENV);
                if (result != LS.NONE)
                    System.out.println(LS.stringify(result));
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            load(args[0]);
            if (! (args.length > 1 && args[1].equals("-")))
                return;
        }
        readEvalPrintLoop();
    }
}