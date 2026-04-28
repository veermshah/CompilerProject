import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AstNode extends Token implements SemanticNode {
    protected static String indent(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            builder.append("    ");
        }
        return builder.toString();
    }

    protected static String joinLines(List<? extends AstNode> nodes, int indentLevel) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            builder.append(nodes.get(i).toString(indentLevel));
            if (i + 1 < nodes.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}

final class TypeHelpers {
    private TypeHelpers() {
    }

    static TypeInfo fromPrimitive(String name) {
        return TypeInfo.fromPrimitive(name);
    }

    static void ensureAssignable(TypeInfo target, TypeInfo source, String message) throws SemanticException {
        if (!target.isAssignableFrom(source)) {
            throw new SemanticException(message + ": cannot assign " + source + " to " + target);
        }
    }

    static void ensureBoolCondition(TypeInfo type, String message) throws SemanticException {
        if (!type.isBoolCoercible()) {
            throw new SemanticException(message + ": condition must be bool-compatible, found " + type);
        }
    }

    static void ensureNumeric(TypeInfo type, String message) throws SemanticException {
        if (!type.isNumeric()) {
            throw new SemanticException(message + ": numeric operand required, found " + type);
        }
    }

    static void ensureBoolCompatible(TypeInfo type, String message) throws SemanticException {
        if (!type.isBoolCoercible()) {
            throw new SemanticException(message + ": bool or bool-coercible operand required, found " + type);
        }
    }

    static TypeInfo numericResult(TypeInfo left, TypeInfo right) throws SemanticException {
        if (!left.isNumeric() || !right.isNumeric()) {
            throw new SemanticException("numeric operands required, found " + left + " and " + right);
        }
        if (left.getKind() == TypeInfo.Kind.FLOAT || right.getKind() == TypeInfo.Kind.FLOAT) {
            return TypeInfo.FLOAT;
        }
        return TypeInfo.INT;
    }

    static TypeInfo equalityResult(TypeInfo left, TypeInfo right) throws SemanticException {
        if (left.isNumeric() && right.isNumeric()) {
            return TypeInfo.BOOL;
        }
        throw new SemanticException("numeric equality operands required, found " + left + " and " + right);
    }
}

final class ClassDeclNode extends AstNode {
    private final String name;
    private final List<MemberDeclNode> members;

    ClassDeclNode(String name, List<MemberDeclNode> members) {
        this.name = name;
        this.members = members == null ? Collections.emptyList() : new ArrayList<>(members);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append("class ").append(name).append(" {\n");
        for (int i = 0; i < members.size(); i++) {
            builder.append(members.get(i).toString(indentLevel + 1));
            if (i + 1 < members.size()) {
                builder.append("\n");
            }
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        context.enterClass(name);
        try {
            for (MemberDeclNode member : members) {
                member.register(context);
                member.typeCheck(context);
            }
        } finally {
            context.exitClass();
        }
        return TypeInfo.VOID;
    }
}

abstract class MemberDeclNode extends AstNode {
    protected final String name;

    MemberDeclNode(String name) {
        this.name = name;
    }

    abstract void register(SemanticContext context) throws SemanticException;
}

final class FieldDeclNode extends MemberDeclNode {
    private final TypeInfo type;
    private final boolean isFinal;
    private final ExprNode initializer;
    private final boolean isArray;

    FieldDeclNode(String name, TypeInfo type, boolean isFinal, ExprNode initializer, boolean isArray) {
        super(name);
        this.type = type;
        this.isFinal = isFinal;
        this.initializer = initializer;
        this.isArray = isArray;
    }

    @Override
    void register(SemanticContext context) throws SemanticException {
        TypeInfo declaredType = isArray ? TypeInfo.arrayOf(type) : type;
        context.symbols().declare(name, SymbolTable.SymbolKind.FIELD, declaredType, isFinal, null, null);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel));
        if (isFinal) {
            builder.append("final ");
        }
        builder.append(type);
        builder.append(isArray ? "[] " : " ");
        builder.append(name);
        if (initializer != null) {
            builder.append(" = ").append(initializer.toString(0));
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        if (initializer != null) {
            TypeInfo actual = initializer.typeCheck(context);
            TypeInfo expected = isArray ? TypeInfo.arrayOf(type) : type;
            TypeHelpers.ensureAssignable(expected, actual, "field initializer for " + name);
        }
        return TypeInfo.VOID;
    }
}

