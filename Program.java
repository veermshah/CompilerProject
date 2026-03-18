public class Program extends Token {
    private final String text;

    public Program(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    public String toString(int indentLevel) {
        return text;
    }
}
