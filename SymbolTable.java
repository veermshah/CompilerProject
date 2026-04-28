import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    public enum SymbolKind {
        FIELD,
        METHOD,
        PARAMETER,
        LOCAL
    }

    public static final class Symbol {
        public final String name;
        public final SymbolKind kind;
        public final TypeInfo type;
        public final boolean isFinal;
        public final List<String> parameterNames;
        public final List<TypeInfo> parameterTypes;

        public Symbol(String name, SymbolKind kind, TypeInfo type, boolean isFinal, List<String> parameterNames,
                List<TypeInfo> parameterTypes) {
            this.name = name;
            this.kind = kind;
            this.type = type;
            this.isFinal = isFinal;
            if (parameterNames == null) {
                this.parameterNames = Collections.emptyList();
            } else {
                this.parameterNames = new ArrayList<>(parameterNames);
            }
            if (parameterTypes == null) {
                this.parameterTypes = Collections.emptyList();
            } else {
                this.parameterTypes = new ArrayList<>(parameterTypes);
            }
        }
    }

    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
    private final List<Symbol> globalDeclarations = new ArrayList<>();

    public SymbolTable() {
        scopes.push(new LinkedHashMap<>());
    }

    public void enterScope() {
        scopes.push(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (scopes.size() <= 1) {
            return;
        }
        scopes.pop();
    }

    public Symbol declare(String name, SymbolKind kind, TypeInfo type, boolean isFinal, List<String> parameterNames,
            List<TypeInfo> parameterTypes) throws SemanticException {
        Map<String, Symbol> current = scopes.peek();
        if (current.containsKey(name)) {
            throw new SemanticException("same-scope redeclaration of '" + name + "'");
        }
        Symbol symbol = new Symbol(name, kind, type, isFinal, parameterNames, parameterTypes);
        current.put(name, symbol);
        if (scopes.size() == 1) {
            globalDeclarations.add(symbol);
        }
        return symbol;
    }

    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    public Symbol lookupGlobal(String name) {
        for (Symbol symbol : globalDeclarations) {
            if (symbol.name.equals(name)) {
                return symbol;
            }
        }
        return null;
    }
}