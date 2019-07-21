// R01.06.08/R01.07.21 by SUZUKI Hisao
package little_scheme;

import java.io.IOException;

/** Evaluator of Scheme's expression */
public class Eval {
    private Object exp;
    private Env env;
    private Continuation k;

    /** Construct an expression evaluator with an environment. */
    public Eval(Object exp, Env env) {
        this.exp = exp;
        this.env = env;
        this.k = new Continuation();
    }

    /** Evaluate the given expression in the given environment.
        This method is not re-entrant.
    */
    public Object evaluate() throws IOException {
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
                            k.push(ContOp.THEN, kdr.cdr);
                        } else if (kar == Sym.BEGIN) { // (begin e...)
                            exp = kdr.car;
                            if (kdr.cdr != null)
                                k.push(ContOp.BEGIN, kdr.cdr);
                        } else if (kar == Sym.LAMBDA) { // (lambda (v..) e...)
                            Cell params = (Cell) kdr.car;
                            Cell body = (Cell) kdr.cdr;
                            exp = new Closure(params, body, env);
                            break;
                        } else if (kar == Sym.DEFINE) { // (define v e)
                            exp = ((Cell) kdr.cdr).car;
                            k.push(ContOp.DEFINE, kdr.car);
                        } else if (kar == Sym.SETQ) {   // (set! v e)
                            exp = ((Cell) kdr.cdr).car;
                            Sym v = (Sym) kdr.car;
                            k.push(ContOp.SETQ, env.lookFor(v));
                        } else { // (fun arg...)
                            exp = kar;
                            k.push(ContOp.APPLY, kdr);
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
                    ContOp op = step.op;
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
                            k.push(ContOp.BEGIN, c.cdr);
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
                            applyFunction(exp, null);
                            break;
                        } else {
                            k.push(ContOp.APPLY_FUN, exp);
                            Cell c = (Cell) x;
                            while (c.cdr != null) {
                                k.push(ContOp.EVAL_ARG, c.car);
                                c = (Cell) c.cdr;
                            }
                            exp = c.car;
                            k.push(ContOp.CONS_ARGS, null);
                            break LOOP2;
                        }
                    case CONS_ARGS: {
                        // x is a list of evaluated args (to be cdr);
                        // exp is a newly evaluated arg (to be car).
                        Cell args = new Cell(exp, x);
                        step = k.pop();
                        op = step.op;
                        exp = step.val;
                        switch (op) {
                        case EVAL_ARG: // exp is the next arg.
                            k.push(ContOp.CONS_ARGS, args);
                            break LOOP2;
                        case APPLY_FUN: { // exp is the evaluated function.
                            applyFunction(exp, args);
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
        } catch (ErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            if (k.isEmpty())
                throw ex;
            throw new RuntimeException
                (ex.getMessage() + "\n\t" + LS.stringify(k), ex);
        }
    }

    /** Apply a function to arguments with a continuation. */
    private void applyFunction(Object fun, Cell arg) throws IOException {
        for (;;) {
            if (fun == Sym.CALLCC) {
                k.pushRestoreEnv(env);
                fun = arg.car;
                Continuation cont = new Continuation(k);
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
            exp = f.fun.call(arg);
        } else if (fun instanceof Closure) {
            Closure f = (Closure) fun;
            k.pushRestoreEnv(env);
            k.push(ContOp.BEGIN, f.body);
            exp = LS.NONE;
            env = new Env(null, // frame marker
                          null,
                          f.env.prependDefs(f.params, arg));
        } else if (fun instanceof Continuation) {
            k = new Continuation((Continuation) fun);
            exp = arg.car;
        } else {
            throw new RuntimeException("not a function: " +
                                       LS.stringify(fun) + " with " +
                                       LS.stringify(arg));
        }
    }
}
