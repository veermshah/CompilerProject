public class SemanticException extends Exception {
    public SemanticException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}