// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.util.HashMap;
import java.util.Map;

/** Scheme's symbol */
public class Sym {
    private final String name;

    /** Construct a symbol that is not interned yet. */
    private Sym(String name) {
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    /** The table of interned symbols */
    public static final Map<String, Sym> symbols = new HashMap<>();

    /** Retrieve or construct an interned symbol. */
    public static Sym of(String name) {
        synchronized (symbols) {
            Sym sym = symbols.get(name);
            if (sym == null) {
                sym = new Sym(name);
                symbols.put(name, sym);
            }
            return sym;
        }
    }

    //----------------------------------------------------------------------

    static final Sym QUOTE = Sym.of("quote");
    static final Sym IF = Sym.of("if");
    static final Sym BEGIN = Sym.of("begin");
    static final Sym LAMBDA = Sym.of("lambda");
    static final Sym DEFINE = Sym.of("define");
    static final Sym SETQ = Sym.of("set!");
    static final Sym APPLY = Sym.of("apply");
    static final Sym CALLCC = Sym.of("call/cc");
}