final class MethodDeclNode extends MemberDeclNode {
    private final TypeInfo returnType;
    private final List<ParamDeclNode> params;
    private final BlockStmtNode body;
    private final boolean isVoid;

    MethodDeclNode(TypeInfo returnType, String name, List<ParamDeclNode> params, BlockStmtNode body, boolean isVoid) {
        super(name);
        this.returnType = returnType;
        this.params = params == null ? Collections.emptyList() : new ArrayList<>(params);
        this.body = body;
        this.isVoid = isVoid;
    }

    @Override
    void register(SemanticContext context) throws SemanticException {
        List<String> paramNames = new ArrayList<>();
        List<TypeInfo> paramTypes = new ArrayList<>();
        for (ParamDeclNode param : params) {
            paramNames.add(param.name());
            paramTypes.add(param.type());
        }
        context.symbols().declare(name, SymbolTable.SymbolKind.METHOD, isVoid ? TypeInfo.VOID : returnType, false,
                paramNames, paramTypes);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel));
        builder.append(isVoid ? "void" : returnType.toString());
        builder.append(" ").append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            builder.append(params.get(i).toString(0));
            if (i + 1 < params.size()) {
                builder.append(", ");
            }
        }
        builder.append(") ").append(body.toString(indentLevel));
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        context.enterMethod(name, isVoid ? TypeInfo.VOID : returnType);
        try {
            for (ParamDeclNode param : params) {
                param.register(context);
            }
            body.typeCheck(context);
            if (!isVoid && !body.containsReturn()) {
                throw new SemanticException("missing return in method " + name);
            }
        } finally {
            context.exitMethod();
        }
        return TypeInfo.VOID;
    }
}

final class ParamDeclNode extends AstNode {
    private final String name;
    private final TypeInfo type;
    private final boolean isArray;

    ParamDeclNode(String name, TypeInfo type, boolean isArray) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
    }

    TypeInfo type() {
        return isArray ? TypeInfo.arrayOf(type) : type;
    }

    String name() {
        return name;
    }

    void register(SemanticContext context) throws SemanticException {
        context.symbols().declare(name, SymbolTable.SymbolKind.PARAMETER, type(), false, null, null);
    }

    @Override
    public String toString(int indentLevel) {
        return type + (isArray ? "[]" : "") + " " + name;
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) {
        return TypeInfo.VOID;
    }
}

abstract class StmtNode extends AstNode {
    boolean containsReturn() {
        return false;
    }
}

final class BlockStmtNode extends StmtNode {
    private final List<VarDeclStmtNode> declarations;
    private final List<StmtNode> statements;

    BlockStmtNode(List<VarDeclStmtNode> declarations, List<StmtNode> statements) {
        this.declarations = declarations == null ? Collections.emptyList() : new ArrayList<>(declarations);
        this.statements = statements == null ? Collections.emptyList() : new ArrayList<>(statements);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (VarDeclStmtNode declaration : declarations) {
            builder.append(declaration.toString(indentLevel + 1)).append("\n");
        }
        for (StmtNode statement : statements) {
            builder.append(statement.toString(indentLevel + 1)).append("\n");
        }
        builder.append(indent(indentLevel)).append("}");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        context.enterBlock();
        for (VarDeclStmtNode declaration : declarations) {
            declaration.typeCheck(context);
        }
        for (StmtNode statement : statements) {
            statement.typeCheck(context);
        }
        context.exitBlock();
        return TypeInfo.VOID;
    }

    @Override
    boolean containsReturn() {
        for (StmtNode statement : statements) {
            if (statement.containsReturn()) {
                return true;
            }
        }
        return false;
    }
}

final class VarDeclStmtNode extends StmtNode {
    private final String name;
    private final TypeInfo type;
    private final boolean isArray;
    private final ExprNode initializer;
    private final boolean isFinal;

    VarDeclStmtNode(String name, TypeInfo type, boolean isArray, ExprNode initializer, boolean isFinal) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.initializer = initializer;
        this.isFinal = isFinal;
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel));
        if (isFinal) {
            builder.append("final ");
        }
        builder.append(type);
        builder.append(isArray ? "[] " : " ");
        builder.append(name);
        if (initializer != null) {
            builder.append(" = ").append(initializer.toString(0));
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo declaredType = isArray ? TypeInfo.arrayOf(type) : type;
        context.symbols().declare(name, SymbolTable.SymbolKind.LOCAL, declaredType, isFinal, null, null);
        if (initializer != null) {
            TypeInfo actual = initializer.typeCheck(context);
            TypeHelpers.ensureAssignable(declaredType, actual, "local initializer for " + name);
        }
        return TypeInfo.VOID;
    }
}

