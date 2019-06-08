// R01.06.08 by SUZUKI Hisao
package little_scheme;

/** Lambda expression with its environment */
public class Closure {
    public final Cell params;
    public final Cell body;
    public final Env env;

    public Closure(Cell params, Cell body, Env env) {
        this.params = params;
        this.body = body;
        this.env = env;
    }
}
