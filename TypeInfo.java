public final class TypeInfo {
    public enum Kind {
        INT,
        FLOAT,
        BOOL,
        CHAR,
        STRING,
        VOID,
        ARRAY,
        ERROR
    }

    public static final TypeInfo INT = new TypeInfo(Kind.INT, null);
    public static final TypeInfo FLOAT = new TypeInfo(Kind.FLOAT, null);
    public static final TypeInfo BOOL = new TypeInfo(Kind.BOOL, null);
    public static final TypeInfo CHAR = new TypeInfo(Kind.CHAR, null);
    public static final TypeInfo STRING = new TypeInfo(Kind.STRING, null);
    public static final TypeInfo VOID = new TypeInfo(Kind.VOID, null);
    public static final TypeInfo ERROR = new TypeInfo(Kind.ERROR, null);

    private final Kind kind;
    private final TypeInfo elementType;

    private TypeInfo(Kind kind, TypeInfo elementType) {
        this.kind = kind;
        this.elementType = elementType;
    }

    public static TypeInfo arrayOf(TypeInfo elementType) {
        return new TypeInfo(Kind.ARRAY, elementType);
    }

    public Kind getKind() {
        return kind;
    }

    public TypeInfo getElementType() {
        return elementType;
    }

    public boolean isNumeric() {
        return kind == Kind.INT || kind == Kind.FLOAT;
    }

    public boolean isScalar() {
        return kind != Kind.ARRAY && kind != Kind.VOID && kind != Kind.ERROR;
    }

    public boolean isAssignableFrom(TypeInfo source) {
        if (this == ERROR || source == ERROR) {
            return true;
        }
        if (kind == source.kind) {
            if (kind != Kind.ARRAY) {
                return true;
            }
            return elementType.isSameBase(source.elementType);
        }
        if (kind == Kind.FLOAT && source.kind == Kind.INT) {
            return true;
        }
        if (kind == Kind.BOOL && source.kind == Kind.INT) {
            return true;
        }
        if (kind == Kind.STRING && source.kind != Kind.ARRAY && source.kind != Kind.VOID) {
            return true;
        }
        return false;
    }

    public boolean isBoolCoercible() {
        return kind == Kind.BOOL || kind == Kind.INT;
    }

    public boolean isSameBase(TypeInfo other) {
        if (this == ERROR || other == ERROR) {
            return true;
        }
        if (kind != other.kind) {
            return false;
        }
        if (kind != Kind.ARRAY) {
            return true;
        }
        return elementType.isSameBase(other.elementType);
    }

    @Override
    public String toString() {
        if (kind == Kind.ARRAY) {
            return elementType + "[]";
        }
        return kind.name().toLowerCase();
    }

    public static TypeInfo fromPrimitive(String name) {
        if ("int".equals(name)) {
            return INT;
        }
        if ("float".equals(name)) {
            return FLOAT;
        }
        if ("bool".equals(name)) {
            return BOOL;
        }
        if ("char".equals(name)) {
            return CHAR;
        }
        if ("string".equals(name)) {
            return STRING;
        }
        return ERROR;
    }
}
