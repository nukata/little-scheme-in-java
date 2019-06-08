// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.io.IOException;

/** Evaluator of Scheme's expression */
public class Eval
{
    /** Evaluate an expression in an environment. */
    public static Object evaluate(Object exp, Env env) throws IOException {
        Continuation k = new Continuation();
        try {
            for (;;) {
                for (;;) {
                    if (exp instanceof Cell) {
                        Object kar = ((Cell) exp).car;
                        Cell kdr = (Cell) ((Cell) exp).cdr;
                        if (kar == Sym.QUOTE) { // (quote e)
                            exp = kdr.car;
                            break;
                        } else if (kar == Sym.IF) { // (if e1 e2 [e3])
                            exp = kdr.car;
                            k.push(Step.Op.THEN, kdr.cdr);
                        } else if (kar == Sym.BEGIN) { // (begin e...)
                            exp = kdr.car;
                            if (kdr.cdr != null)
                                k.push(Step.Op.BEGIN, kdr.cdr);
                        } else if (kar == Sym.LAMBDA) { // (lambda (v..) e...)
                            Cell params = (Cell) kdr.car;
                            Cell body = (Cell) kdr.cdr;
                            exp = new Closure(params, body, env);
                            break;
                        } else if (kar == Sym.DEFINE) { // (define v e)
                            exp = ((Cell) kdr.cdr).car;
                            k.push(Step.Op.DEFINE, kdr.car);
                        } else if (kar == Sym.SETQ) {   // (set! v e)
                            exp = ((Cell) kdr.cdr).car;
                            Sym v = (Sym) kdr.car;
                            k.push(Step.Op.SETQ, env.lookFor(v));
                        } else { // (fun arg...)
                            exp = kar;
                            k.push(Step.Op.APPLY, kdr);
                        }
                    } else if (exp instanceof Sym) {
                        exp = env.lookFor((Sym) exp).val;
                        break;
                    } else {    // a number, #t, #f etc.
                        break;
                    }
                }
                LOOP2: for (;;) {
                    if (k.isEmpty())
                        return exp;
                    Step step = k.pop();
                    Step.Op op = step.op;
                    Object x = step.val;
                    switch (op) {
                    case THEN: { // x is (e2 e3).
                        Cell c = (Cell) x;
                        if (exp == Boolean.FALSE) {
                            if (c.cdr == null) {
                                exp = LS.NONE;
                                break;
                            } else {
                                exp = ((Cell) c.cdr).car; // e3
                                break LOOP2;
                            }
                        } else {
                            exp = c.car; // e2
                            break LOOP2;
                        }
                    }
                    case BEGIN: { // x is (e...).
                        Cell c = (Cell) x;
                        if (c.cdr != null) // Unless on a tail call...
                            k.push(Step.Op.BEGIN, c.cdr);
                        exp = c.car;
                        break LOOP2;
                    }
                    case DEFINE: // x is a variable name.
                        assert env.sym == null; // Check for the frame top.
                        env.next = new Env((Sym) x, exp, env.next);
                        exp = LS.NONE;
                        break;
                    case SETQ:  // x is an Environment.
                        ((Env) x).val = exp;
                        exp = LS.NONE;
                        break;
                    case APPLY: // x is a list of args; exp is a function.
                        if (x == null) {
                            REPair pair = applyFunction(exp, null, k, env);
                            exp = pair.result;
                            env = pair.env;
                            break;
                        } else {
                            k.push(Step.Op.APPLY_FUN, exp);
                            Cell c = (Cell) x;
                            while (c.cdr != null) {
                                k.push(Step.Op.EVAL_ARG, c.car);
                                c = (Cell) c.cdr;
                            }
                            exp = c.car;
                            k.push(Step.Op.PUSH_ARG, null);
                            break LOOP2;
                        }
                    case PUSH_ARG: { // x is a list of evaluated args.
                        Cell args = new Cell(exp, x);
                        step = k.pop();
                        op = step.op;
                        exp = step.val;
                        switch (op) {
                        case EVAL_ARG: // exp is the next arg.
                            k.push(Step.Op.PUSH_ARG, args);
                            break LOOP2;
                        case APPLY_FUN: { // exp is the evaluated function.
                            REPair pair = applyFunction(exp, args, k, env);
                            exp = pair.result;
                            env = pair.env;
                            break;
                        }
                        default:
                            throw new RuntimeException("unexpected op: " + op);
                        }
                        break;
                    }
                    case RESTORE_ENV: // x is an Environment.
                        env = (Env) x;
                        break;
                    default:
                        throw new RuntimeException("bad op: " + op);
                    }
                }
            }
        } catch (Exception ex) {
            if (k.isEmpty())
                throw ex;
            throw new RuntimeException
                (ex.getMessage() + "\n\t" + LS.stringify(k), ex);
        }
    }

    //----------------------------------------------------------------------

    private static class REPair {
        final Object result;
        final Env env;

        REPair(Object result, Env env) {
            this.result = result;
            this.env = env;
        }
    }

    /** Apply a function to arguments with a continuation.
        An environment will be referred to push Step.Op.RESTORE_ENV to
        the continuation.
     */
    private static REPair applyFunction(Object fun, Cell arg, Continuation k,
                                        Env env) throws IOException {
        for (;;) {
            if (fun == Sym.CALLCC) {
                k.pushRestoreEnv(env);
                fun = arg.car;
                Continuation cont = new Continuation();
                cont.copyFrom(k);
                arg = new Cell(cont, null);
            } else if (fun == Sym.APPLY) {
                fun = arg.car;
                arg = (Cell) ((Cell) arg.cdr).car;
            } else {
                break;
            }
        }
        if (fun instanceof Intrinsic) {
            Intrinsic f = (Intrinsic) fun;
            if (f.arity >= 0)
                if (arg == null ? f.arity > 0 : arg.size() != f.arity)
                    throw new RuntimeException("arity not matched: " + f +
                                               " and " + LS.stringify(arg));
            return new REPair(f.fun.call(arg), env);
        } else if (fun instanceof Closure) {
            Closure f = (Closure) fun;
            k.pushRestoreEnv(env);
            k.push(Step.Op.BEGIN, f.body);
            return new REPair
                (LS.NONE,
                 new Env(null, // marker of the frame top
                         null,
                         f.env.prependDefs(f.params, arg)));
        } else if (fun instanceof Continuation) {
            k.copyFrom((Continuation) fun);
            return new REPair(arg.car, env);
        } else {
            throw new RuntimeException("not a functin: " +
                                       LS.stringify(fun) + " with " +
                                       LS.stringify(arg));
        }
    }
}
