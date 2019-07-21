// R01.07.21 by SUZUKI Hisao
package little_scheme;

/** Exception thrown by error procedure of SRFI-23 */
public class ErrorException extends RuntimeException {
    public ErrorException(Object reason, Object arg) {
        super("Error: " + LS.stringify(reason, false) + ": " +
              LS.stringify(arg));
    }
}
