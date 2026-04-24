public class Program extends Token {
    private final String text;
    private final SemanticNode semanticRoot;

    public Program(String text) {
        this.text = text == null ? "" : text;
        this.semanticRoot = null;
    }

    public Program(SemanticNode semanticRoot) {
        this.text = "";
        this.semanticRoot = semanticRoot;
    }

    @Override
    public String toString(int indentLevel) {
        if (semanticRoot != null) {
            return semanticRoot.toString(indentLevel);
        }
        return text;
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        if (semanticRoot == null) {
            throw new SemanticException("semantic analysis is not yet wired to the parser AST");
        }
        return semanticRoot.typeCheck(context);
    }

    public TypeInfo typeCheck() throws SemanticException {
        return typeCheck(new SemanticContext());
    }
}
