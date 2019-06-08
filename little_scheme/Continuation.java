// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/** Scheme's continuation as a stack of steps */
public class Continuation implements Iterable<Step> {
    private final Deque<Step> stack = new ArrayDeque<>();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    /** Yield each step. */
    public Iterator<Step> iterator() {
        return stack.iterator();
    }

    /** Append a step to the tail of the continuation. */
    public void push(Step.Op op, Object value) {
        stack.addLast(new Step(op, value));
    }

    /** Pop a step from the tail of the continuation. */
    public Step pop() {
        return stack.removeLast();
    }

    /** Copy a continuation. */
    public void copyFrom(Continuation other) {
        stack.clear();
        stack.addAll(other.stack);
    }

    /** Push RESTORE_ENV unless on a tail call. */
    public void pushRestoreEnv(Env env) {
        Step last = stack.peekLast();
        if (last == null || last.op != Step.Op.RESTORE_ENV)
            push(Step.Op.RESTORE_ENV, env);
    }
}

