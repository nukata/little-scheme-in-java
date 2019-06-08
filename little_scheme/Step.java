// R01.06.08 by SUZUKI Hisao
package little_scheme;

/** Scheme's step in a continuation */
public class Step {

    /** Operation of a step */
    public enum Op {
        THEN, BEGIN, DEFINE, SETQ, APPLY,
            APPLY_FUN, EVAL_ARG, PUSH_ARG, RESTORE_ENV;
    }

    public final Op op;
    public final Object val;

    public Step(Op op, Object val) {
        this.op = op;
        this.val = val;
    }
}
