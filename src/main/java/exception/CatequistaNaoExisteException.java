package exception;

public class CatequistaNaoExisteException extends RuntimeException {
    public CatequistaNaoExisteException(String message) {
        super(message);
    }
}
