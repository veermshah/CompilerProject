public interface SemanticNode {
    String toString(int indentLevel);

    TypeInfo typeCheck(SemanticContext context) throws SemanticException;
}