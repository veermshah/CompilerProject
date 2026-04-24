public abstract class Token {
    public abstract String toString(int indentLevel);

    public abstract TypeInfo typeCheck(SemanticContext context) throws SemanticException;
}
