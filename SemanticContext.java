public class SemanticContext {
    private final SymbolTable symbols;
    private TypeInfo currentReturnType = TypeInfo.VOID;
    private boolean inMethod = false;
    private String currentClassName = "";
    private String currentMethodName = "";

    public SemanticContext() {
        this.symbols = new SymbolTable();
    }

    public SymbolTable symbols() {
        return symbols;
    }

    public TypeInfo currentReturnType() {
        return currentReturnType;
    }

    public void enterClass(String className) {
        currentClassName = className;
    }

    public void exitClass() {
        currentClassName = "";
    }

    public void enterMethod(String methodName, TypeInfo returnType) {
        currentMethodName = methodName;
        currentReturnType = returnType;
        inMethod = true;
        symbols.enterScope();
    }

    public void exitMethod() {
        symbols.exitScope();
        currentMethodName = "";
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

    public String errorPrefix() {
        if (!currentClassName.isEmpty() && !currentMethodName.isEmpty()) {
            return "class<" + currentClassName + ">:" + currentReturnType + " " + currentMethodName + ":";
        }
        return "";
    }
}