final class IfStmtNode extends StmtNode {
    private final ExprNode condition;
    private final StmtNode thenBranch;
    private final StmtNode elseBranch;

    IfStmtNode(ExprNode condition, StmtNode thenBranch, StmtNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel)).append("if (").append(condition.toString(0)).append(") ");
        builder.append(thenBranch.toString(indentLevel));
        if (elseBranch != null) {
            builder.append(" else ").append(elseBranch.toString(indentLevel));
        }
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeHelpers.ensureBoolCondition(condition.typeCheck(context), "if statement");
        thenBranch.typeCheck(context);
        if (elseBranch != null) {
            elseBranch.typeCheck(context);
        }
        return TypeInfo.VOID;
    }

    @Override
    boolean containsReturn() {
        return elseBranch != null && thenBranch.containsReturn() && elseBranch.containsReturn();
    }
}

final class WhileStmtNode extends StmtNode {
    private final ExprNode condition;
    private final StmtNode body;

    WhileStmtNode(ExprNode condition, StmtNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString(int indentLevel) {
        return indent(indentLevel) + "while (" + condition.toString(0) + ") " + body.toString(indentLevel);
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeHelpers.ensureBoolCondition(condition.typeCheck(context), "while statement");
        body.typeCheck(context);
        return TypeInfo.VOID;
    }
}

final class AssignStmtNode extends StmtNode {
    private final ExprNode target;
    private final ExprNode value;

    AssignStmtNode(ExprNode target, ExprNode value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public String toString(int indentLevel) {
        return indent(indentLevel) + target.toString(0) + " = " + value.toString(0) + ";";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo targetType = target.typeCheck(context);
        TypeInfo valueType = value.typeCheck(context);
        if (!target.isAssignable()) {
            throw new SemanticException("assignment target is not assignable: " + target.toString(0));
        }
        if (target.isFinalTarget(context)) {
            throw new SemanticException("cannot reassign final value: " + target.toString(0));
        }
        TypeHelpers.ensureAssignable(targetType, valueType, "assignment");
        return TypeInfo.VOID;
    }
}

final class ReadStmtNode extends StmtNode {
    private final List<ExprNode> targets;

    ReadStmtNode(List<ExprNode> targets) {
        this.targets = targets == null ? Collections.emptyList() : new ArrayList<>(targets);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel)).append("read(");
        for (int i = 0; i < targets.size(); i++) {
            builder.append(targets.get(i).toString(0));
            if (i + 1 < targets.size()) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        for (ExprNode target : targets) {
            TypeInfo type = target.typeCheck(context);
            if (!target.isAssignable()) {
                throw new SemanticException("read target is not assignable: " + target.toString(0));
            }
            if (target.isFinalTarget(context)) {
                throw new SemanticException("cannot read into final value: " + target.toString(0));
            }
            if (type.getKind() == TypeInfo.Kind.ARRAY || type.getKind() == TypeInfo.Kind.VOID) {
                throw new SemanticException("read target has invalid type: " + type);
            }
        }
        return TypeInfo.VOID;
    }
}

final class PrintStmtNode extends StmtNode {
    private final List<ExprNode> values;
    private final boolean line;

    PrintStmtNode(List<ExprNode> values, boolean line) {
        this.values = values == null ? Collections.emptyList() : new ArrayList<>(values);
        this.line = line;
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(indentLevel)).append(line ? "printline(" : "print(");
        for (int i = 0; i < values.size(); i++) {
            builder.append(values.get(i).toString(0));
            if (i + 1 < values.size()) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        for (ExprNode value : values) {
            TypeInfo type = value.typeCheck(context);
            if (type.getKind() == TypeInfo.Kind.VOID || type.getKind() == TypeInfo.Kind.ARRAY) {
                throw new SemanticException("print operand has invalid type: " + type);
            }
        }
        return TypeInfo.VOID;
    }
}

final class CallStmtNode extends StmtNode {
    private final CallExprNode call;

    CallStmtNode(String name, List<ExprNode> arguments) {
        this.call = new CallExprNode(name, arguments);
    }

    @Override
    public String toString(int indentLevel) {
        return indent(indentLevel) + call.toString(0) + ";";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        call.typeCheck(context);
        return TypeInfo.VOID;
    }
}

final class ReturnStmtNode extends StmtNode {
    private final ExprNode value;

