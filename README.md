# UTDLang Compiler Project

This repository contains a small compiler front-end for the UTD language. It implements the typical compiler phases needed for parsing and semantic analysis: a lexer (JFlex), a parser (CUP), AST node definitions, scoped symbol tables, and a type-checking/semantic analysis pass. The project includes a set of test programs and expected outputs to exercise the type checker.

Key components

- `tokens.jflex` — lexer specification (JFlex)
- `grammar.cup` — parser grammar (CUP)
- `Lexer.java`, `parser.java`, `sym.java` — generated lexer/parser scaffolding
- `AstNodes.java` — AST classes and node utilities
- `SemanticContext.java`, `SymbolTable.java`, `TypeInfo.java`, `SemanticException.java` — semantic analysis and type system
- `TypeCheckingTest.java`, `LexerTest.java`, `ScannerTest.java` — test drivers

Build & usage

The project uses a `Makefile` to regenerate lexer/parser sources and to compile/run tests. You will need a Java JDK and the JFlex/CUP jars referenced by the build.

Common targets:

```
make all   # generate sources and compile
make run   # run the default type-checker driver
```

Tests

- Test inputs are in `p3tests/p3tests/`.
- Expected outputs are in `p3tests-results/`.

This README provides a high-level overview. See the source files and the `Makefile` for build details and specific commands.
