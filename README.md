# A Little Scheme in Java

This is a small interpreter of a subset of Scheme
in circa 900 lines of _Java 8_ (including a small arithmetic package
[`little_arith`](little_arith) in circa 100 lines).
It implements the same language as
[little-scheme-in-python](https://github.com/nukata/little-scheme-in-python),
[little-scheme-in-go](https://github.com/nukata/little-scheme-in-go)
and their meta-circular interpreter, 
[little-scheme](https://github.com/nukata/little-scheme).

As a Scheme implementation, 
it optimizes _tail calls_ and handles _first-class continuations_ properly.


## How to run

```
$ make
rm -f little_arith/*.class little_scheme/*.class
javac -encoding utf-8 little_scheme/Main.java
jar cfm little-scheme.jar Manifest little_arith/*.class little_scheme/*.class
$ java -jar little-scheme.jar
> (+ 5 6)
11
> (cons 'a (cons 'b 'c))
(a b . c)
> (list
| 1
| 2
| 3
| )
(1 2 3)
> 
```

Press EOF (e.g. Control-D) to exit the session.

```
> Goodbye
$ 
```

You can run it with a Scheme script.
Examples are found in 
[little-scheme](https://github.com/nukata/little-scheme).

```
$ java -jar little-scheme.jar ../little-scheme/examples/yin-yang-puzzle.scm |
> head

*
**
***
****
*****
******
*******
********
*********
^C$
$ time java -jar little-scheme.jaro ../little-scheme/scm.scm \
> < ../little-scheme/examples/nqueens.scm 
((5 3 1 6 4 2) (4 1 5 2 6 3) (3 6 2 5 1 4) (2 4 6 1 3 5))

real	0m1.660s
user	0m1.819s
sys	0m0.223s
$ 
```

Press INTR (e.g. Control-C) to terminate the yin-yang-puzzle.

Put a "`-`" after the script in the command line to begin a session 
after running the script.

```
$ java -jar little-scheme.jar ../little-scheme/examples/fib90.scm -
2880067194370816120
> (globals)
(apply call/cc globals = < * - + symbol? eof-object? read newline display list n
ot null? pair? eqv? eq? cons cdr car fibonacci)
> (fibonacci 1000)
43466557686937456435688527675040625802564660517371780402481729089536555417949051
89040387984007925516929592259308032263477520968962323987332247116164299644090653
3187938298969649928516003704476137795166849228875
> 
```


## The implemented language

| Scheme Expression                   | Internal Representation             |
|:------------------------------------|:------------------------------------|
| numbers `1`, `2.3`                  | `java.lang.Number`                  |
| `#t`                                | `java.lang.Boolean.TRUE`            |
| `#f`                                | `java.lang.Boolean.FALSE`           |
| strings `"hello, world"`            | `java.lang.String`                  |
| symbols `a`, `+`                    | `little_scheme.Sym`                 |
| `()`                                | `null`                              |
| pairs `(1 . 2)`, `(x y z)`          | `little_scheme.Cell`                |
| closures `(lambda (x) (+ x 1))`     | `little_scheme.Closure`             |
| built-in procedures `car`, `cdr`    | `little_scheme.Intrinsic`           |
| continuations                       | `little_scheme.Continuation`        |


The implementation is similar to that of
[little-scheme-in-dart](https://github.com/nukata/little-scheme-in-dart).

For expression types and built-in procedures, see
[little-scheme-in-python](https://github.com/nukata/little-scheme-in-python).
