// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Linked list of bindings which map symbols to values */
public class Env implements Iterable<Env> {
    public final Sym sym;
    public Object val;
    public Env next;

    public Env(Sym sym, Object val, Env next) {
        this.sym = sym;
        this.val = val;
        this.next = next;
    }

    /** Yield each binding. */
    public Iterator<Env> iterator() {
        return new Iterator<Env>() {
            Env env = Env.this;

            public boolean hasNext() {
                return env != null;
            }

            public Env next() {
                if (env == null)
                    throw new NoSuchElementException();
                Env current = env;
                env = env.next;
                return current;
            }
        };
    }

    /** Search the binding for a symbol. */
    public Env lookFor(Sym symbol) {
        for (Env env: this)
            if (env.sym == symbol)
                return env;
        throw new RuntimeException("name not found: " + symbol);
    }

    /** Build an environment prepending the binding of symbols and data. */
    public Env prependDefs(Cell symbols, Cell data) {
        if (symbols == null) {
            if (data != null)
                throw new RuntimeException("surplus arg: " +
                                           LS.stringify(data));
            return this;
        } else {
            if (data == null)
                throw new RuntimeException("surplus param: " +
                                           LS.stringify(symbols));
            Env next = prependDefs((Cell) symbols.cdr,
                                   (Cell) data.cdr);
            return new Env((Sym) symbols.car, data.car, next);
        }
    }
}
