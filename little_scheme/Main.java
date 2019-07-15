// R01.06.08/R01.07.15 by SUZUKI Hisao
package little_scheme;

/** This class contains the main method of the interpreter. */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            LS.load(args[0]);
            if (! (args.length > 1 && args[1].equals("-")))
                return;
        }
        LS.readEvalPrintLoop();
    }
}