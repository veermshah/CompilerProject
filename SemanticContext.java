public class SemanticContext {
    private final SymbolTable symbols;
    private TypeInfo currentReturnType = TypeInfo.VOID;
    private boolean inMethod = false;

    public SemanticContext() {
        this.symbols = new SymbolTable();
    }

    public SymbolTable symbols() {
        return symbols;
    }

    public TypeInfo currentReturnType() {
        return currentReturnType;
    }

    public void enterMethod(TypeInfo returnType) {
        currentReturnType = returnType;
        inMethod = true;
        symbols.enterScope();
    }

    public void exitMethod() {
        symbols.exitScope();
        currentReturnType = TypeInfo.VOID;
        inMethod = false;
    }

    public boolean isInMethod() {
        return inMethod;
    }

    public void enterBlock() {
        symbols.enterScope();
    }

    public void exitBlock() {
        symbols.exitScope();
    }
}