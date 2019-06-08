// R01.06.08 by SUZUKI Hisao
package little_scheme;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Cons cell */
public class Cell implements Iterable {
    public Object car;
    public Object cdr;

    public Cell(Object car, Object cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    /** Yield car, cadr, caddr and so on. */
    public Iterator iterator() {
        return new Iterator() {
            Object j = Cell.this;

            public boolean hasNext() {
                return asCell(j) != null;
            }

            public Object next() {
                if (j == null)
                    throw new NoSuchElementException();
                Cell c = asCell(j);
                j = c.cdr;
                return c.car;
            }
        };
    }

    /** Return the number of elements in this list. */
    public int size() {
        int n = 0;
        for (Object e: this)
            n++;
        return n;
    }

    //----------------------------------------------------------------------

    /** Cast it as a cons cell or throw ImproperListException.  */
    public static Cell asCell(Object j) throws ImproperListException {
        if (j == null)
            return null;
        else if (j instanceof Cell)
            return (Cell) j;
        else
            throw new ImproperListException(j);
    }

    /** Exception to carry up the non-cell tail */
    public static class ImproperListException extends RuntimeException {
        public final Object tail;

        public ImproperListException(Object tail) {
            this.tail = tail;
        }
    }
}

