// R01.06.08/R01.07.15 by SUZUKI Hisao
package little_scheme;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Operations in continuations */
enum ContOp { // internal
    THEN, BEGIN, DEFINE, SETQ, APPLY, APPLY_FUN, EVAL_ARG, CONS_ARGS,
        RESTORE_ENV;
}

/** Scheme's step in a continuation */
class Step { // internal
    final ContOp op;
    final Object val;

    Step(ContOp op, Object val) {
        this.op = op;
        this.val = val;
    }
}

/** Scheme's continuation as a stack of steps */
public class Continuation {
    private final Deque<Step> stack;

    /** Construct an empty continuation. */
    public Continuation() {
        stack = new ArrayDeque<Step>();
    }

    /** Construct a copy of another continuation. */
    public Continuation(Continuation other) {
        stack = new ArrayDeque<Step>(other.stack);
    }

    /** Return true if this continuation contains no steps. */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /** Return the number of steps in this continuation. */
    public int size() {
        return stack.size();
    }

    @Override public String toString() {
        List<String> ss = new ArrayList<>();
        for (Step step: stack)
            ss.add(step.op + " " + LS.stringify(step.val));
        return "#<" + String.join("\n\t  ", ss) + ">";
    }

    /** Append a step to the tail of the continuation. */
    void push(ContOp op, Object value) { // internal
        stack.push(new Step(op, value));
    }

    /** Pop a step from the tail of the continuation. */
    Step pop() {                // internal
        return stack.pop();
    }

    /** Push RESTORE_ENV unless on a tail call. */
    void pushRestoreEnv(Env env) { // internal
        Step last = stack.peek();
        if (last == null || last.op != ContOp.RESTORE_ENV)
            push(ContOp.RESTORE_ENV, env);
    }
}

