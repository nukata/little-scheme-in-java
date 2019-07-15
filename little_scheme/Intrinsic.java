// R01.06.08/R01.07.15 by SUZUKI Hisao
package little_scheme;

import java.io.IOException;

/** Built-in function */
public class Intrinsic {
    public final String name;
    public final int arity;
    public final Body fun;

    public Intrinsic(String name, int arity, Body fun) {
        this.name = name;
        this.arity = arity;
        this.fun = fun;
    }

    @Override public String toString() {
        return "#<" + name + ":" + arity + ">";
    }

    /** Body of a built-in function */
    @FunctionalInterface public static interface Body {
        Object call(Cell args) throws IOException;
    }
}