    ReturnStmtNode(ExprNode value) {
        this.value = value;
    }

    @Override
    public String toString(int indentLevel) {
        if (value == null) {
            return indent(indentLevel) + "return;";
        }
        return indent(indentLevel) + "return " + value.toString(0) + ";";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo expected = context.currentReturnType();
        if (expected.getKind() == TypeInfo.Kind.VOID) {
            if (value != null) {
                throw new SemanticException("void method cannot return a value");
            }
            return TypeInfo.VOID;
        }
        if (value == null) {
            throw new SemanticException("non-void method must return a value");
        }
        TypeInfo actual = value.typeCheck(context);
        TypeHelpers.ensureAssignable(expected, actual, "return statement");
        return TypeInfo.VOID;
    }

    @Override
    boolean containsReturn() {
        return true;
    }
}

final class IncDecStmtNode extends StmtNode {
    private final ExprNode target;
    private final boolean increment;

    IncDecStmtNode(ExprNode target, boolean increment) {
        this.target = target;
        this.increment = increment;
    }

    @Override
    public String toString(int indentLevel) {
        return indent(indentLevel) + target.toString(0) + (increment ? "++" : "--") + ";";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo type = target.typeCheck(context);
        if (!target.isAssignable()) {
            throw new SemanticException("increment/decrement target is not assignable: " + target.toString(0));
        }
        if (target.isFinalTarget(context)) {
            throw new SemanticException("cannot modify final value: " + target.toString(0));
        }
        if (!type.isNumeric()) {
            String suffix = increment ? "++;" : "--;";
            throw new SemanticException(context.errorPrefix() + "Cannot increment/decrement variable of type: " + type
                    + " line: " + target.toString(0) + suffix);
        }
        return TypeInfo.VOID;
    }
}

abstract class ExprNode extends AstNode {
    boolean isAssignable() {
        return false;
    }

    boolean isFinalTarget(SemanticContext context) {
        return false;
    }
}

final class LiteralExprNode extends ExprNode {
    private final String literal;
    private final TypeInfo type;

    LiteralExprNode(String literal, TypeInfo type) {
        this.literal = literal;
        this.type = type;
    }

    @Override
    public String toString(int indentLevel) {
        return literal;
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) {
        return type;
    }
}

final class NameExprNode extends ExprNode {
    private final String name;

    NameExprNode(String name) {
        this.name = name;
    }

    @Override
    public String toString(int indentLevel) {
        return name;
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        SymbolTable.Symbol symbol = context.symbols().lookup(name);
        if (symbol == null) {
            throw new SemanticException("undeclared identifier: " + name);
        }
        if (symbol.kind == SymbolTable.SymbolKind.METHOD) {
            throw new SemanticException("method identifier cannot be used as variable: " + name);
        }
        return symbol.type;
    }

    @Override
    boolean isAssignable() {
        return true;
    }

    @Override
    boolean isFinalTarget(SemanticContext context) {
        SymbolTable.Symbol symbol = context.symbols().lookup(name);
        return symbol != null && symbol.isFinal;
    }
}

final class IndexExprNode extends ExprNode {
    private final ExprNode base;
    private final ExprNode index;

    IndexExprNode(ExprNode base, ExprNode index) {
        this.base = base;
        this.index = index;
    }

    @Override
    public String toString(int indentLevel) {
        return base.toString(0) + "[" + index.toString(0) + "]";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo baseType = base.typeCheck(context);
        TypeInfo indexType = index.typeCheck(context);
        if (indexType.getKind() != TypeInfo.Kind.INT) {
            throw new SemanticException("array index must be int, found " + indexType);
        }
        if (baseType.getKind() != TypeInfo.Kind.ARRAY) {
            throw new SemanticException("indexed value is not an array: " + baseType);
        }
        return baseType.getElementType();
    }

    @Override
    boolean isAssignable() {
        return true;
    }

    @Override
    boolean isFinalTarget(SemanticContext context) {
        if (base instanceof NameExprNode) {
            return ((NameExprNode) base).isFinalTarget(context);
        }
        return false;
    }
}

final class CallExprNode extends ExprNode {
    private final String name;
    private final List<ExprNode> arguments;

    CallExprNode(String name, List<ExprNode> arguments) {
        this.name = name;
        this.arguments = arguments == null ? Collections.emptyList() : new ArrayList<>(arguments);
    }

    @Override
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            builder.append(arguments.get(i).toString(0));
            if (i + 1 < arguments.size()) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        SymbolTable.Symbol symbol = context.symbols().lookupGlobal(name);
        if (symbol == null || symbol.kind != SymbolTable.SymbolKind.METHOD) {
            throw new SemanticException("undefined method: " + name);
        }
        if (symbol.parameterTypes.size() != arguments.size()) {
            throw new SemanticException("argument count mismatch for " + name);
        }
        for (int i = 0; i < arguments.size(); i++) {
            TypeInfo actual = arguments.get(i).typeCheck(context);
            TypeInfo expected = symbol.parameterTypes.get(i);
            TypeHelpers.ensureAssignable(expected, actual, "argument " + (i + 1) + " to " + name);
        }
        return symbol.type;
    }
}

final class UnaryExprNode extends ExprNode {
    private final String operator;
    private final ExprNode operand;
    private final TypeInfo castType;

    UnaryExprNode(String operator, ExprNode operand) {
        this(operator, operand, null);
    }

    UnaryExprNode(String operator, ExprNode operand, TypeInfo castType) {
        this.operator = operator;
        this.operand = operand;
        this.castType = castType;
    }

    @Override
    public String toString(int indentLevel) {
        if (castType != null) {
            return "(" + castType + ")" + operand.toString(0);
        }
        return operator + operand.toString(0);
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo type = operand.typeCheck(context);
        if (castType != null) {
            if (!castType.isScalar() || !type.isScalar()) {
                throw new SemanticException("invalid cast from " + type + " to " + castType);
            }
            return castType;
        }
        if ("+".equals(operator) || "-".equals(operator)) {
            TypeHelpers.ensureNumeric(type, "unary " + operator);
            return type;
        }
        if ("~".equals(operator)) {
            TypeHelpers.ensureBoolCompatible(type, "unary ~");
            return TypeInfo.BOOL;
        }
        throw new SemanticException("unsupported unary operator: " + operator);
    }
}

final class BinaryExprNode extends ExprNode {
    private final ExprNode left;
    private final String operator;
    private final ExprNode right;

    BinaryExprNode(ExprNode left, String operator, ExprNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString(int indentLevel) {
        return "(" + left.toString(0) + " " + operator + " " + right.toString(0) + ")";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeInfo leftType = left.typeCheck(context);
        TypeInfo rightType = right.typeCheck(context);
        if ("&&".equals(operator) || "||".equals(operator)) {
            TypeHelpers.ensureBoolCondition(leftType, "logical operator left operand");
            TypeHelpers.ensureBoolCondition(rightType, "logical operator right operand");
            return TypeInfo.BOOL;
        }
        if ("<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator)) {
            TypeHelpers.ensureNumeric(leftType, "comparison left operand");
            TypeHelpers.ensureNumeric(rightType, "comparison right operand");
            return TypeInfo.BOOL;
        }
        if ("==".equals(operator) || "<>".equals(operator)) {
            return TypeHelpers.equalityResult(leftType, rightType);
        }
        if ("+".equals(operator)) {
            if ((leftType.getKind() == TypeInfo.Kind.STRING && rightType.isScalar())
                    || (rightType.getKind() == TypeInfo.Kind.STRING && leftType.isScalar())) {
                return TypeInfo.STRING;
            }
            return TypeHelpers.numericResult(leftType, rightType);
        }
        if ("-".equals(operator) || "*".equals(operator) || "/".equals(operator)) {
            return TypeHelpers.numericResult(leftType, rightType);
        }
        throw new SemanticException("unsupported binary operator: " + operator);
    }
}

final class TernaryExprNode extends ExprNode {
    private final ExprNode condition;
    private final ExprNode whenTrue;
    private final ExprNode whenFalse;

    TernaryExprNode(ExprNode condition, ExprNode whenTrue, ExprNode whenFalse) {
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public String toString(int indentLevel) {
        return "(" + condition.toString(0) + " ? " + whenTrue.toString(0) + " : " + whenFalse.toString(0) + ")";
    }

    @Override
    public TypeInfo typeCheck(SemanticContext context) throws SemanticException {
        TypeHelpers.ensureBoolCondition(condition.typeCheck(context), "ternary condition");
        TypeInfo trueType = whenTrue.typeCheck(context);
        TypeInfo falseType = whenFalse.typeCheck(context);
        if (trueType.isSameBase(falseType)) {
            return trueType;
        }
        throw new SemanticException("ternary branches are incompatible: " + trueType + " and " + falseType);
    }
}
